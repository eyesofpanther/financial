package com.market.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortfolioConsolidatedDTO(
    LocalDate date,
    String assetId,
    BigDecimal shareQuantity,
    BigDecimal priceOnDate,
    BigDecimal equityValue
) {}