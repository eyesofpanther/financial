package com.market.financial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyConsolidatedRequestDTO(
    @NotNull(message = "A data do consolidado é obrigatória")
    LocalDate date,

    @NotBlank(message = "O ID do ativo é obrigatório")
    String assetId,

    @NotNull(message = "A quantidade (Stock) é obrigatória")
    @PositiveOrZero(message = "A quantidade de ações não pode ser negativa")
    BigDecimal stock
) {}
