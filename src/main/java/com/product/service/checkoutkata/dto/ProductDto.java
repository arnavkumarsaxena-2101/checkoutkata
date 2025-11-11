package com.product.service.checkoutkata.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record ProductDto(
    @Schema(example = "A", description = "Single-letter SKU") @Pattern(regexp = "[A-Z]") String sku,
    @Schema(example = "50.00", description = "Unit price (scale=2)") @DecimalMin("0.00")
        BigDecimal unitPrice) {}
