package com.market.financial.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
import com.market.financial.dto.PortfolioTotalDTO;
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

    @GetMapping({"", "/"})
    public ResponseEntity<PortfolioSummaryDTO> getPortfolioFullSummary() {
        PortfolioSummaryDTO summary = portfolioService.calculatePortfolioSummary();
        return ResponseEntity.ok(summary);
    }

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

    // --- ENDPOINTS DE HISTÓRICO CONSOLIDADO UNIFICADOS E SEGUROS ---

    /**
     * Endpoint dinâmico que aceita tanto o ID do ativo (ex: IBM) quanto uma data (ex: 2026-06-30).
     * Resolve o problema de colisão de rotas no Spring Boot.
     */
    @GetMapping("/consolidado/{parametro}")
    public ResponseEntity<List<PortfolioConsolidatedDTO>> getConsolidatedHistory(
            @PathVariable @NotBlank(message = "Parameter (Asset ID or Date) is required") String parametro) {
        try {
            // Tenta interpretar o parâmetro recebido na URL como uma data válida (AAAA-MM-DD)
            LocalDate data = LocalDate.parse(parametro);
            
            // Se der certo, chama a nova lógica de histórico por data que criamos na Service
            List<PortfolioConsolidatedDTO> historyByDate = portfolioService.getConsolidatedHistoryByDate(data);
            return ResponseEntity.ok(historyByDate);
            
        } catch (DateTimeParseException e) {
            // Se falhar a conversão para data, assume que é o ID de um ativo (ex: IBM, SPAXX)
            List<PortfolioConsolidatedDTO> historyByAsset = portfolioService.getConsolidatedHistoryByAsset(parametro);
            return ResponseEntity.ok(historyByAsset);
        }
    }

    /**
     * Mantido por retrocompatibilidade caso queira consultar o Objeto Sumário completo via Query Param.
     * Chamada: /api/portfolio/consolidado?date=2026-06-30
     */
    @GetMapping("/consolidado")
    public ResponseEntity<PortfolioConsolidatedSummaryDTO> getConsolidatedSummary(
            @RequestParam("date") @NotNull(message = "Date parameter is required") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PortfolioConsolidatedSummaryDTO summary = portfolioService.getConsolidatedSummaryByDate(date);
        return ResponseEntity.ok(summary);
    }
}
