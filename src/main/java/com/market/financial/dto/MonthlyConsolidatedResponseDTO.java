package com.market.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyConsolidatedResponseDTO(
    Long id,
    LocalDate date,
    String assetId,
    BigDecimal stock
) {}
