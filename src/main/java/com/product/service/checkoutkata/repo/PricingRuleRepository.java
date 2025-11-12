package com.product.service.checkoutkata.repo;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.product.service.checkoutkata.domain.PricingRule;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
  List<PricingRule> findBySku(String sku);

  @Query(
      """
    SELECT r FROM PricingRule r
    WHERE r.sku = :sku
      AND (r.startsAt IS NULL OR r.startsAt <= :now)
      AND (r.endsAt IS NULL OR r.endsAt >= :now)
    ORDER BY r.id
  """)
  List<PricingRule> findActiveBySkuAt(@Param("sku") String sku, @Param("now") OffsetDateTime now);
}
