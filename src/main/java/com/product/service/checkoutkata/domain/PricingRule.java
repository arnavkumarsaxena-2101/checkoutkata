package com.product.service.checkoutkata.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(
    name = "pricing_rules",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_rule_sku_type",
            columnNames = {"sku", "rule_type"}))
@ToString
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

  protected PricingRule() {}

  public PricingRule(String sku, RuleType type, Integer xQty, BigDecimal yPrice) {
    this.sku = sku;
    this.ruleType = type;
    this.xQty = xQty;
    this.yPrice = yPrice;
  }

  public Long getId() {
    return id;
  }

  public String getSku() {
    return sku;
  }

  public RuleType getRuleType() {
    return ruleType;
  }

  public Integer getXQty() {
    return xQty;
  }

  public BigDecimal getYPrice() {
    return yPrice;
  }
}
