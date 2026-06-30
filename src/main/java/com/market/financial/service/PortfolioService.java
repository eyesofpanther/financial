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
        RoundingMode rm = RoundingMode.HALF_UP;

        // 1. Total Purchase Value (Blindagem de nulos no objeto Transaction e no
        // reduce)
        BigDecimal totalPurchases = transactions.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getOperationType() != null && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                .map(t -> (t.getStock() == null || t.getUnitValue() == null) ? BigDecimal.ZERO
                        : t.getStock().multiply(t.getUnitValue()))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        // 2. Total Sales Value
        BigDecimal totalSales = transactions.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getOperationType() != null && Integer.valueOf(0).equals(t.getOperationType().getIo()))
                .map(t -> (t.getStock() == null || t.getUnitValue() == null) ? BigDecimal.ZERO
                        : t.getStock().multiply(t.getUnitValue()))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        // 3. Net Invested Balance
        BigDecimal netInvested;
        if ("SPAXX".equals(assetId)) {
            BigDecimal activePurchasesValue = transactions.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                    .filter(t -> t.getOperationType() != null
                            && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                    .map(t -> (t.getStock() == null || t.getUnitValue() == null) ? BigDecimal.ZERO
                            : t.getStock().multiply(t.getUnitValue()))
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            BigDecimal activeSalesValue = transactions.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                    .filter(t -> t.getOperationType() != null
                            && Integer.valueOf(0).equals(t.getOperationType().getIo()))
                    .map(t -> (t.getStock() == null || t.getUnitValue() == null) ? BigDecimal.ZERO
                            : t.getStock().multiply(t.getUnitValue()))
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            netInvested = activePurchasesValue.subtract(activeSalesValue);
        } else {
            netInvested = transactions.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                    .filter(t -> t.getOperationType() != null
                            && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                    .map(t -> (t.getAvailableQuantity() == null || t.getUnitValue() == null) ? BigDecimal.ZERO
                            : t.getAvailableQuantity().multiply(t.getUnitValue()))
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        }

        // --- QUANTIDADE FÍSICA ATUAL DE COTAS (CURRENT STOCK) ---
        BigDecimal currentStock;
        if ("SPAXX".equals(assetId)) {
            BigDecimal stockPurchased = transactions.stream()
                    .filter(Objects::nonNull) // Garante que a transação não é nula antes do map
                    .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                    .filter(t -> t.getOperationType() != null
                            && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                    .map(t -> t.getStock()) // Lambda explícita elimina o warning de 'this'
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            BigDecimal stockSold = transactions.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                    .filter(t -> t.getOperationType() != null
                            && Integer.valueOf(0).equals(t.getOperationType().getIo()))
                    .map(t -> t.getStock())
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            currentStock = stockPurchased.subtract(stockSold);
        } else {
            currentStock = transactions.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> Integer.valueOf(1).equals(t.getActive()))
                    .filter(t -> t.getOperationType() != null
                            && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                    .map(t -> t.getAvailableQuantity())
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        }

        // --- BUSCA CRONOLÓGICA DE PREÇOS ---
        LocalDate today = LocalDate.now();
        BigDecimal lastPrice = priceRepository
                .findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(assetId, today)
                .map(p -> p.getPrice()) // Lambda explicita para segurança de tipo
                .orElse(BigDecimal.ZERO);

        BigDecimal currentMarketValue = currentStock.multiply(lastPrice);
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

    public PortfolioSummaryDTO calculatePortfolioSummary() {
        List<com.market.financial.model.Asset> allAssets = assetRepository.findAll();

        // Filtra apenas posições que possuem estoque atualizado ou saldo investido
        // ativo
        List<AssetPositionDTO> activePositions = allAssets.stream()
                .filter(Objects::nonNull) // Blindagem contra objetos nulos na lista
                .map(asset -> calculateAssetPosition(asset.getId()))
                .filter(Objects::nonNull) // Garante que o DTO gerado não é nulo
                .filter(pos -> {
                    BigDecimal stock = pos.currentStock();
                    BigDecimal invested = pos.netInvested();
                    return (stock != null && stock.compareTo(BigDecimal.ZERO) > 0) ||
                            (invested != null && invested.compareTo(BigDecimal.ZERO) > 0);
                })
                .toList();

        // Soma os totais usando lambdas puras e seguras contra nulos
        BigDecimal grandTotalInvested = activePositions.stream()
                .filter(Objects::nonNull)
                .map(pos -> pos.netInvested())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        BigDecimal grandTotalMarketValue = activePositions.stream()
                .filter(Objects::nonNull)
                .map(pos -> pos.currentMarketValue())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        RoundingMode rm = RoundingMode.HALF_UP;

        return new PortfolioSummaryDTO(
                grandTotalInvested.setScale(2, rm),
                grandTotalMarketValue.setScale(2, rm),
                activePositions);
    }

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
                .filter(Objects::nonNull) // Blindagem inicial contra nulos
                .map(asset -> calculateAssetPosition(asset.getId()).netInvested())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b)) // Lambda explícita para o acumulador
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateGrandTotalMarketOnly() {
        List<com.market.financial.model.Asset> allAssets = assetRepository.findAll();
        return allAssets.stream()
                .filter(Objects::nonNull) // Blindagem inicial contra nulos
                .map(asset -> calculateAssetPosition(asset.getId()).currentMarketValue())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b)) // Lambda explícita para o acumulador
                .setScale(2, RoundingMode.HALF_UP);
    }

    // --- LÓGICA DE HISTÓRICO MENSAL CONSOLIDADO (EOD) ---
    @Transactional(readOnly = true)
    public List<PortfolioConsolidatedDTO> getConsolidatedHistoryByAsset(String assetId) {
        List<MonthlyConsolidated> consolidatedRecords = consolidatedRepo.findByAssetId(assetId);

        return consolidatedRecords.stream()
                .filter(Objects::nonNull)
                .map(consolidated -> {
                    LocalDate consolidatedDate = consolidated.getDate();
                    var priceOptional = priceRepository.findByAssetIdAndDate(assetId, consolidatedDate);

                    // Correção do Warning: Lambda explícita em vez de Price::getPrice
                    BigDecimal priceOnDate = "SPAXX".equals(assetId) ? BigDecimal.ONE
                            : priceOptional.map(p -> p.getPrice()).orElse(BigDecimal.ZERO);

                    BigDecimal shareQuantity = consolidated.getStock();
                    BigDecimal equityValue = (shareQuantity == null) ? BigDecimal.ZERO
                            : shareQuantity.multiply(priceOnDate);

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
        List<MonthlyConsolidated> consolidatedRecords = consolidatedRepo.findByDate(date);
        RoundingMode rm = RoundingMode.HALF_UP;

        List<PortfolioConsolidatedDTO> assetList = consolidatedRecords.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getAsset() != null)
                .map(consolidated -> {
                    String assetId = consolidated.getAsset().getId();
                    var priceOptional = priceRepository.findByAssetIdAndDate(assetId, date);

                    // Correção do Warning: Lambda explícita em vez de Price::getPrice
                    BigDecimal priceOnDate = "SPAXX".equals(assetId) ? BigDecimal.ONE
                            : priceOptional.map(p -> p.getPrice()).orElse(BigDecimal.ZERO);
                    BigDecimal shareQuantity = consolidated.getStock();
                    BigDecimal equityValue = (shareQuantity == null) ? BigDecimal.ZERO
                            : shareQuantity.multiply(priceOnDate);

                    return new PortfolioConsolidatedDTO(
                            date,
                            assetId,
                            shareQuantity,
                            priceOnDate.setScale(2, rm),
                            equityValue.setScale(2, rm));
                })
                .collect(Collectors.toList());

        // Correção do Warning: Filtragem explícita e lambda na soma eliminam o erro de
        // Null type safety
        BigDecimal totalEquityValue = assetList.stream()
                .filter(Objects::nonNull)
                .map(dto -> dto.equityValue()) // Lambda explícita no mapeamento de DTO
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        return new PortfolioConsolidatedSummaryDTO(
                date,
                totalEquityValue.setScale(2, rm),
                assetList);
    }
}