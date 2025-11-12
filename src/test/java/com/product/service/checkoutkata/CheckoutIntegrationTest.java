package com.product.service.checkoutkata;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
@DisplayName("Checkout pricing — integration")
class CheckoutIntegrationTest {
  private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER =
      new com.fasterxml.jackson.databind.ObjectMapper();

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  private static String body(String items) throws Exception {
    return OBJECT_MAPPER.writeValueAsString(java.util.Map.of("items", items));
  }

  @ParameterizedTest(name = "[{index}] invalid items=\"{0}\" -> 400")
  @MethodSource("invalidCases")
  void shouldRejectInvalidInputs(String items) throws Exception {
    mvc.perform(
            post("/api/v1/checkout/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(items)))
        .andExpect(status().isBadRequest());
  }

  static Stream<String> invalidCases() {
    return Stream.of(
        "", // empty
        " ", // blank
        "A1" // non-letters (violates @Pattern)
        );
  }

  @ParameterizedTest(name = "[{index}] items=\"{0}\" -> total={1}")
  @MethodSource("validCases")
  void shouldProvidePriceForValidSequences(String items, double expectedTotal) throws Exception {
    mvc.perform(
            post("/api/v1/checkout/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(items)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(expectedTotal))
        // new: response now includes offers array (may be empty)
        .andExpect(jsonPath("$.offers").isArray());
  }

  @Test
  @DisplayName("should report applied offers for bundling (AAAB)")
  void shouldReportAppliedOffersForBundleCase() throws Exception {
    mvc.perform(
            post("/api/v1/checkout/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("AAAB")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").exists())
        .andExpect(jsonPath("$.offers").isArray())
        .andExpect(jsonPath("$.offers.length()").value(greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.offers[0].sku").value("A"))
        .andExpect(jsonPath("$.offers[0].bundlesApplied").value(1))
        .andExpect(jsonPath("$.offers[0].bundleSize").value(3))
        .andExpect(jsonPath("$.offers[0].bundlePrice").value(130.00));
  }

  static Stream<org.junit.jupiter.params.provider.Arguments> validCases() {
    return Stream.of(
        Arguments.of("A", 50.00),
        Arguments.of("B", 30.00),
        Arguments.of("AB", 80.00),
        Arguments.of("CDBA", 115.00),
        Arguments.of("AA", 100.00),
        Arguments.of("AAA", 130.00),
        Arguments.of("AAAA", 180.00),
        Arguments.of("AAAAA", 230.00),
        Arguments.of("AAAAAA", 260.00),
        Arguments.of("AAABB", 175.00),
        Arguments.of("AAABBD", 190.00),
        Arguments.of("DABABA", 190.00),
        Arguments.of("BAB", 95.00) // requires B's 2-for-45 rule in seed data
        );
  }
}
