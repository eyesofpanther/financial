package com.market.financial.controller;

import com.market.financial.dto.AssetRequestDTO;
import com.market.financial.dto.AssetResponseDTO;
import com.market.financial.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    // 1. Atributo final e sem @Autowired de campo
    private final AssetService service;

    // 2. Construtor explícito para injeção limpa pelo Spring
    public AssetController(AssetService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssetResponseDTO>> getAll() {
        List<AssetResponseDTO> list = service.findAll().stream()
                .map(AssetResponseDTO::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> getById(@PathVariable String id) {
        return service.findById(id)
                .map(entity -> ResponseEntity.ok(new AssetResponseDTO(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetResponseDTO> create(@Valid @RequestBody AssetRequestDTO dto) {
        try {
            var saved = service.save(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new AssetResponseDTO(saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> update(@PathVariable String id, @Valid @RequestBody AssetRequestDTO dto) {
        try {
            var updated = service.update(id, dto);
            return ResponseEntity.ok(new AssetResponseDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
