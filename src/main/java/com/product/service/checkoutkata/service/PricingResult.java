package com.product.service.checkoutkata.service;

import java.math.BigDecimal;
import java.util.List;

import com.product.service.checkoutkata.dto.OfferApplied;

public record PricingResult(BigDecimal total, List<OfferApplied> offers) {}
