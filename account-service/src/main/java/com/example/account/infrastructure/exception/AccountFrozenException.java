package com.example.account.infrastructure.exception;

public class AccountFrozenException extends RuntimeException {

    private final String accountNumber;

    public AccountFrozenException(String accountNumber) {
        super("Account is frozen: " + accountNumber);
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
