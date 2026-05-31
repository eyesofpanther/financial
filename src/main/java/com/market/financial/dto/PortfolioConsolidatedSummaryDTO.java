package com.market.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PortfolioConsolidatedSummaryDTO(
    LocalDate date,
    BigDecimal totalEquityValue,
    List<PortfolioConsolidatedDTO> assets
) {}