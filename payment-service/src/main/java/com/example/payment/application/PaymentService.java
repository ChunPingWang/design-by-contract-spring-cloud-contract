package com.example.payment.application;

import com.example.payment.domain.PaymentRequest;
import com.example.payment.infrastructure.client.AccountClient;
import com.example.payment.infrastructure.client.dto.AccountDto;
import com.example.payment.infrastructure.client.dto.DebitRequestDto;
import com.example.payment.infrastructure.client.dto.DebitResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Payment service that orchestrates payment operations.
 *
 * Contract dependencies:
 * - Uses account-service contracts for account operations
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final AccountClient accountClient;

    public PaymentService(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    /**
     * Get account information.
     *
     * Delegates to account-service via contract.
     */
    public AccountDto getAccount(String accountNumber) {
        log.info("Getting account: {}", accountNumber);
        return accountClient.getAccount(accountNumber);
    }

    /**
     * Process a payment by debiting from an account.
     *
     * Precondition: account exists, sufficient balance, account is active
     * Postcondition: if successful, balance decreased by amount
     *
     * @param request the payment request
     * @return payment result
     */
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing payment: account={}, amount={}", request.accountNumber(), request.amount());

        try {
            // First, verify account exists and is active
            AccountDto account = accountClient.getAccount(request.accountNumber());
            log.debug("Account found: {}, status: {}", account.accountNumber(), account.status());

            if (!"ACTIVE".equals(account.status())) {
                log.warn("Account is not active: {}", account.status());
                return PaymentResult.failed(request.accountNumber(), "Account is not active");
            }

            // Perform debit
            DebitRequestDto debitRequest = new DebitRequestDto(request.amount());
            DebitResponseDto debitResponse = accountClient.debit(request.accountNumber(), debitRequest);

            if (debitResponse.success()) {
                log.info("Payment successful: account={}, amount={}", request.accountNumber(), request.amount());
                return PaymentResult.success(
                        request.accountNumber(),
                        debitResponse.previousBalance(),
                        debitResponse.currentBalance(),
                        request.amount()
                );
            } else {
                log.warn("Payment failed: {}", debitResponse.message());
                return PaymentResult.failed(request.accountNumber(), debitResponse.message());
            }
        } catch (Exception e) {
            log.error("Payment error for account {}: {}", request.accountNumber(), e.getMessage());
            return PaymentResult.error(request.accountNumber(), e.getMessage());
        }
    }

    /**
     * Payment result record.
     */
    public record PaymentResult(
            String accountNumber,
            java.math.BigDecimal previousBalance,
            java.math.BigDecimal currentBalance,
            java.math.BigDecimal amount,
            Status status,
            String message
    ) {
        public enum Status {
            SUCCESS, FAILED, ERROR
        }

        public static PaymentResult success(String accountNumber, java.math.BigDecimal previousBalance,
                                            java.math.BigDecimal currentBalance, java.math.BigDecimal amount) {
            return new PaymentResult(accountNumber, previousBalance, currentBalance, amount, Status.SUCCESS, "Payment successful");
        }

        public static PaymentResult failed(String accountNumber, String message) {
            return new PaymentResult(accountNumber, null, null, null, Status.FAILED, message);
        }

        public static PaymentResult error(String accountNumber, String message) {
            return new PaymentResult(accountNumber, null, null, null, Status.ERROR, message);
        }
    }
}
