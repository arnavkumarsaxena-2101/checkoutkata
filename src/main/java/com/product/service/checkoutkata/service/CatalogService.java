package com.product.service.checkoutkata.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.product.service.checkoutkata.domain.PricingRule;
import com.product.service.checkoutkata.domain.Product;
import com.product.service.checkoutkata.repo.PricingRuleRepository;
import com.product.service.checkoutkata.repo.ProductRepository;

@Service
public class CatalogService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogService.class);

  private final ProductRepository products;
  private final PricingRuleRepository rules;

  public CatalogService(ProductRepository products, PricingRuleRepository rules) {
    this.products = products;
    this.rules = rules;
  }

  public List<Product> allProducts() {
    return products.findAll();
  }

  public List<PricingRule> rulesFor(String sku) {
    return rules.findBySku(sku);
  }

  @Transactional
  public Product upsertProduct(Product p) {
    LOGGER.debug("Updating product {}", p);
    return products
        .findBySku(p.getSku())
        .map(
            ex -> {
              LOGGER.debug("Found SKU {}", ex);
              ex.setUnitPrice(p.getUnitPrice());
              return ex;
            })
        .orElseGet(
            () -> {
              LOGGER.debug("Creating new product {}", p);
              return products.save(p);
            });
  }

  @Transactional
  public PricingRule addRule(PricingRule r) {
    return rules.save(r);
  }
}
