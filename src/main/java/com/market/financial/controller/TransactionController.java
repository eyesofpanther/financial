package com.market.financial.controller;

import com.market.financial.dto.TransactionRequestDTO;
import com.market.financial.dto.TransactionResponseDTO;
import com.market.financial.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<TransactionResponseDTO> createPurchase(@Valid @RequestBody TransactionRequestDTO dto) {
        TransactionResponseDTO response = transactionService.processPurchaseTransaction(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // NOVO: Endpoint para Vendas (Op 2, 3 e Inativação em lote)
    @PostMapping("/sale")
    public ResponseEntity<TransactionResponseDTO> createSale(@Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.processSaleTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TransactionResponseDTO> getAll() {
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> update(@PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(transactionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<TransactionResponseDTO>> getByAssetId(@PathVariable String assetId) {
        List<TransactionResponseDTO> transactions = transactionService.findByAssetId(assetId);
        return transactions.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(transactions);
    }

    @GetMapping("/operation-type/{operationTypeId}")
    public ResponseEntity<List<TransactionResponseDTO>> getByOperationTypeId(@PathVariable Integer operationTypeId) {
        List<TransactionResponseDTO> transactions = transactionService.findByOperationTypeId(operationTypeId);
        return transactions.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{active}")
    public ResponseEntity<List<TransactionResponseDTO>> getByActiveStatus(@PathVariable Integer active) {
        List<TransactionResponseDTO> transactions = transactionService.findByActive(active);
        return transactions.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(transactions);
    }

    @GetMapping("/date/{date}")
    public org.springframework.http.ResponseEntity<List<com.market.financial.dto.TransactionResponseDTO>> getTransactionsByDate(
            @PathVariable @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate date) {
        return org.springframework.http.ResponseEntity.ok(transactionService.findByDate(date));
    }

}