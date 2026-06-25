package com.market.financial.controller;

import com.market.financial.dto.PriceRequestDTO;
import com.market.financial.dto.PriceResponseDTO;
import com.market.financial.service.PriceService;
import jakarta.validation.Valid;
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
            return ResponseEntity.noContent().build();
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
    public ResponseEntity<PriceResponseDTO> createPrice(@Valid @RequestBody PriceRequestDTO request) {
        return ResponseEntity.ok(priceService.save(request));
    }

    // REMOVIDO O TRY-CATCH MASCARADOR
    @PutMapping("/{id}")
    public ResponseEntity<PriceResponseDTO> updatePrice(@PathVariable Long id, @Valid @RequestBody PriceRequestDTO request) {
        PriceResponseDTO response = priceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // REMOVIDO O TRY-CATCH MASCARADOR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrice(@PathVariable Long id) {
        priceService.delete(id);
        return ResponseEntity.noContent().build(); // Boa prática: DELETE bem-sucedido retorna 204 No Content
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<PriceResponseDTO>> getPricesByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(priceService.findByDate(date));
    }
}
