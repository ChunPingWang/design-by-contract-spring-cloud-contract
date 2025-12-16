package com.example.account.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DebitResponse Tests")
class DebitResponseTest {

    @Nested
    @DisplayName("Success Factory Method")
    class SuccessFactoryMethod {

        @Test
        @DisplayName("should create successful debit response")
        void shouldCreateSuccessfulDebitResponse() {
            DebitResponse response = DebitResponse.success(
                    "ACC-001",
                    new BigDecimal("10000.00"),
                    new BigDecimal("9000.00"),
                    new BigDecimal("1000.00")
            );

            assertThat(response.accountNumber()).isEqualTo("ACC-001");
            assertThat(response.previousBalance()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(response.currentBalance()).isEqualByComparingTo(new BigDecimal("9000.00"));
            assertThat(response.debitedAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Debit successful");
        }
    }

    @Nested
    @DisplayName("Insufficient Balance Factory Method")
    class InsufficientBalanceFactoryMethod {

        @Test
        @DisplayName("should create insufficient balance response")
        void shouldCreateInsufficientBalanceResponse() {
            DebitResponse response = DebitResponse.insufficientBalance(
                    "ACC-001",
                    new BigDecimal("500.00"),
                    new BigDecimal("1000.00")
            );

            assertThat(response.accountNumber()).isEqualTo("ACC-001");
            assertThat(response.previousBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(response.currentBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(response.debitedAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(response.success()).isFalse();
            assertThat(response.message()).isEqualTo("Insufficient balance");
        }
    }

    @Nested
    @DisplayName("Record Properties")
    class RecordProperties {

        @Test
        @DisplayName("should support record equality")
        void shouldSupportRecordEquality() {
            DebitResponse response1 = new DebitResponse(
                    "ACC-001",
                    new BigDecimal("10000.00"),
                    new BigDecimal("9000.00"),
                    new BigDecimal("1000.00"),
                    true,
                    "Debit successful"
            );
            DebitResponse response2 = new DebitResponse(
                    "ACC-001",
                    new BigDecimal("10000.00"),
                    new BigDecimal("9000.00"),
                    new BigDecimal("1000.00"),
                    true,
                    "Debit successful"
            );

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }
}
