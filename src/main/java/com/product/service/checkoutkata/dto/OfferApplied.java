package com.product.service.checkoutkata.dto;

import java.math.BigDecimal;

public record OfferApplied(
    String sku,
    String ruleType,
    int bundlesApplied,
    int bundleSize,
    BigDecimal bundlePrice,
    int remainder,
    BigDecimal remainderPrice) {}
