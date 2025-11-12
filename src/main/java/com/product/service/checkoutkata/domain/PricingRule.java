package com.product.service.checkoutkata.domain;

import java.math.BigDecimal;

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
  private Integer xQty;

  @Column(name = "y_price", precision = 12, scale = 2)
  private BigDecimal yPrice;

  protected PricingRule() {}

  public PricingRule(String sku, RuleType type, Integer xQty, BigDecimal yPrice) {
    this.sku = sku;
    this.ruleType = type;
    this.xQty = xQty;
    this.yPrice = yPrice;
  }
}
