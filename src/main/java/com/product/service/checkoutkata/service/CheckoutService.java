package com.product.service.checkoutkata.service;

import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.product.service.checkoutkata.domain.Product;
import com.product.service.checkoutkata.dto.OfferApplied;
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
    return priceOfWithDetails(itemSequence).total();
  }

  @Transactional(readOnly = true)
  public PricingResult priceOfWithDetails(String itemSequence) {
    LOGGER.debug("Calculating price (with details) for items: {}", itemSequence);
    if (itemSequence == null || itemSequence.isBlank()) {
      LOGGER.error("No item sequence found");
      return new PricingResult(BigDecimal.ZERO.setScale(2), List.of());
    }
    Map<String, Integer> counts = new HashMap<>();
    for (char c : itemSequence.toCharArray()) {
      String sku = String.valueOf(Character.toUpperCase(c));
      if (!sku.matches("[A-Z]")) continue;
      counts.merge(sku, 1, Integer::sum);
    }

    BigDecimal total = BigDecimal.ZERO.setScale(2);
    List<OfferApplied> overallOffers = new ArrayList<>();

    for (Map.Entry<String, Integer> e : counts.entrySet()) {
      Product p =
          products
              .findBySku(e.getKey())
              .orElseThrow(() -> new NoSuchElementException("Unknown SKU: " + e.getKey()));
      PricingResult res =
          engine.priceForWithDetails(e.getValue(), p.getUnitPrice(), rules.findBySku(p.getSku()));
      total = total.add(res.total());
      overallOffers.addAll(res.offers());
    }

    return new PricingResult(total.setScale(2), overallOffers);
  }
}
