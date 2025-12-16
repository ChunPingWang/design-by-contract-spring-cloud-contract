package com.example.account.domain;

import java.math.BigDecimal;

/**
 * Value object for debit response.
 *
 * Postcondition: Contains the result of a debit operation
 */
public record DebitResponse(
        String accountNumber,
        BigDecimal previousBalance,
        BigDecimal currentBalance,
        BigDecimal debitedAmount,
        boolean success,
        String message
) {
    public static DebitResponse success(String accountNumber, BigDecimal previousBalance,
                                         BigDecimal currentBalance, BigDecimal debitedAmount) {
        return new DebitResponse(accountNumber, previousBalance, currentBalance, debitedAmount,
                true, "Debit successful");
    }

    public static DebitResponse insufficientBalance(String accountNumber, BigDecimal currentBalance,
                                                     BigDecimal requestedAmount) {
        return new DebitResponse(accountNumber, currentBalance, currentBalance, requestedAmount,
                false, "Insufficient balance");
    }
}
