package com.product.service.checkoutkata.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.product.service.checkoutkata.domain.Product;
import com.product.service.checkoutkata.repo.PricingRuleRepository;
import com.product.service.checkoutkata.repo.ProductRepository;

@Service
public class CheckoutService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutService.class);
  private final ProductRepository products;
  private final PricingRuleRepository rules;
  private final PricingEngine engine;

  public CheckoutService(ProductRepository products, PricingRuleRepository rules) {
    this.products = products;
    this.rules = rules;
    this.engine = new PricingEngine();
  }

  @Transactional(readOnly = true)
  public BigDecimal priceOf(String itemSequence) {
    LOGGER.debug("Calculating price for items: {}", itemSequence);
    if (itemSequence == null || itemSequence.isBlank()) {
      LOGGER.error("No item sequence found");
      return BigDecimal.ZERO.setScale(2);
    }
    Map<String, Integer> counts = new HashMap<>();
    for (char c : itemSequence.toCharArray()) {
      String sku = String.valueOf(Character.toUpperCase(c));
      LOGGER.info("Fetching SKU: {}", sku);
      if (!sku.matches("[A-Z]")) continue;
      counts.merge(sku, 1, Integer::sum);
    }
    BigDecimal total = BigDecimal.ZERO.setScale(2);
    for (Map.Entry<String, Integer> e : counts.entrySet()) {
      Product p =
          products
              .findBySku(e.getKey())
              .orElseThrow(
                  () -> {
                    LOGGER.error("No product found for SKU: {}", e.getKey());
                    return new NoSuchElementException("Unknown SKU: " + e.getKey());
                  });
      total =
          total.add(engine.priceFor(e.getValue(), p.getUnitPrice(), rules.findBySku(p.getSku())));
    }
    LOGGER.info("Total price: {} for SKU: {}", total, itemSequence);
    return total.setScale(2);
  }
}
