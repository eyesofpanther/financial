package com.market.financial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceRequestDTO(
    @NotNull(message = "A data é obrigatória")
    @PastOrPresent(message = "A data não pode ser uma data futura")
    LocalDate date,

    @NotBlank(message = "O ID do ativo é obrigatório")
    @Size(min = 1, max = 5, message = "O ID do ativo deve ter entre 1 e 5 caracteres")
    String assetId,

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser um valor maior que zero")
    BigDecimal price
) {}
