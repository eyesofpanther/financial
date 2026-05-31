package com.market.financial.controller;

import com.market.financial.dto.MonthlyConsolidatedRequestDTO;
import com.market.financial.dto.MonthlyConsolidatedResponseDTO;
import com.market.financial.service.MonthlyConsolidatedService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/monthly-consolidated")
public class MonthlyConsolidatedController {

    private final MonthlyConsolidatedService service;

    public MonthlyConsolidatedController(MonthlyConsolidatedService service) {
        this.service = service;
    }

    @GetMapping
    public List<MonthlyConsolidatedResponseDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonthlyConsolidatedResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<MonthlyConsolidatedResponseDTO>> getByAssetId(@PathVariable String assetId) {
        return ResponseEntity.ok(service.findByAssetId(assetId));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<MonthlyConsolidatedResponseDTO>> getByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(service.findByDate(date));
    }

    @PostMapping
    public ResponseEntity<MonthlyConsolidatedResponseDTO> create(@Valid @RequestBody MonthlyConsolidatedRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonthlyConsolidatedResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MonthlyConsolidatedRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
