package com.product.service.checkoutkata.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.product.service.checkoutkata.domain.APIError;
import com.product.service.checkoutkata.dto.CheckoutRequest;
import com.product.service.checkoutkata.dto.CheckoutResponse;
import com.product.service.checkoutkata.service.CheckoutService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Checkout", description = "Price calculation APIs")
@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutController.class);
  private final CheckoutService checkout;

  public CheckoutController(CheckoutService checkout) {
    this.checkout = checkout;
  }

  @Operation(
      summary = "Calculate total price",
      description =
          "Computes the price of a sequence of SKUs using per-SKU rules. Non-letter characters are ignored.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = CheckoutRequest.class),
                      examples = {
                        @ExampleObject(name = "Simple", value = "{\"items\":\"CDBA\"}"),
                        @ExampleObject(name = "Bundle eligible", value = "{\"items\":\"AAAB\"}")
                      })))
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = CheckoutResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content = @Content(schema = @Schema(implementation = APIError.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Unknown SKU",
      content = @Content(schema = @Schema(implementation = APIError.class)))
  @PostMapping("/price")
  public ResponseEntity<CheckoutResponse> price(@Valid @RequestBody CheckoutRequest req) {
    final String raw = (req.items() == null) ? "" : req.items().trim();
    LOGGER.info("Received pricing request");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Raw items payload: '{}'", raw);
    }
    BigDecimal total = checkout.priceOf(req.items());
    Map<String, Integer> counts = new HashMap<>();
    for (char c : req.items().toCharArray()) {
      String s = String.valueOf(Character.toUpperCase(c));
      if (s.matches("[A-Z]")) counts.merge(s, 1, Integer::sum);
    }
    LOGGER.debug("Item counts computed: {}", counts);
    LOGGER.info("Pricing completed. Total={}", total);
    return ResponseEntity.ok(new CheckoutResponse(total, counts));
  }
}
