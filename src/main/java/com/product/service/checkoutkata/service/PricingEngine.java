package com.product.service.checkoutkata.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.product.service.checkoutkata.domain.PricingRule;
import com.product.service.checkoutkata.domain.RuleType;

public class PricingEngine {
  private static final Logger LOGGER = LoggerFactory.getLogger(PricingEngine.class);

  public BigDecimal priceFor(int qty, BigDecimal unitPrice, List<PricingRule> rules) {
    if (qty <= 0) {
      LOGGER.error("Quantity must be greater than zero");
      return BigDecimal.ZERO.setScale(2);
    }
    int remaining = qty;
    BigDecimal total = BigDecimal.ZERO.setScale(2);

    for (PricingRule r : rules) {
      if (r.getRuleType() == RuleType.BULK_X_FOR_Y
          && r.getXQty() != null
          && r.getYPrice() != null) {
        LOGGER.info("Applying Bundling");
        int bundles = remaining / r.getXQty();
        if (bundles > 0) {
          total = total.add(r.getYPrice().multiply(BigDecimal.valueOf(bundles)));
          remaining = remaining % r.getXQty();
        }
      }
    }
    total = total.add(unitPrice.multiply(BigDecimal.valueOf(remaining)));
    LOGGER.info("Total price after bundling: {}, quantity: {}", total, qty);
    return total.setScale(2);
  }
}
