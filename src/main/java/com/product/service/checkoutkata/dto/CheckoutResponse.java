package com.product.service.checkoutkata.dto;

import java.math.BigDecimal;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckoutResponse(
    @Schema(
            description = "Final total price after applying unit prices and SKU-specific offers",
            example = "115.00")
        BigDecimal total,
    @Schema(
            description = "Map of SKU → quantity purchased. Derived from the input string.",
            example = "{ \"A\": 2, \"B\": 1 }")
        Map<String, Integer> itemCounts) {}
