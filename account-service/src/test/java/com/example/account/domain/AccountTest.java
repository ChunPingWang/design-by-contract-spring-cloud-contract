package com.example.account.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account Domain Tests")
class AccountTest {

    @Nested
    @DisplayName("Account Creation")
    class AccountCreation {

        @Test
        @DisplayName("should create account with valid parameters")
        void shouldCreateAccountWithValidParameters() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));

            assertThat(account.getAccountNumber()).isEqualTo("ACC-001");
            assertThat(account.getOwnerName()).isEqualTo("王大明");
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(account.getCreatedAt()).isNotNull();
            assertThat(account.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should create account with zero balance")
        void shouldCreateAccountWithZeroBalance() {
            Account account = new Account("ACC-002", "李小華", BigDecimal.ZERO);

            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("should throw exception for null account number")
        void shouldThrowExceptionForNullAccountNumber() {
            assertThatThrownBy(() -> new Account(null, "王大明", new BigDecimal("10000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account number cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception for blank account number")
        void shouldThrowExceptionForBlankAccountNumber() {
            assertThatThrownBy(() -> new Account("  ", "王大明", new BigDecimal("10000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account number cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception for null owner name")
        void shouldThrowExceptionForNullOwnerName() {
            assertThatThrownBy(() -> new Account("ACC-001", null, new BigDecimal("10000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Owner name cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception for blank owner name")
        void shouldThrowExceptionForBlankOwnerName() {
            assertThatThrownBy(() -> new Account("ACC-001", "", new BigDecimal("10000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Owner name cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception for null balance")
        void shouldThrowExceptionForNullBalance() {
            assertThatThrownBy(() -> new Account("ACC-001", "王大明", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Initial balance cannot be null or negative");
        }

        @Test
        @DisplayName("should throw exception for negative balance")
        void shouldThrowExceptionForNegativeBalance() {
            assertThatThrownBy(() -> new Account("ACC-001", "王大明", new BigDecimal("-100")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Initial balance cannot be null or negative");
        }
    }

    @Nested
    @DisplayName("Debit Operations")
    class DebitOperations {

        @Test
        @DisplayName("should debit successfully with sufficient balance")
        void shouldDebitSuccessfullyWithSufficientBalance() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));

            boolean result = account.debit(new BigDecimal("1000.00"));

            assertThat(result).isTrue();
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("9000.00"));
        }

        @Test
        @DisplayName("should debit exact balance successfully")
        void shouldDebitExactBalanceSuccessfully() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("1000.00"));

            boolean result = account.debit(new BigDecimal("1000.00"));

            assertThat(result).isTrue();
            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should return false for insufficient balance")
        void shouldReturnFalseForInsufficientBalance() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("500.00"));

            boolean result = account.debit(new BigDecimal("1000.00"));

            assertThat(result).isFalse();
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("should throw exception for null debit amount")
        void shouldThrowExceptionForNullDebitAmount() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));

            assertThatThrownBy(() -> account.debit(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debit amount must be positive");
        }

        @Test
        @DisplayName("should throw exception for zero debit amount")
        void shouldThrowExceptionForZeroDebitAmount() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));

            assertThatThrownBy(() -> account.debit(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debit amount must be positive");
        }

        @Test
        @DisplayName("should throw exception for negative debit amount")
        void shouldThrowExceptionForNegativeDebitAmount() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));

            assertThatThrownBy(() -> account.debit(new BigDecimal("-100")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debit amount must be positive");
        }

        @Test
        @DisplayName("should throw exception when debiting from frozen account")
        void shouldThrowExceptionWhenDebitingFromFrozenAccount() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));
            account.freeze();

            assertThatThrownBy(() -> account.debit(new BigDecimal("1000.00")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot debit from account with status: FROZEN");
        }
    }

    @Nested
    @DisplayName("Freeze Operations")
    class FreezeOperations {

        @Test
        @DisplayName("should freeze active account successfully")
        void shouldFreezeActiveAccountSuccessfully() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));

            account.freeze();

            assertThat(account.getStatus()).isEqualTo(AccountStatus.FROZEN);
        }

        @Test
        @DisplayName("should throw exception when freezing already frozen account")
        void shouldThrowExceptionWhenFreezingAlreadyFrozenAccount() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));
            account.freeze();

            assertThatThrownBy(() -> account.freeze())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot freeze account with status: FROZEN");
        }
    }

    @Nested
    @DisplayName("Unfreeze Operations")
    class UnfreezeOperations {

        @Test
        @DisplayName("should unfreeze frozen account successfully")
        void shouldUnfreezeFrozenAccountSuccessfully() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));
            account.freeze();

            account.unfreeze();

            assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("should throw exception when unfreezing active account")
        void shouldThrowExceptionWhenUnfreezingActiveAccount() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));

            assertThatThrownBy(() -> account.unfreeze())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot unfreeze account with status: ACTIVE");
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSetters {

        @Test
        @DisplayName("should get and set id")
        void shouldGetAndSetId() {
            Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));

            account.setId(123L);

            assertThat(account.getId()).isEqualTo(123L);
        }
    }
}
