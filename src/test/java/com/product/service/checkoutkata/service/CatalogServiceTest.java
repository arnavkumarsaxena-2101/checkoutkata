package com.product.service.checkoutkata.service;

import com.product.service.checkoutkata.domain.PricingRule;
import com.product.service.checkoutkata.domain.Product;
import com.product.service.checkoutkata.domain.RuleType;
import com.product.service.checkoutkata.repo.PricingRuleRepository;
import com.product.service.checkoutkata.repo.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock ProductRepository products;
    @Mock PricingRuleRepository rules;
    @InjectMocks CatalogService service;

    @Test
    void allProducts_shouldReturnListFromRepo() {
        var p1 = new Product("A", new BigDecimal("50.00"));
        var p2 = new Product( "B", new BigDecimal("30.00"));
        when(products.findAll()).thenReturn(List.of(p1, p2));

        var result = service.allProducts();

        assertThat(result).containsExactly(p1, p2);
        verify(products).findAll();
        verifyNoMoreInteractions(products, rules);
    }

    @Test
    void rulesFor_shouldDelegateToRepo() {
        var r = new PricingRule("A", RuleType.BULK_X_FOR_Y, 3, new BigDecimal("130.00"));
        when(rules.findBySku("A")).thenReturn(List.of(r));

        var result = service.rulesFor("A");

        assertThat(result).containsExactly(r);
        verify(rules).findBySku("A");
        verifyNoMoreInteractions(products, rules);
    }

    @Test
    void upsertProduct_whenSkuExists_shouldMutatePrice_andNotCallSave() {
        var existing = new Product("A", new BigDecimal("10.00"));
        when(products.findBySku("A")).thenReturn(Optional.of(existing));

        var input = new Product("A", new BigDecimal("50.00"));

        var returned = service.upsertProduct(input);

        // same instance returned, mutated price
        assertThat(returned).isSameAs(existing);
        assertThat(returned.getUnitPrice()).isEqualByComparingTo("50.00");

        verify(products).findBySku("A");
        verify(products, never()).save(any());
        verifyNoMoreInteractions(products, rules);
    }

    @Test
    void upsertProduct_whenSkuMissing_shouldSaveAndReturnPersisted() {
        when(products.findBySku("C")).thenReturn(Optional.empty());
        var toCreate = new Product("C", new BigDecimal("20.00"));
        var persisted = new Product("C", new BigDecimal("20.00"));
        when(products.save(toCreate)).thenReturn(persisted);

        var returned = service.upsertProduct(toCreate);

        assertThat(returned).isEqualTo(persisted);

        verify(products).findBySku("C");
        verify(products).save(toCreate);
        verifyNoMoreInteractions(products, rules);
    }

    @Test
    void addRule_shouldSaveAndReturnEntity() {
        var toSave = new PricingRule("B", RuleType.BULK_X_FOR_Y, 2, new BigDecimal("45.00"));
        var persisted = new PricingRule("B", RuleType.BULK_X_FOR_Y, 2, new BigDecimal("45.00"));
        when(rules.save(toSave)).thenReturn(persisted);

        var result = service.addRule(toSave);

        assertThat(result).isEqualTo(persisted);
        verify(rules).save(toSave);
        verifyNoMoreInteractions(products, rules);
    }
}
