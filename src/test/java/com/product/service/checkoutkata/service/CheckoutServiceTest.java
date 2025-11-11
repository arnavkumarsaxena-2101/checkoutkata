package com.product.service.checkoutkata.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.product.service.checkoutkata.domain.Product;
import com.product.service.checkoutkata.repo.PricingRuleRepository;
import com.product.service.checkoutkata.repo.ProductRepository;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {
  @Mock ProductRepository products;
  @Mock PricingRuleRepository rules;
  @InjectMocks CheckoutService service;

  private static Product product(String sku, String price) {
    return new Product(sku, new BigDecimal(price));
  }

  @Test
  @DisplayName("priceOf(null) -> 0.00 and no repository interactions")
  void nullInput() {
    var total = service.priceOf(null);
    assertThat(total).isEqualByComparingTo("0.00");
    verifyNoInteractions(products, rules);
  }

  @Test
  @DisplayName("priceOf(blank) -> 0.00 and no repository interactions")
  void blankInput() {
    var total = service.priceOf("   ");
    assertThat(total).isEqualByComparingTo("0.00");
    verifyNoInteractions(products, rules);
  }

  @Test
  @DisplayName("Filters non-letters and uppercases letters: 'a-1B*' -> counts A=1,B=1")
  void filtersAndUppercases() {
    when(products.findBySku("A")).thenReturn(Optional.of(product("A", "50.00")));
    when(products.findBySku("B")).thenReturn(Optional.of(product("B", "30.00")));
    when(rules.findBySku("A")).thenReturn(List.of());
    when(rules.findBySku("B")).thenReturn(List.of());

    var total = service.priceOf("a-1B*");

    assertThat(total).isEqualByComparingTo("80.00");

    verify(products, times(1)).findBySku("A");
    verify(products, times(1)).findBySku("B");
    verify(rules, times(1)).findBySku("A");
    verify(rules, times(1)).findBySku("B");
    verifyNoMoreInteractions(products, rules);
  }

  @Test
  @DisplayName("Counts per distinct SKU; looks up product/rules once per SKU even if repeated")
  void countsDistinctSkus() {
    when(products.findBySku("B")).thenReturn(Optional.of(product("B", "30.00")));
    when(rules.findBySku("B")).thenReturn(List.of());

    var total = service.priceOf("BBB");

    assertThat(total.scale()).isGreaterThanOrEqualTo(2);

    verify(products, times(1)).findBySku("B");
    verify(rules, times(1)).findBySku("B");
    verifyNoMoreInteractions(products, rules);
  }

  @Test
  @DisplayName("Unknown SKU -> throws NoSuchElementException with message 'Unknown SKU: Z'")
  void unknownSkuThrows() {
    when(products.findBySku("Z")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.priceOf("Z"))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("Unknown SKU: Z");

    verify(products, times(1)).findBySku("Z");
    verifyNoMoreInteractions(products, rules);
  }

  @Test
  @DisplayName("Mixed sequence with qty=1 each -> sums unit prices")
  void mixedNoBundles() {
    when(products.findBySku("A")).thenReturn(Optional.of(product("A", "50.00")));
    when(products.findBySku("C")).thenReturn(Optional.of(product("C", "20.00")));
    when(rules.findBySku(anyString())).thenReturn(List.of()); // no specials

    var total = service.priceOf("Ca"); // case mix

    assertThat(total).isEqualByComparingTo("70.00");

    verify(products, times(1)).findBySku("A");
    verify(products, times(1)).findBySku("C");
    verify(rules, times(1)).findBySku("A");
    verify(rules, times(1)).findBySku("C");
    verifyNoMoreInteractions(products, rules);
  }

  @Test
  @DisplayName("Queries rules for each resolved product SKU (A & D)")
  void queriesRulesForEachResolvedSku() {
    when(products.findBySku("A")).thenReturn(Optional.of(product("A", "50.00")));
    when(products.findBySku("D")).thenReturn(Optional.of(product("D", "15.00")));
    when(rules.findBySku(anyString())).thenReturn(List.of());

    service.priceOf("AD");

    ArgumentCaptor<String> skuCaptor = ArgumentCaptor.forClass(String.class);
    verify(rules, times(2)).findBySku(skuCaptor.capture());
    assertThat(skuCaptor.getAllValues()).containsExactlyInAnyOrder("A", "D");
  }
}
