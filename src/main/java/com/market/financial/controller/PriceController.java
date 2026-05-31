package com.market.financial.controller;

import com.market.financial.dto.PriceRequestDTO;
import com.market.financial.dto.PriceResponseDTO;
import com.market.financial.service.PriceService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
    public List<PriceResponseDTO> getAllPrices() {
        return priceService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceResponseDTO> getPriceById(@PathVariable Long id) {
        return priceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<PriceResponseDTO>> getPricesByAssetId(@PathVariable String assetId) {
        List<PriceResponseDTO> prices = priceService.findByAssetId(assetId);
        if (prices.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 se não houver preços para o ativo
        }
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/search")
    public ResponseEntity<PriceResponseDTO> getPriceByAssetAndDate(
            @RequestParam String assetId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        PriceResponseDTO response = priceService.findByAssetIdAndDate(assetId, date);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PriceResponseDTO> createPrice(
            @jakarta.validation.Valid @RequestBody PriceRequestDTO request) {
        return ResponseEntity.ok(priceService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceResponseDTO> updatePrice(@PathVariable Long id,
            @jakarta.validation.Valid @RequestBody PriceRequestDTO request) {
        try {
            return ResponseEntity.ok(priceService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrice(@PathVariable Long id) {
        try {
            priceService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/date/{date}")
    public org.springframework.http.ResponseEntity<List<com.market.financial.dto.PriceResponseDTO>> getPricesByDate(
            @PathVariable @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate date) {
        return org.springframework.http.ResponseEntity.ok(priceService.findByDate(date));
    }
}
