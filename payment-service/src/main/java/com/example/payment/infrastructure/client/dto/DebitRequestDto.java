package com.example.payment.infrastructure.client.dto;

import java.math.BigDecimal;

/**
 * DTO for debit request to Account Service.
 */
public record DebitRequestDto(
        BigDecimal amount
) {
}
