package com.market.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceResponseDTO(
    Long idPrice,
    LocalDate date,
    String assetId,
    BigDecimal price
) {}
