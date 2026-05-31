package com.market.financial.dto;

import java.math.BigDecimal;

public record AssetPositionDTO(
        String assetId,
        BigDecimal totalPurchases, // Valor Total Compras (IO = 1)
        BigDecimal totalSales, // Valor Total Vendas (IO = 0)
        BigDecimal netInvested, // Saldo Total Investido (Active = 1)
        BigDecimal currentStock, // Quantidade atual de cotas
        BigDecimal lastPrice, // Última cotação disponível
        BigDecimal currentMarketValue, // Valor atualizado a mercado
        BigDecimal profitOrLoss, // Lucro/Prejuízo individual
        BigDecimal profitability // Rentabilidade % individual
) {
}
