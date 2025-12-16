package com.example.account.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for creating a new account.
 *
 * Preconditions:
 * - accountNumber: not blank, 6-20 characters
 * - ownerName: not blank, 1-100 characters
 * - initialBalance: >= 0
 */
public record CreateAccountRequest(
        @NotBlank(message = "Account number is required")
        @Size(min = 6, max = 20, message = "Account number must be 6-20 characters")
        String accountNumber,

        @NotBlank(message = "Owner name is required")
        @Size(min = 1, max = 100, message = "Owner name must be 1-100 characters")
        String ownerName,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
        BigDecimal initialBalance
) {
}
