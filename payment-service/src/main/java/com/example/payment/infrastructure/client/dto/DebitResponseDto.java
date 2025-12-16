package com.example.payment.infrastructure.client.dto;

import java.math.BigDecimal;

/**
 * DTO for debit response from Account Service.
 */
public record DebitResponseDto(
        String accountNumber,
        BigDecimal previousBalance,
        BigDecimal currentBalance,
        BigDecimal debitedAmount,
        boolean success,
        String message
) {
}
