package com.product.service.checkoutkata.config;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {
  @Value("${springdoc.server-url:}")
  String serverUrl;

  @Bean
  public OpenAPI checkoutApi() {
    OpenAPI api =
        new OpenAPI()
            .info(
                new Info()
                    .title("Checkout Service API")
                    .version("v1")
                    .description(
                        "APIs for pricing items at checkout with per-SKU promotional rules.")
                    .contact(new Contact().name("Kumar Arnav").email("arnavkumarsaxena@live.com"))
                    .license(new License().name("Not Licensed").url("http://unlicense.org")));
    if (serverUrl != null && !serverUrl.isBlank()) {
      api.setServers(List.of(new Server().url(serverUrl).description("Deployed")));
    } else {
      api.setServers(List.of(new Server().url("http://localhost:8080").description("Local")));
    }
    return api;
  }

  @Bean
  GroupedOpenApi checkoutGroup() {
    return GroupedOpenApi.builder()
        .group("checkout")
        .packagesToScan("com.product.service.checkoutkata.api")
        .build();
  }

  @Bean
  GroupedOpenApi adminGroup() {
    return GroupedOpenApi.builder()
        .group("admin")
        .packagesToScan("com.product.service.checkoutkata.api.admin")
        .build();
  }
}
