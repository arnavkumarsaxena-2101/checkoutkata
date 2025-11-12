package com.product.service.checkoutkata.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.product.service.checkoutkata.domain.PricingRule;
import com.product.service.checkoutkata.domain.RuleType;
import com.product.service.checkoutkata.dto.OfferApplied;

public class PricingEngine {
  private static final Logger LOGGER = LoggerFactory.getLogger(PricingEngine.class);

  public BigDecimal priceFor(int qty, BigDecimal unitPrice, List<PricingRule> rules) {
    return priceForWithDetails(qty, unitPrice, rules).total();
  }

  public PricingResult priceForWithDetails(int qty, BigDecimal unitPrice, List<PricingRule> rules) {
    if (qty <= 0) {
      LOGGER.error("Quantity must be greater than zero");
      return new PricingResult(BigDecimal.ZERO.setScale(2), List.of());
    }
    int remaining = qty;
    BigDecimal total = BigDecimal.ZERO.setScale(2);
    List<OfferApplied> applied = new ArrayList<>();

    for (PricingRule r : rules) {
      if (r.getRuleType() == RuleType.BULK_X_FOR_Y
          && r.getXQty() != null
          && r.getYPrice() != null) {
        LOGGER.info("Applying Bundling for SKU={} rule={}", r.getSku(), r);
        int bundles = remaining / r.getXQty();
        if (bundles > 0) {
          BigDecimal bundlesTotal = r.getYPrice().multiply(BigDecimal.valueOf(bundles));
          total = total.add(bundlesTotal);
          int beforeRemainder = remaining % r.getXQty();
          int remainderAfter = remaining % r.getXQty();
          // compute remainderPrice (will be charged as unitPrice * remainderAfter)
          BigDecimal remainderPrice = unitPrice.multiply(BigDecimal.valueOf(remainderAfter));
          applied.add(
              new OfferApplied(
                  r.getSku(),
                  r.getRuleType().name(),
                  bundles,
                  r.getXQty(),
                  r.getYPrice(),
                  remainderAfter,
                  remainderPrice.setScale(2)));
          remaining = remaining % r.getXQty();
        }
      }
      // future rule types can be added here and add OfferApplied entries
    }

    // Charge for leftover units at unit price
    BigDecimal remainderCharge = unitPrice.multiply(BigDecimal.valueOf(remaining));
    total = total.add(remainderCharge);
    LOGGER.info("Total price after bundling: {}, quantity: {}", total, qty);
    return new PricingResult(total.setScale(2), applied);
  }
}
