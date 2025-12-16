package com.example.payment.infrastructure.client.dto;

import java.math.BigDecimal;

/**
 * DTO representing account data from Account Service.
 */
public record AccountDto(
        String accountNumber,
        String ownerName,
        BigDecimal balance,
        String status
) {
}
