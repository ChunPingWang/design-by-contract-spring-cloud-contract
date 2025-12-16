package com.example.payment.infrastructure.controller;

import com.example.payment.application.PaymentService;
import com.example.payment.application.PaymentService.PaymentResult;
import com.example.payment.domain.PaymentRequest;
import com.example.payment.infrastructure.client.dto.AccountDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Get account information (proxied from account-service).
     */
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String accountNumber) {
        log.debug("GET /api/v1/payments/accounts/{}", accountNumber);
        AccountDto account = paymentService.getAccount(accountNumber);
        return ResponseEntity.ok(account);
    }

    /**
     * Process a payment.
     *
     * This endpoint orchestrates the payment by:
     * 1. Verifying the account exists and is active
     * 2. Debiting the amount from the account
     */
    @PostMapping
    public ResponseEntity<PaymentResult> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.debug("POST /api/v1/payments - account={}, amount={}",
                request.accountNumber(), request.amount());

        PaymentResult result = paymentService.processPayment(request);

        return switch (result.status()) {
            case SUCCESS -> ResponseEntity.ok(result);
            case FAILED -> ResponseEntity.badRequest().body(result);
            case ERROR -> ResponseEntity.internalServerError().body(result);
        };
    }
}
