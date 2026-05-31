package com.market.financial.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.market.financial.dto.AssetPositionDTO;
import com.market.financial.dto.PortfolioConsolidatedDTO;
import com.market.financial.dto.PortfolioConsolidatedSummaryDTO;
import com.market.financial.dto.PortfolioSummaryDTO;
import com.market.financial.dto.PortfolioTotalDTO; // 1. Importação do DTO correto adicionada
import com.market.financial.service.PortfolioService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/portfolio")
@Validated
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    // 2. CORREÇÃO DO 404: Cria o endpoint para responder na rota base (/api/portfolio)
    // Retorna a visão geral (Totais + Lista de Ativos)
    @GetMapping({"", "/"})
    public ResponseEntity<PortfolioSummaryDTO> getPortfolioFullSummary() {
        PortfolioSummaryDTO summary = portfolioService.calculatePortfolioSummary();
        return ResponseEntity.ok(summary);
    }

    // 3. CORREÇÃO DA INVERSÃO: Altera o retorno para PortfolioTotalDTO 
    // e chama a função correta (calculatePortfolioTotals) que traz apenas as 4 métricas
    @GetMapping("/summary")
    public ResponseEntity<PortfolioTotalDTO> getPortfolioSummary() {
        PortfolioTotalDTO totals = portfolioService.calculatePortfolioTotals();
        return ResponseEntity.ok(totals);
    }

    @GetMapping("/position/{assetId}")
    public ResponseEntity<AssetPositionDTO> getAssetPosition(
            @PathVariable @NotBlank(message = "Asset ID is required") String assetId) {
        AssetPositionDTO position = portfolioService.calculateAssetPosition(assetId);
        return ResponseEntity.ok(position);
    }

    // --- NEW ENDPOINTS TRANSLATED AND STANDARDIZED ---

    @GetMapping("/consolidado/{assetId}")
    public ResponseEntity<List<PortfolioConsolidatedDTO>> getConsolidatedHistory(
            @PathVariable @NotBlank(message = "Asset ID is required") String assetId) {
        List<PortfolioConsolidatedDTO> history = portfolioService.getConsolidatedHistoryByAsset(assetId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/consolidado")
    public ResponseEntity<PortfolioConsolidatedSummaryDTO> getConsolidatedSummary(
            @RequestParam("date") @NotNull(message = "Date parameter is required") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PortfolioConsolidatedSummaryDTO summary = portfolioService.getConsolidatedSummaryByDate(date);
        return ResponseEntity.ok(summary);
    }
}
