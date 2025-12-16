package com.example.account.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountStatus Tests")
class AccountStatusTest {

    @Nested
    @DisplayName("canDebit() Tests")
    class CanDebitTests {

        @Test
        @DisplayName("ACTIVE status should allow debit")
        void activeStatusShouldAllowDebit() {
            assertThat(AccountStatus.ACTIVE.canDebit()).isTrue();
        }

        @Test
        @DisplayName("FROZEN status should not allow debit")
        void frozenStatusShouldNotAllowDebit() {
            assertThat(AccountStatus.FROZEN.canDebit()).isFalse();
        }

        @Test
        @DisplayName("CLOSED status should not allow debit")
        void closedStatusShouldNotAllowDebit() {
            assertThat(AccountStatus.CLOSED.canDebit()).isFalse();
        }
    }

    @Nested
    @DisplayName("canFreeze() Tests")
    class CanFreezeTests {

        @Test
        @DisplayName("ACTIVE status should allow freeze")
        void activeStatusShouldAllowFreeze() {
            assertThat(AccountStatus.ACTIVE.canFreeze()).isTrue();
        }

        @Test
        @DisplayName("FROZEN status should not allow freeze")
        void frozenStatusShouldNotAllowFreeze() {
            assertThat(AccountStatus.FROZEN.canFreeze()).isFalse();
        }

        @Test
        @DisplayName("CLOSED status should not allow freeze")
        void closedStatusShouldNotAllowFreeze() {
            assertThat(AccountStatus.CLOSED.canFreeze()).isFalse();
        }
    }

    @Nested
    @DisplayName("canUnfreeze() Tests")
    class CanUnfreezeTests {

        @Test
        @DisplayName("FROZEN status should allow unfreeze")
        void frozenStatusShouldAllowUnfreeze() {
            assertThat(AccountStatus.FROZEN.canUnfreeze()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE status should not allow unfreeze")
        void activeStatusShouldNotAllowUnfreeze() {
            assertThat(AccountStatus.ACTIVE.canUnfreeze()).isFalse();
        }

        @Test
        @DisplayName("CLOSED status should not allow unfreeze")
        void closedStatusShouldNotAllowUnfreeze() {
            assertThat(AccountStatus.CLOSED.canUnfreeze()).isFalse();
        }
    }

    @Nested
    @DisplayName("canClose() Tests")
    class CanCloseTests {

        @Test
        @DisplayName("ACTIVE status should allow close")
        void activeStatusShouldAllowClose() {
            assertThat(AccountStatus.ACTIVE.canClose()).isTrue();
        }

        @Test
        @DisplayName("FROZEN status should allow close")
        void frozenStatusShouldAllowClose() {
            assertThat(AccountStatus.FROZEN.canClose()).isTrue();
        }

        @Test
        @DisplayName("CLOSED status should not allow close")
        void closedStatusShouldNotAllowClose() {
            assertThat(AccountStatus.CLOSED.canClose()).isFalse();
        }
    }

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @ParameterizedTest
        @EnumSource(AccountStatus.class)
        @DisplayName("should have all expected enum values")
        void shouldHaveAllExpectedEnumValues(AccountStatus status) {
            assertThat(status).isNotNull();
        }

        @Test
        @DisplayName("should have exactly 3 status values")
        void shouldHaveExactlyThreeStatusValues() {
            assertThat(AccountStatus.values()).hasSize(3);
        }
    }
}
