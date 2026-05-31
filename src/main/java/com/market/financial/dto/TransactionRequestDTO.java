package com.market.financial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDTO(
    @NotNull(message = "A data da transação é obrigatória")
    LocalDate date,

    @NotNull(message = "O ID do tipo de operação é obrigatório")
    Integer operationTypeId,

    @NotBlank(message = "O ID do ativo é obrigatório")
    String assetId,

    @NotNull(message = "A quantidade (Stock) é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    BigDecimal stock,

    @Positive(message = "O valor unitário deve ser maior que zero")
    BigDecimal unitValue,

    BigDecimal fee,
    String memo,
    Integer active,
    Integer refCompra,
    LocalDate dateSales
) {}
