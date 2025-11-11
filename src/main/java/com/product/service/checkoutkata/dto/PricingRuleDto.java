package com.product.service.checkoutkata.dto;

import java.math.BigDecimal;

import com.product.service.checkoutkata.domain.RuleType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record PricingRuleDto(
    @Schema(example = "A", description = "SKU the rule applies to") @Pattern(regexp = "[A-Z]")
        String sku,
    @Schema(example = "BULK_X_FOR_Y") RuleType ruleType,
    @Schema(example = "3", description = "Bundle size for BULK_X_FOR_Y") @Min(1) Integer xQty,
    @Schema(example = "130.00", description = "Bundle price for BULK_X_FOR_Y") @DecimalMin("0.00")
        BigDecimal yPrice) {}
