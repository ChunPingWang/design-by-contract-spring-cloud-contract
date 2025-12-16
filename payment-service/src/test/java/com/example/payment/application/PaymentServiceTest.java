package com.example.payment.application;

import com.example.payment.application.PaymentService.PaymentResult;
import com.example.payment.domain.PaymentRequest;
import com.example.payment.infrastructure.client.AccountClient;
import com.example.payment.infrastructure.client.dto.AccountDto;
import com.example.payment.infrastructure.client.dto.DebitRequestDto;
import com.example.payment.infrastructure.client.dto.DebitResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private AccountClient accountClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(accountClient);
    }

    @Nested
    @DisplayName("getAccount()")
    class GetAccountTests {

        @Test
        @DisplayName("should return account from client")
        void shouldReturnAccountFromClient() {
            AccountDto expectedAccount = new AccountDto("ACC-001", "王大明", new BigDecimal("10000.00"), "ACTIVE");
            when(accountClient.getAccount("ACC-001")).thenReturn(expectedAccount);

            AccountDto result = paymentService.getAccount("ACC-001");

            assertThat(result).isEqualTo(expectedAccount);
        }
    }

    @Nested
    @DisplayName("processPayment()")
    class ProcessPaymentTests {

        @Test
        @DisplayName("should process payment successfully")
        void shouldProcessPaymentSuccessfully() {
            AccountDto account = new AccountDto("ACC-001", "王大明", new BigDecimal("10000.00"), "ACTIVE");
            DebitResponseDto debitResponse = new DebitResponseDto(
                    "ACC-001",
                    new BigDecimal("10000.00"),
                    new BigDecimal("9000.00"),
                    new BigDecimal("1000.00"),
                    true,
                    "Debit successful"
            );
            when(accountClient.getAccount("ACC-001")).thenReturn(account);
            when(accountClient.debit(eq("ACC-001"), any(DebitRequestDto.class))).thenReturn(debitResponse);

            PaymentRequest request = new PaymentRequest("ACC-001", new BigDecimal("1000.00"), "Test payment");
            PaymentResult result = paymentService.processPayment(request);

            assertThat(result.status()).isEqualTo(PaymentResult.Status.SUCCESS);
            assertThat(result.accountNumber()).isEqualTo("ACC-001");
            assertThat(result.previousBalance()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(result.currentBalance()).isEqualByComparingTo(new BigDecimal("9000.00"));
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("should fail when account is not active")
        void shouldFailWhenAccountIsNotActive() {
            AccountDto frozenAccount = new AccountDto("ACC-001", "王大明", new BigDecimal("10000.00"), "FROZEN");
            when(accountClient.getAccount("ACC-001")).thenReturn(frozenAccount);

            PaymentRequest request = new PaymentRequest("ACC-001", new BigDecimal("1000.00"), "Test payment");
            PaymentResult result = paymentService.processPayment(request);

            assertThat(result.status()).isEqualTo(PaymentResult.Status.FAILED);
            assertThat(result.message()).isEqualTo("Account is not active");
        }

        @Test
        @DisplayName("should fail when insufficient balance")
        void shouldFailWhenInsufficientBalance() {
            AccountDto account = new AccountDto("ACC-001", "王大明", new BigDecimal("500.00"), "ACTIVE");
            DebitResponseDto debitResponse = new DebitResponseDto(
                    "ACC-001",
                    new BigDecimal("500.00"),
                    new BigDecimal("500.00"),
                    new BigDecimal("1000.00"),
                    false,
                    "Insufficient balance"
            );
            when(accountClient.getAccount("ACC-001")).thenReturn(account);
            when(accountClient.debit(eq("ACC-001"), any(DebitRequestDto.class))).thenReturn(debitResponse);

            PaymentRequest request = new PaymentRequest("ACC-001", new BigDecimal("1000.00"), "Test payment");
            PaymentResult result = paymentService.processPayment(request);

            assertThat(result.status()).isEqualTo(PaymentResult.Status.FAILED);
            assertThat(result.message()).isEqualTo("Insufficient balance");
        }

        @Test
        @DisplayName("should return error when exception occurs")
        void shouldReturnErrorWhenExceptionOccurs() {
            when(accountClient.getAccount("ACC-001")).thenThrow(new RuntimeException("Service unavailable"));

            PaymentRequest request = new PaymentRequest("ACC-001", new BigDecimal("1000.00"), "Test payment");
            PaymentResult result = paymentService.processPayment(request);

            assertThat(result.status()).isEqualTo(PaymentResult.Status.ERROR);
            assertThat(result.message()).contains("Service unavailable");
        }
    }

    @Nested
    @DisplayName("PaymentResult")
    class PaymentResultTests {

        @Test
        @DisplayName("should create success result")
        void shouldCreateSuccessResult() {
            PaymentResult result = PaymentResult.success(
                    "ACC-001",
                    new BigDecimal("10000.00"),
                    new BigDecimal("9000.00"),
                    new BigDecimal("1000.00")
            );

            assertThat(result.status()).isEqualTo(PaymentResult.Status.SUCCESS);
            assertThat(result.message()).isEqualTo("Payment successful");
        }

        @Test
        @DisplayName("should create failed result")
        void shouldCreateFailedResult() {
            PaymentResult result = PaymentResult.failed("ACC-001", "Insufficient balance");

            assertThat(result.status()).isEqualTo(PaymentResult.Status.FAILED);
            assertThat(result.message()).isEqualTo("Insufficient balance");
            assertThat(result.previousBalance()).isNull();
        }

        @Test
        @DisplayName("should create error result")
        void shouldCreateErrorResult() {
            PaymentResult result = PaymentResult.error("ACC-001", "Service unavailable");

            assertThat(result.status()).isEqualTo(PaymentResult.Status.ERROR);
            assertThat(result.message()).isEqualTo("Service unavailable");
        }
    }
}
