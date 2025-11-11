package com.product.service.checkoutkata.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "APIError", description = "Standard error envelope")
public record APIError(@Schema(example = "Unknown SKU: Z") String error) {}
