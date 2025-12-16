package com.example.payment.infrastructure.client;

import com.example.payment.infrastructure.client.dto.AccountDto;
import com.example.payment.infrastructure.client.dto.DebitRequestDto;
import com.example.payment.infrastructure.client.dto.DebitResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Account Service.
 *
 * Contract: This client follows the contract defined by account-service contracts.
 */
@FeignClient(name = "account-service", url = "${account-service.url:http://localhost:8080}")
public interface AccountClient {

    /**
     * Get account by account number.
     *
     * Contract: getAccount.groovy
     * Precondition: accountNumber exists
     * Postcondition: returns account details
     */
    @GetMapping("/api/v1/accounts/{accountNumber}")
    AccountDto getAccount(@PathVariable("accountNumber") String accountNumber);

    /**
     * Debit from an account.
     *
     * Contract: debitAccount.groovy, debitInsufficientBalance.groovy
     * Precondition: accountNumber exists, amount > 0
     * Postcondition: returns debit result
     */
    @PostMapping("/api/v1/accounts/{accountNumber}/debit")
    DebitResponseDto debit(@PathVariable("accountNumber") String accountNumber,
                           @RequestBody DebitRequestDto request);
}
