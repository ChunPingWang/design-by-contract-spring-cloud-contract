package com.example.account.domain;

/**
 * Account status with state machine transitions.
 *
 * State transitions:
 * - ACTIVE -> FROZEN (via freeze operation)
 * - FROZEN -> ACTIVE (via unfreeze operation)
 * - ACTIVE -> CLOSED (via close operation)
 * - FROZEN -> CLOSED (via close operation)
 *
 * Invariant: CLOSED is a terminal state, no transitions allowed from CLOSED.
 */
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED;

    /**
     * Check if debit operation is allowed in current status.
     * Precondition: none
     * Postcondition: returns true only if status is ACTIVE
     */
    public boolean canDebit() {
        return this == ACTIVE;
    }

    /**
     * Check if freeze operation is allowed in current status.
     * Precondition: none
     * Postcondition: returns true only if status is ACTIVE
     */
    public boolean canFreeze() {
        return this == ACTIVE;
    }

    /**
     * Check if unfreeze operation is allowed in current status.
     * Precondition: none
     * Postcondition: returns true only if status is FROZEN
     */
    public boolean canUnfreeze() {
        return this == FROZEN;
    }

    /**
     * Check if close operation is allowed in current status.
     * Precondition: none
     * Postcondition: returns true if status is not already CLOSED
     */
    public boolean canClose() {
        return this != CLOSED;
    }
}
