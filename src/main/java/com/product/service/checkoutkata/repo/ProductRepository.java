package com.product.service.checkoutkata.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.product.service.checkoutkata.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Optional<Product> findBySku(String sku);
}
