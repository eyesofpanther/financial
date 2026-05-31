package com.market.financial.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummaryDTO(
    BigDecimal grandTotalInvested,      // Valor de Custo Total
    BigDecimal grandTotalMarketValue,   // NOVO: Valor Total Atualizado a Mercado
    List<AssetPositionDTO> assetPositions
) {}
