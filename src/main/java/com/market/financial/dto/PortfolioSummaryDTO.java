package com.market.financial.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummaryDTO(
    BigDecimal grandTotalInvested,
    BigDecimal grandTotalMarketValue,
    List<AssetPositionDTO> assetPositions
) {}