package com.product.service.checkoutkata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {
  @Bean
  public OpenAPI checkoutApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Checkout Service API")
                .version("v1")
                .description("APIs for pricing items at checkout with per-SKU promotional rules.")
                .contact(new Contact().name("Kumar Arnav").email("arnavkumarsaxena@live.com"))
                .license(new License().name("Not Licensed").url("http://unlicense.org")))
        .addServersItem(new Server().url("http://localhost:8080").description("Local"));
  }
}
