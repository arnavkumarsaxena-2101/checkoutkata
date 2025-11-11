package com.product.service.checkoutkata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Characters represent SKUs; case-insensitive, non-letters ignored.")
public record CheckoutRequest(
    @JsonProperty("items")
        @NotBlank(message = "items is required")
        @Schema(example = "CDBA", description = "Sequence of item SKUs")
        @Pattern(regexp = "^[A-Za-z]*$", message = "Only letters allowed")
        String items) {}
