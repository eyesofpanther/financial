package com.market.financial.service;

import com.market.financial.dto.TransactionRequestDTO;
import com.market.financial.dto.TransactionResponseDTO;
import com.market.financial.infra.exception.ResourceNotFoundException;
import com.market.financial.model.Asset;
import com.market.financial.model.OperationType;
import com.market.financial.model.Transaction;
import com.market.financial.repository.AssetRepository;
import com.market.financial.repository.OperationTypeRepository;
import com.market.financial.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final OperationTypeRepository operationTypeRepository;

    public TransactionService(TransactionRepository transactionRepository,
            AssetRepository assetRepository,
            OperationTypeRepository operationTypeRepository) {
        this.transactionRepository = transactionRepository;
        this.assetRepository = assetRepository;
        this.operationTypeRepository = operationTypeRepository;
    }

    public List<TransactionResponseDTO> findAll() {
        return transactionRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public TransactionResponseDTO findById(Long id) {
        return transactionRepository.findById(id)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));
    }

    public TransactionResponseDTO save(TransactionRequestDTO request) {
        Transaction transaction = new Transaction();
        mapRequestToEntity(request, transaction);
        return convertToResponseDTO(transactionRepository.save(transaction));
    }

    public TransactionResponseDTO update(Long id, TransactionRequestDTO request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));
        mapRequestToEntity(request, transaction);
        return convertToResponseDTO(transactionRepository.save(transaction));
    }

    public void delete(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));
        transactionRepository.delete(transaction);
    }

    private void mapRequestToEntity(TransactionRequestDTO request, Transaction transaction) {
        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset não encontrado: " + request.assetId()));

        OperationType opType = operationTypeRepository.findById(request.operationTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de Operação não encontrado: " + request.operationTypeId()));

        transaction.setDate(request.date());
        transaction.setAsset(asset);
        transaction.setOperationType(opType);
        transaction.setStock(request.stock());
        transaction.setUnitValue(request.unitValue());
        transaction.setFee(request.fee());
        transaction.setMemo(request.memo());
        transaction.setActive(request.active());
        transaction.setRefCompra(request.refCompra());
        transaction.setDateSales(request.dateSales());
    }

    private TransactionResponseDTO convertToResponseDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getDate(),
                transaction.getOperationType() != null ? transaction.getOperationType().getIdOperation() : null,
                transaction.getOperationType() != null ? transaction.getOperationType().getDescription() : null,
                transaction.getAsset() != null ? transaction.getAsset().getId() : null,
                transaction.getStock(),
                transaction.getUnitValue(),
                transaction.getFee(),
                transaction.getMemo(),
                transaction.getActive(),
                transaction.getRefCompra(),
                transaction.getDateSales());
    }

    public List<TransactionResponseDTO> findByAssetId(String assetId) {
        return transactionRepository.findByAssetId(assetId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<TransactionResponseDTO> findByOperationTypeId(Integer operationTypeId) {
        return transactionRepository.findByOperationTypeIdOperation(operationTypeId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<TransactionResponseDTO> findByActive(Integer active) {
        return transactionRepository.findByActive(active).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<com.market.financial.dto.TransactionResponseDTO> findByDate(java.time.LocalDate date) {
        List<com.market.financial.model.Transaction> transactions = transactionRepository.findByDate(date);
        if (transactions.isEmpty()) {
            throw new com.market.financial.infra.exception.ResourceNotFoundException(
                    "Nenhuma transação financeira foi localizada na data " + date + ".");
        }
        return transactions.stream().map(this::convertToResponseDTO).toList();
    }

}
