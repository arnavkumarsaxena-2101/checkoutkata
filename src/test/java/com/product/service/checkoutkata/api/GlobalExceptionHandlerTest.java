package com.product.service.checkoutkata.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.validation.Valid;

import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GlobalExceptionHandlerTest {
    private final ObjectMapper om = new ObjectMapper();

    @RestController
    @RequestMapping("/stub")
    @EnableWebMvc
    static class StubController {
        @GetMapping("/missing")
        public String missing() {
            throw new NoSuchElementException("Unknown SKU: Z");
        }

        record Payload(
                @NotBlank(message = "items is required")
                String items
        ) {}

        @PostMapping("/validate")
        public String validate(@Valid @RequestBody Payload payload, WebRequest req) {
            return "OK";
        }
    }

    private MockMvc mvc() {
        return standaloneSetup(new StubController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("NoSuchElementException -> 404 with error message")
    void notFoundMappedTo404() throws Exception {
        mvc().perform(get("/stub/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", equalTo("Unknown SKU: Z")));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException -> 400 with first error message")
    void validationMappedTo400() throws Exception {
        var badJson = om.writeValueAsString(new StubController.Payload(null));

        mvc().perform(post("/stub/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", containsString("items is required")));
    }

    @Test
    @DisplayName("Valid payload passes through (sanity check that advice doesn't interfere)")
    void validPayloadPasses() throws Exception {
        var goodJson = om.writeValueAsString(new StubController.Payload("ABC"));

        mvc().perform(post("/stub/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goodJson))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
