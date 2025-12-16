package com.example.account.application;

import com.example.account.domain.Account;
import com.example.account.domain.DebitResponse;
import com.example.account.infrastructure.dto.CreateAccountRequest;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Account service interface.
 *
 * Contract definitions:
 * - All methods throw AccountNotFoundException if account does not exist (except create)
 * - Debit operations may throw InsufficientBalanceException or AccountFrozenException
 */
public interface AccountService {

    /**
     * Get account by account number.
     * Precondition: accountNumber != null
     * Postcondition: returns Optional containing account if found, empty otherwise
     */
    Optional<Account> getAccount(String accountNumber);

    /**
     * Create a new account.
     * Precondition: request is valid, accountNumber is unique
     * Postcondition: new account created with ACTIVE status
     *
     * @throws IllegalArgumentException if account number already exists
     */
    Account createAccount(CreateAccountRequest request);

    /**
     * Debit the specified amount from an account.
     * Precondition: accountNumber exists, amount > 0
     * Postcondition: if successful, balance decreased by amount; otherwise unchanged
     *
     * @throws AccountNotFoundException if account does not exist
     * @throws AccountFrozenException if account is frozen
     */
    DebitResponse debit(String accountNumber, BigDecimal amount);

    /**
     * Freeze an account.
     * Precondition: accountNumber exists, status == ACTIVE
     * Postcondition: status == FROZEN
     *
     * @throws AccountNotFoundException if account does not exist
     * @throws IllegalStateException if account is not in ACTIVE status
     */
    Account freeze(String accountNumber);

    /**
     * Unfreeze an account.
     * Precondition: accountNumber exists, status == FROZEN
     * Postcondition: status == ACTIVE
     *
     * @throws AccountNotFoundException if account does not exist
     * @throws IllegalStateException if account is not in FROZEN status
     */
    Account unfreeze(String accountNumber);
}
