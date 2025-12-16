package com.example.account.infrastructure.controller;

import com.example.account.application.AccountService;
import com.example.account.domain.Account;
import com.example.account.domain.DebitRequest;
import com.example.account.domain.DebitResponse;
import com.example.account.infrastructure.dto.CreateAccountRequest;
import com.example.account.infrastructure.dto.FreezeAccountRequest;
import com.example.account.infrastructure.exception.AccountNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Get account by account number.
     *
     * Precondition: accountNumber is valid format
     * Postcondition: returns account details if found, 404 otherwise
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        log.debug("GET /api/v1/accounts/{}", accountNumber);

        return accountService.getAccount(accountNumber)
                .map(account -> ResponseEntity.ok(AccountResponse.from(account)))
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    /**
     * Create a new account.
     *
     * Precondition: request is valid, accountNumber is unique
     * Postcondition: new account created with ACTIVE status
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.debug("POST /api/v1/accounts - Creating account: {}", request.accountNumber());

        Account account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(account));
    }

    /**
     * Debit from an account.
     *
     * Precondition: accountNumber exists, amount > 0, account is ACTIVE
     * Postcondition: if balance sufficient, balance decreased; otherwise unchanged
     */
    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<DebitResponse> debitAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody DebitRequest request) {
        log.debug("POST /api/v1/accounts/{}/debit - amount: {}", accountNumber, request.amount());

        DebitResponse response = accountService.debit(accountNumber, request.amount());
        return ResponseEntity.ok(response);
    }

    /**
     * Freeze an account.
     *
     * Precondition: accountNumber exists, status is ACTIVE
     * Postcondition: status changed to FROZEN
     */
    @PostMapping("/{accountNumber}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(
            @PathVariable String accountNumber,
            @RequestBody(required = false) FreezeAccountRequest request) {
        log.debug("POST /api/v1/accounts/{}/freeze", accountNumber);

        Account account = accountService.freeze(accountNumber);
        return ResponseEntity.ok(AccountResponse.from(account));
    }

    /**
     * Unfreeze an account.
     *
     * Precondition: accountNumber exists, status is FROZEN
     * Postcondition: status changed to ACTIVE
     */
    @PostMapping("/{accountNumber}/unfreeze")
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable String accountNumber) {
        log.debug("POST /api/v1/accounts/{}/unfreeze", accountNumber);

        Account account = accountService.unfreeze(accountNumber);
        return ResponseEntity.ok(AccountResponse.from(account));
    }

    /**
     * Response DTO for Account.
     *
     * Version 1.1.0: Added createdAt field (backward compatible - optional field)
     */
    public record AccountResponse(
            String accountNumber,
            String ownerName,
            java.math.BigDecimal balance,
            String status,
            // New optional field added in v1.1.0 - backward compatible
            String createdAt
    ) {
        public static AccountResponse from(Account account) {
            return new AccountResponse(
                    account.getAccountNumber(),
                    account.getOwnerName(),
                    account.getBalance(),
                    account.getStatus().name(),
                    account.getCreatedAt() != null ? account.getCreatedAt().toString() : null
            );
        }
    }
}
