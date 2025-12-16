package com.example.payment.infrastructure.controller;

import com.example.payment.application.PaymentService;
import com.example.payment.application.PaymentService.PaymentResult;
import com.example.payment.infrastructure.client.dto.AccountDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Nested
    @DisplayName("GET /api/v1/payments/accounts/{accountNumber}")
    class GetAccount {

        @Test
        @DisplayName("should return account when found")
        void shouldReturnAccountWhenFound() throws Exception {
            AccountDto account = new AccountDto("ACC-001", "王大明", new BigDecimal("10000.00"), "ACTIVE");
            when(paymentService.getAccount("ACC-001")).thenReturn(account);

            mockMvc.perform(get("/api/v1/payments/accounts/ACC-001"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                    .andExpect(jsonPath("$.ownerName").value("王大明"))
                    .andExpect(jsonPath("$.balance").value(10000.00))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments")
    class ProcessPayment {

        @Test
        @DisplayName("should process payment successfully")
        void shouldProcessPaymentSuccessfully() throws Exception {
            PaymentResult result = PaymentResult.success(
                    "ACC-001",
                    new BigDecimal("10000.00"),
                    new BigDecimal("9000.00"),
                    new BigDecimal("1000.00")
            );
            when(paymentService.processPayment(any())).thenReturn(result);

            String requestBody = """
                    {
                        "accountNumber": "ACC-001",
                        "amount": 1000.00
                    }
                    """;

            mockMvc.perform(post("/api/v1/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                    .andExpect(jsonPath("$.previousBalance").value(10000.00))
                    .andExpect(jsonPath("$.currentBalance").value(9000.00));
        }

        @Test
        @DisplayName("should return bad request for failed payment")
        void shouldReturnBadRequestForFailedPayment() throws Exception {
            PaymentResult result = PaymentResult.failed("ACC-001", "Insufficient balance");
            when(paymentService.processPayment(any())).thenReturn(result);

            String requestBody = """
                    {
                        "accountNumber": "ACC-001",
                        "amount": 99999.00
                    }
                    """;

            mockMvc.perform(post("/api/v1/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("FAILED"))
                    .andExpect(jsonPath("$.message").value("Insufficient balance"));
        }

        @Test
        @DisplayName("should return internal server error for error result")
        void shouldReturnInternalServerErrorForErrorResult() throws Exception {
            PaymentResult result = PaymentResult.error("ACC-001", "Service unavailable");
            when(paymentService.processPayment(any())).thenReturn(result);

            String requestBody = """
                    {
                        "accountNumber": "ACC-001",
                        "amount": 1000.00
                    }
                    """;

            mockMvc.perform(post("/api/v1/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("Service unavailable"));
        }

        @Test
        @DisplayName("should return bad request for invalid request body")
        void shouldReturnBadRequestForInvalidRequestBody() throws Exception {
            String requestBody = """
                    {
                        "accountNumber": "",
                        "amount": -100
                    }
                    """;

            mockMvc.perform(post("/api/v1/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }
}
