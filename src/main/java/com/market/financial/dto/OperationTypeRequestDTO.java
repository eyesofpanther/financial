package com.market.financial.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OperationTypeRequestDTO(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 20, message = "A descrição deve ter no máximo 20 caracteres")
    String description,

    @NotNull(message = "O campo IO é obrigatório")
    @Min(value = 0, message = "O campo IO deve ser 0 ou 1")
    @Max(value = 1, message = "O campo IO deve ser 0 ou 1")
    Integer io
) {}

