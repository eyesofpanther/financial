package com.market.financial.controller;

import com.market.financial.dto.AssetTypeRequestDTO;
import com.market.financial.dto.AssetTypeResponseDTO;
import com.market.financial.service.AssetTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-types")
public class AssetTypeController {

    // 1. Atributo final e sem @Autowired de campo
    private final AssetTypeService service;

    // 2. Construtor explícito para injeção via Spring
    public AssetTypeController(AssetTypeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssetTypeResponseDTO>> getAll() {
        List<AssetTypeResponseDTO> list = service.findAll().stream()
                .map(AssetTypeResponseDTO::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetTypeResponseDTO> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(entity -> ResponseEntity.ok(new AssetTypeResponseDTO(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetTypeResponseDTO> create(@Valid @RequestBody AssetTypeRequestDTO dto) {
        var saved = service.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AssetTypeResponseDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetTypeResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody AssetTypeRequestDTO dto) {
        try {
            var updated = service.update(id, dto);
            return ResponseEntity.ok(new AssetTypeResponseDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
