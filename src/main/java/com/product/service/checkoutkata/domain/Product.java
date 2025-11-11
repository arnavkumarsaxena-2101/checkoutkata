package com.product.service.checkoutkata.domain;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name = "products")
@ToString
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 1)
  private String sku;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  protected Product() {}

  public Product(String sku, BigDecimal unitPrice) {
    this.sku = sku;
    this.unitPrice = unitPrice;
  }

  public Long getId() {
    return id;
  }

  public String getSku() {
    return sku;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal p) {
    this.unitPrice = p;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Product p && Objects.equals(sku, p.sku);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sku);
  }
}
