package com.product.service.checkoutkata.service;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class RequestIdFilterTest {
  private final RequestIdFilter filter = new RequestIdFilter();

  @Test
  @DisplayName("Uses X-Request-Id header when present and sets method/path")
  void usesHeaderWhenPresent() throws Exception {
    var req = new MockHttpServletRequest("GET", "/ping");
    var res = new MockHttpServletResponse();
    req.addHeader("X-Request-Id", "abc-123");

    var seenRequestId = new AtomicReference<String>();
    var seenMethod = new AtomicReference<String>();
    var seenPath = new AtomicReference<String>();

    FilterChain chain =
        (request, response) -> {
          seenRequestId.set(MDC.get("requestId"));
          seenMethod.set(MDC.get("method"));
          seenPath.set(MDC.get("path"));
        };

    filter.doFilter(req, res, chain);

    assertThat(seenRequestId.get()).isEqualTo("abc-123");
    assertThat(seenMethod.get()).isEqualTo("GET");
    assertThat(seenPath.get()).isEqualTo("/ping");

    assertThat(MDC.get("requestId")).isNull();
    assertThat(MDC.get("method")).isNull();
    assertThat(MDC.get("path")).isNull();
  }

  @Test
  @DisplayName("Generates UUID when header missing, sets method/path, and clears afterward")
  void generatesWhenMissing() throws Exception {
    var req = new MockHttpServletRequest("POST", "/api/v1/checkout/price");
    var res = new MockHttpServletResponse();

    var seenRequestId = new AtomicReference<String>();
    var seenMethod = new AtomicReference<String>();
    var seenPath = new AtomicReference<String>();

    FilterChain chain =
        (request, response) -> {
          seenRequestId.set(MDC.get("requestId"));
          seenMethod.set(MDC.get("method"));
          seenPath.set(MDC.get("path"));
        };

    filter.doFilter(req, res, chain);

    assertThat(seenRequestId.get()).isNotBlank();
    assertThatCode(() -> UUID.fromString(seenRequestId.get())).doesNotThrowAnyException();

    assertThat(seenMethod.get()).isEqualTo("POST");
    assertThat(seenPath.get()).isEqualTo("/api/v1/checkout/price");

    assertThat(MDC.get("requestId")).isNull();
    assertThat(MDC.get("method")).isNull();
    assertThat(MDC.get("path")).isNull();
  }

  @Test
  @DisplayName("Clears MDC even if downstream throws")
  void clearsOnException() {
    var req = new MockHttpServletRequest("DELETE", "/danger");
    var res = new MockHttpServletResponse();

    FilterChain throwingChain =
        (request, response) -> {
          assertThat(MDC.get("requestId")).isNotBlank();
          assertThat(MDC.get("method")).isEqualTo("DELETE");
          assertThat(MDC.get("path")).isEqualTo("/danger");
          throw new ServletException("boom");
        };

    assertThatThrownBy(() -> filter.doFilter(req, res, throwingChain))
        .isInstanceOf(ServletException.class)
        .hasMessage("boom");

    assertThat(MDC.get("requestId")).isNull();
    assertThat(MDC.get("method")).isNull();
    assertThat(MDC.get("path")).isNull();
  }
}
