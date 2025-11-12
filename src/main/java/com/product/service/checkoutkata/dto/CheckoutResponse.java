package com.product.service.checkoutkata.dto;

import java.math.BigDecimal;
import java.util.List;
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
        Map<String, Integer> itemCounts,
    @Schema(
            description = "List of offers applied during the pricing calculation",
            example =
                "[ { \"sku\": \"A\", \"ruleType\": \"BULK_X_FOR_Y\", \"bundlesApplied\": 1, \"bundleSize\": 3, \"bundlePrice\": 130.00, \"remainder\": 2, \"remainderPrice\": 80.00 } ]")
        List<OfferApplied> offers) {}
