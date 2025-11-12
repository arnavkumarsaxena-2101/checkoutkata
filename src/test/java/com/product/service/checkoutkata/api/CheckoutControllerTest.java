package com.product.service.checkoutkata.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.service.checkoutkata.service.CheckoutService;
import com.product.service.checkoutkata.service.PricingResult;

@WebMvcTest(controllers = CheckoutController.class)
public class CheckoutControllerTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;
  @MockBean CheckoutService checkoutService;

  private static String body(String items) throws Exception {
    return new ObjectMapper().writeValueAsString(Map.of("items", items));
  }

  @Nested
  @DisplayName("POST /api/v1/checkout/price — happy paths")
  class HappyPaths {
    @ParameterizedTest(name = "[{index}] items=\"{0}\" -> total={1}, counts={2}")
    @MethodSource("com.product.service.checkoutkata.api.CheckoutControllerTest#happyCases")
    void shouldComputeTotalsAndCounts(
        String items, BigDecimal stubTotal, Map<String, Integer> expectedCounts) throws Exception {

      // stub service to return total + empty offers
      when(checkoutService.priceOfWithDetails(items))
          .thenReturn(new PricingResult(stubTotal, List.of()));

      mvc.perform(
              post("/api/v1/checkout/price")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body(items)))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.total").value(stubTotal.doubleValue()))
          // Assert itemCounts entries
          .andExpectAll(
              expectedCounts.entrySet().stream()
                  .map(e -> jsonPath("$.itemCounts." + e.getKey()).value(e.getValue()))
                  .toArray(org.springframework.test.web.servlet.ResultMatcher[]::new));

      verify(checkoutService, times(1)).priceOfWithDetails(items);
      verifyNoMoreInteractions(checkoutService);
    }
  }

  static Stream<Arguments> happyCases() {
    return Stream.of(
        Arguments.of("A", new BigDecimal("50.00"), Map.of("A", 1)),
        Arguments.of("CDBA", new BigDecimal("115.00"), Map.of("A", 1, "B", 1, "C", 1, "D", 1)),
        Arguments.of("AAAB", new BigDecimal("175.00"), Map.of("A", 3, "B", 1)));
  }

  @Test
  @DisplayName("should be case-insensitive and ignore non-letters in counts")
  void shouldHandleCaseAndNonLetters() throws Exception {
    String items = "abA";
    BigDecimal stubTotal = new BigDecimal("95.00");

    // stub service: total + no offers
    when(checkoutService.priceOfWithDetails(items))
        .thenReturn(new PricingResult(stubTotal, List.of()));

    mvc.perform(
            post("/api/v1/checkout/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(items)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(95.00))
        .andExpect(jsonPath("$.itemCounts.A").value(2))
        .andExpect(jsonPath("$.itemCounts.B").value(1))
        .andExpect(jsonPath("$.itemCounts").isMap())
        .andExpect(jsonPath("$.offers").isArray()); // should be present (possibly empty)

    verify(checkoutService).priceOfWithDetails(items);
    verifyNoMoreInteractions(checkoutService);
  }

  @Nested
  @DisplayName("POST /api/v1/checkout/price — invalid payloads")
  class InvalidPayloads {
    @ParameterizedTest(name = "[{index}] invalid body -> 400: {0}")
    @MethodSource("com.product.service.checkoutkata.api.CheckoutControllerTest#invalidBodies")
    void should400OnInvalidPayload(String rawJson) throws Exception {
      mvc.perform(
              post("/api/v1/checkout/price")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(rawJson))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(checkoutService);
    }

    @Test
    @DisplayName("missing body -> 400")
    void missingBody() throws Exception {
      mvc.perform(post("/api/v1/checkout/price").contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(checkoutService);
    }
  }

  static Stream<String> invalidBodies() {
    return Stream.of("{}", "{\"items\":\"\"}", "{\"items\":\"A1\"}");
  }
}
