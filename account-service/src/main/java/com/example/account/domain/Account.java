package com.example.account.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account domain entity.
 *
 * Invariants:
 * - balance >= 0 (non-negative balance)
 * - accountNumber is unique and immutable after creation
 * - status follows state machine rules defined in AccountStatus
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Account() {
        // JPA required
    }

    /**
     * Create a new account.
     * Precondition: accountNumber != null, ownerName != null, initialBalance >= 0
     * Postcondition: new account with ACTIVE status and given initial balance
     */
    public Account(String accountNumber, String ownerName, BigDecimal initialBalance) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be null or blank");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name cannot be null or blank");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be null or negative");
        }

        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Debit the account by the specified amount.
     * Precondition: amount > 0, status == ACTIVE, balance >= amount
     * Postcondition: balance = old_balance - amount
     *
     * @param amount the amount to debit
     * @return true if debit was successful
     * @throws IllegalStateException if account is not active
     * @throws IllegalArgumentException if amount is invalid or insufficient balance
     */
    public boolean debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (!status.canDebit()) {
            throw new IllegalStateException("Cannot debit from account with status: " + status);
        }
        if (balance.compareTo(amount) < 0) {
            return false; // Insufficient balance
        }

        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * Freeze the account.
     * Precondition: status == ACTIVE
     * Postcondition: status == FROZEN
     */
    public void freeze() {
        if (!status.canFreeze()) {
            throw new IllegalStateException("Cannot freeze account with status: " + status);
        }
        this.status = AccountStatus.FROZEN;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Unfreeze the account.
     * Precondition: status == FROZEN
     * Postcondition: status == ACTIVE
     */
    public void unfreeze() {
        if (!status.canUnfreeze()) {
            throw new IllegalStateException("Cannot unfreeze account with status: " + status);
        }
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // For testing purposes
    public void setId(Long id) {
        this.id = id;
    }
}
