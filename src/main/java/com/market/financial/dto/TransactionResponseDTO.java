package com.market.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDTO(
    Long id,
    LocalDate date,
    Integer operationTypeId,
    String operationTypeDescription,
    String assetId,
    BigDecimal stock,
    BigDecimal unitValue,
    BigDecimal fee,
    String memo,
    Integer active,
    Integer refCompra,
    LocalDate dateSales
) {}
