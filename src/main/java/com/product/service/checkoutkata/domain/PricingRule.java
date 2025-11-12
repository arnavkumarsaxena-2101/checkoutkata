package com.product.service.checkoutkata.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(
    name = "pricing_rules",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_rule_sku_type",
            columnNames = {"sku", "rule_type"}))
@ToString
@Getter
public class PricingRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonProperty("id")
  private Long id;

  @Column(nullable = false, length = 1)
  @JsonProperty("sku")
  private String sku;

  @Enumerated(EnumType.STRING)
  @Column(name = "rule_type", nullable = false)
  @JsonProperty("ruleType")
  private RuleType ruleType;

  @Column(name = "x_qty")
  @JsonProperty("xQty")
  private Integer xQty;

  @Column(name = "y_price", precision = 12, scale = 2)
  @JsonProperty("yPrice")
  private BigDecimal yPrice;

  @Column(name = "starts_at")
  private OffsetDateTime startsAt;

  @Column(name = "ends_at")
  private OffsetDateTime endsAt;

  protected PricingRule() {}

  public PricingRule(
      String sku,
      RuleType type,
      Integer xQty,
      BigDecimal yPrice,
      OffsetDateTime startsAt,
      OffsetDateTime endsAt) {
    this.sku = sku;
    this.ruleType = type;
    this.xQty = xQty;
    this.yPrice = yPrice;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
  }

  public boolean isActiveAt(OffsetDateTime at) {
    if (at == null) at = OffsetDateTime.now();
    boolean afterStart = (startsAt == null) || !at.isBefore(startsAt);
    boolean beforeEnd = (endsAt == null) || !at.isAfter(endsAt);
    return afterStart && beforeEnd;
  }
}
