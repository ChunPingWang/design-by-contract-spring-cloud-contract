package com.example.payment.contract;

import com.example.payment.infrastructure.client.AccountClient;
import com.example.payment.infrastructure.client.dto.AccountDto;
import com.example.payment.infrastructure.client.dto.DebitRequestDto;
import com.example.payment.infrastructure.client.dto.DebitResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer contract tests using Stub Runner.
 *
 * These tests verify that the payment-service can correctly consume
 * the account-service API according to the contracts.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "account-service.url=http://localhost:6565"
)
@AutoConfigureStubRunner(
        ids = "com.example:account-service:+:stubs:6565",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public class AccountClientContractTest {

    @StubRunnerPort("account-service")
    int stubPort;

    @Autowired
    private AccountClient accountClient;

    @Test
    void shouldGetAccountSuccessfully() {
        // Contract: getAccount.groovy
        // Precondition: accountId ACC-001 exists
        // Postcondition: returns account details

        AccountDto account = accountClient.getAccount("ACC-001");

        assertThat(account).isNotNull();
        assertThat(account.accountNumber()).isEqualTo("ACC-001");
        assertThat(account.ownerName()).isEqualTo("王大明");
        assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(account.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldDebitAccountSuccessfully() {
        // Contract: debitAccount.groovy
        // Precondition: accountId ACC-001 exists, sufficient balance
        // Postcondition: balance decreased, success response

        DebitRequestDto request = new DebitRequestDto(new BigDecimal("1000.00"));
        DebitResponseDto response = accountClient.debit("ACC-001", request);

        assertThat(response).isNotNull();
        assertThat(response.success()).isTrue();
        assertThat(response.accountNumber()).isEqualTo("ACC-001");
        assertThat(response.previousBalance()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(response.currentBalance()).isEqualByComparingTo(new BigDecimal("9000.00"));
        assertThat(response.debitedAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.message()).isEqualTo("Debit successful");
    }

    @Test
    void shouldHandleInsufficientBalance() {
        // Contract: debitInsufficientBalance.groovy
        // Precondition: accountId ACC-003 exists, insufficient balance
        // Postcondition: balance unchanged, failure response

        DebitRequestDto request = new DebitRequestDto(new BigDecimal("99999.00"));
        DebitResponseDto response = accountClient.debit("ACC-003", request);

        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();
        assertThat(response.accountNumber()).isEqualTo("ACC-003");
        assertThat(response.currentBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.message()).isEqualTo("Insufficient balance");
    }
}
