package com.product.service.checkoutkata.api;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.product.service.checkoutkata.domain.PricingRule;
import com.product.service.checkoutkata.domain.Product;
import com.product.service.checkoutkata.dto.PricingRuleDto;
import com.product.service.checkoutkata.dto.ProductDto;
import com.product.service.checkoutkata.service.CatalogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Admin", description = "Catalog & pricing rule administration")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
  private final CatalogService catalog;

  public AdminController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @Operation(summary = "List all products")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class))))
  @GetMapping("/products")
  public List<Product> products() {
    LOGGER.info("Fetching all products");
    List<Product> products = catalog.allProducts();
    LOGGER.debug("Fetched products: {}", products);
    return products;
  }

  @Operation(
      summary = "Upsert a product",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = ProductDto.class),
                      examples = @ExampleObject(value = "{\"sku\":\"A\",\"unitPrice\":50.00}"))))
  @ApiResponse(
      responseCode = "200",
      description = "Upserted product",
      content = @Content(schema = @Schema(implementation = Product.class)))
  @PostMapping("/products")
  public Product upsertProduct(@RequestBody @Valid ProductDto dto) {
    LOGGER.info("Upserting product with SKU: {}", dto.sku());
    Product product = catalog.upsertProduct(new Product(dto.sku(), dto.unitPrice()));
    LOGGER.debug("Upserted product: {}", product);
    return product;
  }

  @Operation(
      summary = "Add a pricing rule",
      description = "Adds a per-SKU rule (e.g., BULK_X_FOR_Y). Only applies to that SKU.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = PricingRuleDto.class),
                      examples =
                          @ExampleObject(
                              value =
                                  "{\"sku\":\"A\","
                                      + "\"ruleType\":\"BULK_X_FOR_Y\","
                                      + "\"xQty\":3,"
                                      + "\"yPrice\":130.00,"
                                      + "\"startsAt\":\"2025-11-12T00:00:00Z\","
                                      + "\"endsAt\":\"2025-12-31T23:59:59Z\"}"))))
  @PostMapping("/rules")
  public PricingRule addRule(@RequestBody @Valid PricingRuleDto dto) {
    LOGGER.info("Adding pricing rule for SKU: {}", dto.sku());
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    OffsetDateTime starts = (dto.startsAt() == null) ? now : dto.startsAt();
    PricingRule rule =
        catalog.addRule(
            new PricingRule(
                dto.sku(), dto.ruleType(), dto.xQty(), dto.yPrice(), starts, dto.endsAt()));
    LOGGER.debug("Added pricing rule: {}", rule);
    return rule;
  }

  @Operation(
      summary = "List all pricing rules",
      description = "Returns all pricing rules across SKUs (for administration and UI).")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = PricingRule.class))))
  @GetMapping("/rules")
  public List<PricingRule> rules() {
    LOGGER.info("Fetching all pricing rules");
    List<PricingRule> all = catalog.allRules();
    LOGGER.debug("Fetched rules: {}", all);
    return all;
  }
}
