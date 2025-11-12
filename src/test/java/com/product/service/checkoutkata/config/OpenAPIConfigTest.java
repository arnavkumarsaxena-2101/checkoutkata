package com.product.service.checkoutkata.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

class OpenAPIConfigTest {
  @Test
  @DisplayName("checkoutApi() should use local server when springdoc.server-url is blank")
  void localServerWhenNoOverride() {
    OpenAPIConfig cfg = new OpenAPIConfig();
    cfg.serverUrl = "";

    OpenAPI api = cfg.checkoutApi();

    assertThat(api.getServers()).hasSize(1);

    Server server = api.getServers().get(0);
    assertThat(server.getUrl()).isEqualTo("http://localhost:8080");
    assertThat(server.getDescription()).isEqualTo("Local");

    assertThat(api.getInfo().getTitle()).isEqualTo("Checkout Service API");
    assertThat(api.getInfo().getVersion()).isEqualTo("v1");
  }

  @Test
  @DisplayName("checkoutApi() should use provided serverUrl when configured")
  void deployedServerWhenOverrideProvided() {
    OpenAPIConfig cfg = new OpenAPIConfig();
    cfg.serverUrl = "https://unit-test.com";

    OpenAPI api = cfg.checkoutApi();

    assertThat(api.getServers()).hasSize(1);
    Server server = api.getServers().get(0);

    assertThat(server.getUrl()).isEqualTo("https://unit-test.com");
    assertThat(server.getDescription()).isEqualTo("Deployed");
  }
}
