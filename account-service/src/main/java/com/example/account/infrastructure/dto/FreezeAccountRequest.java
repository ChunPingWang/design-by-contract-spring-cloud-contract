package com.example.account.infrastructure.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for freeze/unfreeze account request.
 */
public record FreezeAccountRequest(
        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason
) {
}
