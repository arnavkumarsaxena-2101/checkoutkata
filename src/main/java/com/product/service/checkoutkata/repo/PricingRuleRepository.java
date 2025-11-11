package com.product.service.checkoutkata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.product.service.checkoutkata.domain.PricingRule;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
  List<PricingRule> findBySku(String sku);
}
