package com.product.service.checkoutkata.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true, description = "Pricing rule type.")
public enum RuleType {
  BULK_X_FOR_Y
}
