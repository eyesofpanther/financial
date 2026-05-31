package com.market.financial.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.market.financial.dto.AssetPositionDTO;
import com.market.financial.dto.PortfolioConsolidadoDTO;
import com.market.financial.dto.PortfolioConsolidadoDataSummaryDTO;
import com.market.financial.dto.PortfolioSummaryDTO;
import com.market.financial.dto.PortfolioTotalDTO;
import com.market.financial.service.PortfolioService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/portfolio")
@Validated // Ativa a validação do @NotBlank nos parâmetros de rota
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<AssetPositionDTO> getAssetPosition(
            @PathVariable @NotBlank(message = "O ID do ativo é obrigatório") String assetId) {
        return ResponseEntity.ok(portfolioService.calculateAssetPosition(assetId));
    }

    @GetMapping
    public ResponseEntity<PortfolioSummaryDTO> getPortfolioSummary() {
        return ResponseEntity.ok(portfolioService.calculatePortfolioSummary());
    }

    @GetMapping("/total")
    public ResponseEntity<PortfolioTotalDTO> getPortfolioTotals() {
        BigDecimal totalInvested = portfolioService.calculateGrandTotalOnly();
        BigDecimal totalMarket = portfolioService.calculateGrandTotalMarketOnly();

        // Formula 1: Lucro/Prejuízo = Valor de Mercado - Valor Investido
        BigDecimal profitOrLoss = totalMarket.subtract(totalInvested);

        // Formula 2: Rentabilidade % = ((Valor de Mercado / Valor Investido) - 1) * 100
        BigDecimal profitability = BigDecimal.ZERO;
        RoundingMode rm = RoundingMode.HALF_UP;

        // Proteção estrita contra divisão por zero (caso a carteira esteja vazia)
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            profitability = totalMarket
                    .divide(totalInvested, 4, rm) // Divide com 4 casas para precisão intermediária
                    .subtract(BigDecimal.ONE)
                    .multiply(BigDecimal.valueOf(100));
        }

        return ResponseEntity.ok(new PortfolioTotalDTO(
                totalInvested.setScale(2, rm),
                totalMarket.setScale(2, rm),
                profitOrLoss.setScale(2, rm),
                profitability.setScale(2, rm) // Retorna a % final bonitinha com 2 casas
        ));
    }

    // --- ROTA ATUALIZADA E CORRIGIDA PARA O HISTÓRICO CONSOLIDADO MENSAL ---
    @GetMapping("/consolidado/{assetId}")
    public ResponseEntity<List<PortfolioConsolidadoDTO>> obterConsolidadoHistorico(
            @PathVariable @NotBlank(message = "O ID do ativo é obrigatório") String assetId) {
        List<PortfolioConsolidadoDTO> historico = portfolioService.getHistoricoConsolidadoPorAtivo(assetId);
        return ResponseEntity.ok(historico);
    }

    // --- ROTA ATUALIZADA COM ENVELOPAMENTO E VALOR TOTAL SOMADO ---
    @GetMapping("/consolidado")
    public ResponseEntity<PortfolioConsolidadoDataSummaryDTO> obterConsolidadoPorData(
            @org.springframework.web.bind.annotation.RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date) {

        PortfolioConsolidadoDataSummaryDTO resumo = portfolioService.getConsolidadoPorData(date);
        return ResponseEntity.ok(resumo);
    }

}
