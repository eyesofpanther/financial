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
import com.market.financial.dto.PortfolioConsolidadoDTO;
import com.market.financial.dto.PortfolioConsolidadoDataSummaryDTO;
import com.market.financial.dto.PortfolioSummaryDTO;
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
        private final PriceRepository priceRepository; // Usado para ambas as lógicas de preço
        private final MonthlyConsolidatedRepository consolidatedRepo;

        // CONSTRUTOR ÚNICO: Injeta todos os 4 repositórios necessários de uma vez só
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

                // 1. Valor Total Compras (Histórico Geral)
                BigDecimal totalPurchases = transactions.stream()
                                .filter(t -> t.getOperationType() != null
                                                && Integer.valueOf(1).equals(t.getOperationType().getIo()))
                                .map(t -> multiply(t.getStock(), t.getUnitValue()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 2. Valor Total Vendas (Histórico Geral)
                BigDecimal totalSales = transactions.stream()
                                .filter(t -> t.getOperationType() != null
                                                && Integer.valueOf(0).equals(t.getOperationType().getIo()))
                                .map(t -> multiply(t.getStock(), t.getUnitValue()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 3. Saldo Total Investido (Apenas Transações com Active = 1)
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

                BigDecimal netInvested = activePurchasesValue.subtract(activeSalesValue);

                // --- QUANTIDADE FÍSICA DE AÇÕES (STOCK ATUAL) ---
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

                BigDecimal currentStock = stockPurchased.subtract(stockSold);

                // --- BUSCA CRONOLÓGICA DE PREÇO (MARKET DATA) ---
                LocalDate today = LocalDate.now();
                BigDecimal lastPrice = priceRepository
                                .findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(assetId, today)
                                .map(Price::getPrice)
                                .orElse(BigDecimal.ZERO);

                // Valor da posição atualizado a mercado (Quantidade * Último Preço)
                BigDecimal currentMarketValue = currentStock.multiply(lastPrice);
                RoundingMode rm = RoundingMode.HALF_UP;

                // Lucro/Prejuízo Individual = Valor de Mercado - Custo Líquido Investido
                BigDecimal profitOrLoss = currentMarketValue.subtract(netInvested);

                // Rentabilidade Individual % = ((Valor de Mercado / Custo Líquido) - 1) * 100
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

        // --- NOVA LÓGICA DE INTELIGÊNCIA INTEGRADA COM HISTÓRICO MENSAL ---
        @Transactional(readOnly = true)
        public List<PortfolioConsolidadoDTO> getHistoricoConsolidadoPorAtivo(String assetId) {
                List<MonthlyConsolidated> consolidados = consolidatedRepo.findByAssetId(assetId);

                return consolidados.stream().map(consolidado -> {
                        LocalDate dataConsolidado = consolidado.getDate();

                        // Usando o priceRepository unificado que foi injetado no início da classe
                        var priceOptional = priceRepository.findByAssetIdAndDate(assetId, dataConsolidado);
                        BigDecimal precoNaData = BigDecimal.ZERO;

                        if (priceOptional.isPresent()) {
                                precoNaData = priceOptional.get().getPrice();
                        }

                        BigDecimal quantidadeCotas = consolidado.getStock();
                        BigDecimal valorPatrimonial = quantidadeCotas.multiply(precoNaData);

                        return new PortfolioConsolidadoDTO(
                                        dataConsolidado,
                                        assetId,
                                        quantidadeCotas,
                                        precoNaData,
                                        valorPatrimonial);
                }).collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public PortfolioConsolidadoDataSummaryDTO getConsolidadoPorData(LocalDate data) {
                List<MonthlyConsolidated> consolidados = consolidatedRepo.findByDate(data);
                RoundingMode rm = RoundingMode.HALF_UP;

                // 1. Mapeia e gera a lista com os dados individuais por ativo
                List<PortfolioConsolidadoDTO> ativosList = consolidados.stream()
                                .filter(c -> c.getAsset() != null)
                                .map(consolidado -> {
                                        String assetId = consolidado.getAsset().getId();

                                        var priceOptional = priceRepository.findByAssetIdAndDate(assetId, data);
                                        BigDecimal precoNaData = BigDecimal.ZERO;

                                        if (priceOptional.isPresent()) {
                                                precoNaData = priceOptional.get().getPrice();
                                        }

                                        BigDecimal quantidadeCotas = consolidado.getStock();
                                        BigDecimal valorPatrimonial = quantidadeCotas.multiply(precoNaData);

                                        return new PortfolioConsolidadoDTO(
                                                        data,
                                                        assetId,
                                                        quantidadeCotas,
                                                        precoNaData.setScale(2, rm),
                                                        valorPatrimonial.setScale(2, rm));
                                }).collect(Collectors.toList());

                // 2. Soma o 'valorPatrimonialNaData' de cada ativo para obter o total geral da
                // carteira
                BigDecimal valorTotalPatrimonial = ativosList.stream()
                                .map(PortfolioConsolidadoDTO::valorPatrimonialNaData) // Certifique-se de que o record
                                                                                      // possui este método/campo gerado
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 3. Retorna o DTO envelopado completo
                return new PortfolioConsolidadoDataSummaryDTO(
                                data,
                                valorTotalPatrimonial.setScale(2, rm),
                                ativosList);
        }

}