package com.product.service.checkoutkata.api;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.service.checkoutkata.domain.PricingRule;
import com.product.service.checkoutkata.domain.Product;
import com.product.service.checkoutkata.domain.RuleType;
import com.product.service.checkoutkata.dto.PricingRuleDto;
import com.product.service.checkoutkata.dto.ProductDto;
import com.product.service.checkoutkata.service.CatalogService;

@WebMvcTest(controllers = AdminController.class)
class AdminControllerTest {
  @Autowired MockMvc mvc;
  @MockBean CatalogService catalog;
  @Autowired ObjectMapper om;

  private static final String BASE = "/api/v1/admin";

  @Nested
  @DisplayName("GET /products")
  class ListProducts {

    @Test
    @DisplayName("should return 200 with product list")
    void returnsProducts() throws Exception {
      when(catalog.allProducts())
          .thenReturn(
              List.of(
                  new Product("A", new BigDecimal("50.00")),
                  new Product("B", new BigDecimal("30.00"))));

      mvc.perform(get(BASE + "/products"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[0].sku").value("A"))
          .andExpect(jsonPath("$[0].unitPrice").value(50.00))
          .andExpect(jsonPath("$[1].sku").value("B"));

      verify(catalog, times(1)).allProducts();
      verifyNoMoreInteractions(catalog);
    }
  }

  @Nested
  @DisplayName("POST /products")
  class UpsertProduct {

    @Test
    @DisplayName("should upsert product and return 200")
    void upsertOk() throws Exception {
      var dto = new ProductDto("A", new BigDecimal("50.00"));
      var saved = new Product("A", new BigDecimal("50.00"));

      when(catalog.upsertProduct(any(Product.class))).thenReturn(saved);

      mvc.perform(
              post(BASE + "/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(om.writeValueAsString(dto)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.sku").value("A"))
          .andExpect(jsonPath("$.unitPrice").value(50.00));

      verify(catalog).upsertProduct(new Product("A", new BigDecimal("50.00")));
      verifyNoMoreInteractions(catalog);
    }

    @ParameterizedTest(name = "[{index}] invalid sku=''{0}'' / price=''{1}'' -> 400")
    @MethodSource("invalidProductPayloads")
    void upsertValidationFails(String sku, String unitPrice) throws Exception {
      String body =
          "{\"sku\":"
              + (sku == null ? null : "\"" + sku + "\"")
              + ","
              + "\"unitPrice\":"
              + unitPrice
              + "}";

      mvc.perform(post(BASE + "/products").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(catalog);
    }

    static Stream<Arguments> invalidProductPayloads() {
      return Stream.of(
          Arguments.of("", "50.00"),
          Arguments.of("aa", "50.00"),
          Arguments.of("1", "50.00"),
          Arguments.of("A", "-1"));
    }
  }

  @Nested
  @DisplayName("POST /rules")
  class AddRule {

    @Test
    @DisplayName("should add BULK_X_FOR_Y rule and return 200")
    void addRuleOk() throws Exception {
      var dto = new PricingRuleDto("Z", RuleType.BULK_X_FOR_Y, 4, new BigDecimal("130.00"));
      var saved = new PricingRule("Z", RuleType.BULK_X_FOR_Y, 4, new BigDecimal("130.00"));

      when(catalog.addRule(any(PricingRule.class))).thenReturn(saved);

      mvc.perform(
              post(BASE + "/rules")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(om.writeValueAsString(dto)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.sku").value("Z"))
          .andExpect(jsonPath("$.ruleType").value("BULK_X_FOR_Y"))
          .andExpect(jsonPath("$.xQty").value(4))
          .andExpect(jsonPath("$.yPrice").value(130.00));

      verify(catalog)
          .addRule(
              argThat(
                  r ->
                      r.getSku().equals("Z")
                          && r.getRuleType() == RuleType.BULK_X_FOR_Y
                          && r.getXQty() == 4
                          && r.getYPrice().compareTo(new BigDecimal("130.00")) == 0));
      verifyNoMoreInteractions(catalog);
    }

    @ParameterizedTest(name = "[{index}] invalid rule payload -> 400: {0}")
    @CsvSource({
      "'{\"sku\":\"\",\"ruleType\":\"BULK_X_FOR_Y\",\"xQty\":3,\"yPrice\":130.00}'",
      "'{\"sku\":\"ab\",\"ruleType\":\"BULK_X_FOR_Y\",\"xQty\":3,\"yPrice\":130.00}'",
      "'{\"sku\":\"A\",\"ruleType\":\"BULK_X_FOR_Y\",\"xQty\":0,\"yPrice\":130.00}'",
      "'{\"sku\":\"A\",\"ruleType\":\"BULK_X_FOR_Y\",\"xQty\":-1,\"yPrice\":130.00}'",
      "'{\"sku\":\"A\",\"ruleType\":\"BULK_X_FOR_Y\",\"xQty\":3,\"yPrice\":-1}'",
    })
    void addRuleValidationFails(String rawJson) throws Exception {
      mvc.perform(post(BASE + "/rules").contentType(MediaType.APPLICATION_JSON).content(rawJson))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(catalog);
    }
  }
}
