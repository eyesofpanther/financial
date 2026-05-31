package com.market.financial.dto;

import java.math.BigDecimal;

public record PortfolioTotalDTO(
        BigDecimal grandTotalInvested,
        BigDecimal grandTotalMarketValue,
        BigDecimal profitOrLoss, // Lucro/Prejuízo Absoluto
        BigDecimal profitability // Rentabilidade Percentual (%)
) {
}