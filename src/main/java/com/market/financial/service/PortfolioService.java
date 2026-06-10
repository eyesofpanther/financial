package com.market.financial.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.market.financial.dto.AssetPositionDTO;
import com.market.financial.dto.PortfolioConsolidatedDTO;
import com.market.financial.dto.PortfolioConsolidatedSummaryDTO;
import com.market.financial.dto.PortfolioSummaryDTO;
import com.market.financial.dto.PortfolioTotalDTO; // Importação adicionada
import com.market.financial.model.MonthlyConsolidated;
import com.market.financial.model.Price;
import com.market.financial.model.Transaction;
import com.market.financial.repository.AssetRepository;
import com.market.financial.repository.MonthlyConsolidatedRepository;
import com.market.financial.repository.PriceRepository;
import com.market.financial.repository.TransactionRepository;

@Service
public class PortfolioService {

        private final TransactionRepository transactionRepository;
        private final AssetRepository assetRepository;
        private final PriceRepository priceRepository;
        private final MonthlyConsolidatedRepository consolidatedRepo;

        public PortfolioService(
                        TransactionRepository transactionRepository,
                        AssetRepository assetRepository,
                        PriceRepository priceRepository,
                        MonthlyConsolidatedRepository consolidatedRepo) {
                this.transactionRepository = transactionRepository;
                this.assetRepository = assetRepository;
                this.priceRepository = priceRepository;
                this.consolidatedRepo = consolidatedRepo;
        }

