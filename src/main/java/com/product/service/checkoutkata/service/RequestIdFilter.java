package com.product.service.checkoutkata.service;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

@Component
public class RequestIdFilter implements Filter {
  private static final String REQ_ID = "requestId";

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest http = (HttpServletRequest) req;
    String rid = headerOrGenerate(http, "X-Request-Id");
    MDC.put(REQ_ID, rid);
    MDC.put("method", http.getMethod());
    MDC.put("path", http.getRequestURI());
    try {
      chain.doFilter(req, res);
    } finally {
      MDC.clear();
    }
  }

  private String headerOrGenerate(HttpServletRequest http, String name) {
    String v = http.getHeader(name);
    return (v == null || v.isBlank()) ? UUID.randomUUID().toString() : v;
  }
}
