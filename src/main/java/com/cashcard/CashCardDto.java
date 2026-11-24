package com.cashcard;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CashCardDto(
        Long id,
        @NotNull(message = "Balance is required")
        @Positive(message = "double must be positive")
        Double balance,

        String owner
) {
}
