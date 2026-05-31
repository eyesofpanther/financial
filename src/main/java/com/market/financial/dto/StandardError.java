package com.market.financial.dto;

import java.time.Instant;
import java.util.List;

public record StandardError(
    Instant timestamp,
    Integer status,
    String error,
    String path,
    List<ValidationErrorField> errors // Lista detalhada para validações como @NotBlank, @Min, etc.
) {
    public record ValidationErrorField(String field, String message) {}
}
