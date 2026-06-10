package com.market.financial.controller;

import com.market.financial.dto.OperationTypeRequestDTO;
import com.market.financial.dto.OperationTypeResponseDTO;
import com.market.financial.service.OperationTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operation-types")
public class OperationTypeController {

    // 1. Atributo final e sem @Autowired de campo
    private final OperationTypeService service;

    // 2. Construtor explícito para injeção via Spring
    public OperationTypeController(OperationTypeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OperationTypeResponseDTO>> getAll() {
        List<OperationTypeResponseDTO> list = service.findAll()
                .stream()
                .map(OperationTypeResponseDTO::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationTypeResponseDTO> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(entity -> ResponseEntity.ok(new OperationTypeResponseDTO(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OperationTypeResponseDTO> create(@Valid @RequestBody OperationTypeRequestDTO dto) {
        var savedEntity = service.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new OperationTypeResponseDTO(savedEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperationTypeResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody OperationTypeRequestDTO dto) {
        try {
            var updatedEntity = service.update(id, dto);
            return ResponseEntity.ok(new OperationTypeResponseDTO(updatedEntity));
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
