package com.product.service.checkoutkata.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.product.service.checkoutkata.domain.PricingRule;
import com.product.service.checkoutkata.domain.RuleType;

class PricingEngineTest {
  private final PricingEngine engine = new PricingEngine();

  private static PricingRule bulk(int x, String y) {
    return new PricingRule(
        "A",
        RuleType.BULK_X_FOR_Y,
        x,
        new BigDecimal(y),
        OffsetDateTime.now().minusDays(1),
        OffsetDateTime.now().plusDays(1));
  }

  @Nested
  @DisplayName("qty <= 0")
  class ZeroOrNegative {
    @Test
    void zeroQty_isZero() {
      var total = engine.priceFor(0, new BigDecimal("50.00"), List.of());
      assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    void negativeQty_isZero() {
      var total = engine.priceFor(-3, new BigDecimal("50.00"), List.of());
      assertThat(total).isEqualByComparingTo("0.00");
    }
  }

  @Nested
  @DisplayName("No rules")
  class NoRules {
    @Test
    void qty1_noRules_simpleUnitPrice() {
      var total = engine.priceFor(1, new BigDecimal("50.00"), List.of());
      assertThat(total).isEqualByComparingTo("50.00");
    }

    @Test
    void qtyMany_noRules_multiplies() {
      var total = engine.priceFor(4, new BigDecimal("50.00"), List.of());
      assertThat(total).isEqualByComparingTo("200.00");
    }
  }

  @Nested
  @DisplayName("Single BULK_X_FOR_Y rule")
  class SingleBundle {
    @Test
    void exactBundle_appliesOnce() {
      // 3 for 130; qty=3; unit=50 -> 130
      var total = engine.priceFor(3, new BigDecimal("50.00"), List.of(bulk(3, "130.00")));
      assertThat(total).isEqualByComparingTo("130.00");
    }

    @Test
    void multipleBundles_appliesMany() {
      // 3 for 130; qty=6 -> 260
      var total = engine.priceFor(6, new BigDecimal("50.00"), List.of(bulk(3, "130.00")));
      assertThat(total).isEqualByComparingTo("260.00");
    }

    @Test
    void bundlePlusRemainder() {
      // 3 for 130; qty=5 -> 130 + 2*50 = 230
      var total = engine.priceFor(5, new BigDecimal("50.00"), List.of(bulk(3, "130.00")));
      assertThat(total).isEqualByComparingTo("230.00");
    }

    @Test
    void decimalPrices_andScale() {
      // 2 for 30; unit=19.99; qty=3 -> 30 + 19.99 = 49.99
      var total = engine.priceFor(3, new BigDecimal("19.99"), List.of(bulk(2, "30.00")));
      assertThat(total).isEqualByComparingTo("49.99");
      assertThat(total.scale()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("Invalid/partial rules (ignored)")
  class InvalidRules {
    @Test
    void nullXQty_isIgnored() {
      var bad =
          new PricingRule(
              "A",
              RuleType.BULK_X_FOR_Y,
              null,
              new BigDecimal("130.00"),
              OffsetDateTime.now().minusDays(1),
              OffsetDateTime.now().plusDays(1));
      var total = engine.priceFor(3, new BigDecimal("50.00"), List.of(bad));
      // no bundle applied -> 3 * 50
      assertThat(total).isEqualByComparingTo("150.00");
    }

    @Test
    void nullYPrice_isIgnored() {
      var bad =
          new PricingRule(
              "A",
              RuleType.BULK_X_FOR_Y,
              3,
              null,
              OffsetDateTime.now().minusDays(1),
              OffsetDateTime.now().plusDays(1));
      var total = engine.priceFor(3, new BigDecimal("50.00"), List.of(bad));
      assertThat(total).isEqualByComparingTo("150.00");
    }
  }

  @Nested
  @DisplayName("Multiple rules (order matters)")
  class MultipleRules {
    @Test
    void appliesRulesInGivenOrder() {
      // Rules: first 5-for-200, then 3-for-130; qty=8, unit=50
      // Apply 5-for-200 -> 1 bundle, remaining 3
      // Then 3-for-130 -> 1 bundle, remaining 0
      // Total = 200 + 130 = 330
      var r1 = bulk(5, "200.00");
      var r2 = bulk(3, "130.00");

      var total = engine.priceFor(8, new BigDecimal("50.00"), List.of(r1, r2));
      assertThat(total).isEqualByComparingTo("330.00");
    }

    @Test
    void reversedOrderYieldsDifferentResult() {
      // Reverse: first 3-for-130, then 5-for-200; qty=8, unit=50
      // 3-for-130 -> bundles=2 (6 items) => 260, remaining 2
      // 5-for-200 can't apply to remaining 2 -> + 2 * 50 = 100
      // Total = 360
      var r1 = bulk(3, "130.00");
      var r2 = bulk(5, "200.00");

      var total = engine.priceFor(8, new BigDecimal("50.00"), List.of(r1, r2));
      assertThat(total).isEqualByComparingTo("360.00");
    }
  }
}