        public AssetPositionDTO calculateAssetPosition(String assetId) {
                List<Transaction> transactions = transactionRepository.findByAssetId(assetId);

                // 1. Total Purchase Value (Histórico Geral - Inalterado)
                BigDecimal totalPurchases = transactions.stream()
                                .filter(t -> t.getOperationType() != null
                                                && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                                .map(t -> multiply(t.getStock(), t.getUnitValue()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 2. Total Sales Value (Histórico Geral - Inalterado)
                BigDecimal totalSales = transactions.stream()
                                .filter(t -> t.getOperationType() != null
                                                && Integer.valueOf(0).equals(t.getOperationType().getIo()))
                                .map(t -> multiply(t.getStock(), t.getUnitValue()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 3. Net Invested Balance (Calculado estritamente com base nos Saldos dos Lotes
                // Ativos FIFO)
                BigDecimal netInvested;
                if ("SPAXX".equals(assetId)) {
                        // Para o caixa SPAXX, mantemos a lógica original de fluxo de caixa (Entradas -
                        // Saídas)
                        BigDecimal activePurchasesValue = transactions.stream()
                                        .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                                        .filter(t -> t.getOperationType() != null
                                                        && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                                        .map(t -> multiply(t.getStock(), t.getUnitValue()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal activeSalesValue = transactions.stream()
                                        .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                                        .filter(t -> t.getOperationType() != null
                                                        && Integer.valueOf(0).equals(t.getOperationType().getIo()))
                                        .map(t -> multiply(t.getStock(), t.getUnitValue()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        netInvested = activePurchasesValue.subtract(activeSalesValue);
                } else {
                        // Para Ações e FIIs, usamos o modelo FIFO estrito baseado no saldo dos lotes
                        // ativos
                        netInvested = transactions.stream()
                                        .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                                        .filter(t -> t.getOperationType() != null
                                                        && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                                        .map(t -> multiply(t.getAvailableQuantity(), t.getUnitValue()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                }

                // --- QUANTIDADE FÍSICA ATUAL DE COTAS (CURRENT STOCK) ---
                // Agora reflete apenas o saldo disponível somado dos lotes de compra ativos
                BigDecimal currentStock;
                if ("SPAXX".equals(assetId)) {
                        // Para o caixa SPAXX, a quantidade é o saldo líquido financeiro histórico ativo
                        BigDecimal stockPurchased = transactions.stream()
                                        .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                                        .filter(t -> t.getOperationType() != null
                                                        && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                                        .map(Transaction::getStock)
                                        .filter(Objects::nonNull)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal stockSold = transactions.stream()
                                        .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                                        .filter(t -> t.getOperationType() != null
                                                        && Integer.valueOf(0).equals(t.getOperationType().getIo()))
                                        .map(Transaction::getStock)
                                        .filter(Objects::nonNull)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        currentStock = stockPurchased.subtract(stockSold);
                } else {
                        // Para ativos normais, lê diretamente a nova coluna disponível controlada pelo
                        // FIFO
                        currentStock = transactions.stream()
                                        .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                                        .filter(t -> t.getOperationType() != null
                                                        && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                                        .map(Transaction::getAvailableQuantity)
                                        .filter(Objects::nonNull)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                }

                // --- BUSCA CRONOLÓGICA DE PREÇOS (DADOS DE MERCADO - Inalterado) ---
                LocalDate today = LocalDate.now();
                BigDecimal lastPrice = priceRepository
                                .findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(assetId, today)
                                .map(Price::getPrice)
                                .orElse(BigDecimal.ZERO);

                BigDecimal currentMarketValue = currentStock.multiply(lastPrice);

                RoundingMode rm = RoundingMode.HALF_UP;
                BigDecimal profitOrLoss = currentMarketValue.subtract(netInvested);

                BigDecimal profitability = BigDecimal.ZERO;
                if (netInvested.compareTo(BigDecimal.ZERO) > 0) {
                        profitability = currentMarketValue
                                        .divide(netInvested, 4, rm)
                                        .subtract(BigDecimal.ONE)
                                        .multiply(BigDecimal.valueOf(100));
                }

                return new AssetPositionDTO(
                                assetId,
                                totalPurchases.setScale(2, rm),
                                totalSales.setScale(2, rm),
                                netInvested.setScale(2, rm),
                                currentStock.setScale(4, rm),
                                lastPrice.setScale(2, rm),
                                currentMarketValue.setScale(2, rm),
                                profitOrLoss.setScale(2, rm),
                                profitability.setScale(2, rm));
        }

        private BigDecimal multiply(BigDecimal stock, BigDecimal unitValue) {
                if (stock == null || unitValue == null) {
                        return BigDecimal.ZERO;
                }
                return stock.multiply(unitValue);
        }

        public PortfolioSummaryDTO calculatePortfolioSummary() {
                List<com.market.financial.model.Asset> allAssets = assetRepository.findAll();
                List<AssetPositionDTO> activePositions = allAssets.stream()
                                .map(asset -> calculateAssetPosition(asset.getId()))
                                .filter(pos -> pos.netInvested() != null
                                                && pos.netInvested().compareTo(BigDecimal.ZERO) > 0)
                                .toList();

                BigDecimal grandTotalInvested = activePositions.stream()
                                .map(AssetPositionDTO::netInvested)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal grandTotalMarketValue = activePositions.stream()
                                .map(AssetPositionDTO::currentMarketValue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                RoundingMode rm = RoundingMode.HALF_UP;

                return new PortfolioSummaryDTO(
                                grandTotalInvested.setScale(2, rm),
                                grandTotalMarketValue.setScale(2, rm),
                                activePositions);
        }

        // --- NOVO MÉTODO PARA RETORNAR APENAS AS 4 MÉTRICAS DE TOTALIZADORES ---
        public PortfolioTotalDTO calculatePortfolioTotals() {
                PortfolioSummaryDTO summary = calculatePortfolioSummary();
                RoundingMode rm = RoundingMode.HALF_UP;

                BigDecimal grandTotalInvested = summary.grandTotalInvested();
                BigDecimal grandTotalMarketValue = summary.grandTotalMarketValue();

                // Lucro/Prejuízo Absoluto = Valor de Mercado Total - Custo Total Investido
                BigDecimal profitOrLoss = grandTotalMarketValue.subtract(grandTotalInvested);

                // Rentabilidade Percentual (%) = ((Valor de Mercado Total / Custo Total) - 1) *
                // 100
                BigDecimal profitability = BigDecimal.ZERO;
                if (grandTotalInvested.compareTo(BigDecimal.ZERO) > 0) {
                        profitability = grandTotalMarketValue
                                        .divide(grandTotalInvested, 4, rm)
                                        .subtract(BigDecimal.ONE)
                                        .multiply(BigDecimal.valueOf(100));
                }

                return new PortfolioTotalDTO(
                                grandTotalInvested.setScale(2, rm),
                                grandTotalMarketValue.setScale(2, rm),
                                profitOrLoss.setScale(2, rm),
                                profitability.setScale(2, rm));
        }

        public BigDecimal calculateGrandTotalOnly() {
                List<com.market.financial.model.Asset> allAssets = assetRepository.findAll();
                return allAssets.stream()
                                .map(asset -> calculateAssetPosition(asset.getId()).netInvested())
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .setScale(2, RoundingMode.HALF_UP);
        }

        public BigDecimal calculateGrandTotalMarketOnly() {
                List<com.market.financial.model.Asset> allAssets = assetRepository.findAll();
                return allAssets.stream()
                                .map(asset -> calculateAssetPosition(asset.getId()).currentMarketValue())
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .setScale(2, RoundingMode.HALF_UP);
        }

        // --- INTEGRATED INTELLIGENCE LOGIC WITH MONTHLY HISTORY ---
        @Transactional(readOnly = true)
        public List<PortfolioConsolidatedDTO> getConsolidatedHistoryByAsset(String assetId) {
                List<MonthlyConsolidated> consolidatedRecords = consolidatedRepo.findByAssetId(assetId);

                return consolidatedRecords.stream().map(consolidated -> {
                        LocalDate consolidatedDate = consolidated.getDate();
                        var priceOptional = priceRepository.findByAssetIdAndDate(assetId, consolidatedDate);

                        BigDecimal priceOnDate = priceOptional.map(Price::getPrice).orElse(BigDecimal.ZERO);
                        BigDecimal shareQuantity = consolidated.getStock();
                        BigDecimal equityValue = shareQuantity.multiply(priceOnDate);

                        return new PortfolioConsolidatedDTO(
                                        consolidatedDate,
                                        assetId,
                                        shareQuantity,
                                        priceOnDate,
                                        equityValue);
                }).collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public PortfolioConsolidatedSummaryDTO getConsolidatedSummaryByDate(LocalDate date) {
                // Correção 1: Garantia de tipagem forte na busca do repositório
                List<MonthlyConsolidated> consolidatedRecords = consolidatedRepo.findByDate(date);
                RoundingMode rm = RoundingMode.HALF_UP;

                // Correção 2: Adicionada tipagem explícita <PortfolioConsolidatedDTO> na List e
                // no stream
                List<PortfolioConsolidatedDTO> assetList = consolidatedRecords.stream()
                                .filter(c -> c.getAsset() != null)
                                .map(consolidated -> {
                                        String assetId = consolidated.getAsset().getId();
                                        var priceOptional = priceRepository.findByAssetIdAndDate(assetId, date);

                                        BigDecimal priceOnDate = priceOptional.map(Price::getPrice)
                                                        .orElse(BigDecimal.ZERO);
                                        BigDecimal shareQuantity = consolidated.getStock();
                                        BigDecimal equityValue = shareQuantity.multiply(priceOnDate);

                                        return new PortfolioConsolidatedDTO(
                                                        date,
                                                        assetId,
                                                        shareQuantity,
                                                        priceOnDate.setScale(2, rm),
                                                        equityValue.setScale(2, rm));
                                })
                                .collect(Collectors.toList());

                // Correção 3: Mapeamento direto usando o record em inglês .equityValue()
                BigDecimal totalEquityValue = assetList.stream()
                                .map(PortfolioConsolidatedDTO::equityValue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 3. Retorna o DTO com todos os dados tipados corretamente
                return new PortfolioConsolidatedSummaryDTO(
                                date,
                                totalEquityValue.setScale(2, rm),
                                assetList);
        }

}