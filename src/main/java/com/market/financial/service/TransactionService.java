package com.market.financial.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.market.financial.dto.TransactionRequestDTO;
import com.market.financial.dto.TransactionResponseDTO;
import com.market.financial.infra.exception.ResourceNotFoundException;
import com.market.financial.model.Asset;
import com.market.financial.model.OperationType;
import com.market.financial.model.Transaction;
import com.market.financial.repository.AssetRepository;
import com.market.financial.repository.OperationTypeRepository;
import com.market.financial.repository.TransactionRepository;

import jakarta.transaction.Transactional;

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
    @Transactional
    public TransactionResponseDTO processPurchaseTransaction(TransactionRequestDTO request) {
        
        // 1. REGISTRO DA TRANSAÇÃO 1: Compra do Ativo (Operation_ID = 1)
        Asset purchasedAsset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.assetId()));

        // Configura Tipo de Operação = 1 (Compra) usando o setter correto da sua entidade
        OperationType buyOp = new OperationType();
        buyOp.setIdOperation(1); 

        Transaction purchaseTx = new Transaction();
        purchaseTx.setDate(request.date());
        purchaseTx.setAsset(purchasedAsset);
        purchaseTx.setOperationType(buyOp);
        
        // Alinha precisão rigorosa de duas casas decimais
        purchaseTx.setStock(request.stock().setScale(2, RoundingMode.HALF_UP));
        purchaseTx.setUnitValue(request.unitValue().setScale(2, RoundingMode.HALF_UP));
        purchaseTx.setFee(request.fee() != null ? request.fee().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2));
        
        purchaseTx.setMemo(request.memo());
        purchaseTx.setActive(1);
        purchaseTx.setRefCompra(null);
        purchaseTx.setDateSales(null);

        // Salva a Transação 1
        Transaction savedPurchase = transactionRepository.save(purchaseTx);

        // 2. REGISTRO DA TRANSAÇÃO 2: Débito do Caixa SPAXX (Operation_ID = 4)
        Asset spaxxAsset = assetRepository.findById("SPAXX")
                .orElseThrow(() -> new ResourceNotFoundException("Fixed cash asset 'SPAXX' not found in database."));

        // Configura Tipo de Operação = 4 usando o setter correto da sua entidade
        OperationType cashOp = new OperationType();
        cashOp.setIdOperation(4);

        Transaction cashTx = new Transaction();
        cashTx.setDate(savedPurchase.getDate()); // Mesma data da transação 1
        cashTx.setAsset(spaxxAsset);
        cashTx.setOperationType(cashOp);

        // Cálculo do Stock do SPAXX: (Stock da Transação 1 * Unit_Value da Transação 1)
        BigDecimal totalSpent = savedPurchase.getStock().multiply(savedPurchase.getUnitValue());
        cashTx.setStock(totalSpent.setScale(2, RoundingMode.HALF_UP));

        // Regras estritas para a operação 4
        cashTx.setUnitValue(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP)); // Unit_Value = 1
        cashTx.setFee(null); // Fee = null
        cashTx.setMemo("Compra " + purchasedAsset.getId()); // Substitui o texto pelo Ticker/ID do ativo
        cashTx.setActive(1);
        cashTx.setRefCompra(null);
        cashTx.setDateSales(null);

        // Salva a Transação 2
        transactionRepository.save(cashTx);

        // Retorna o DTO da Transação 1
        return convertToResponseDTO(savedPurchase);
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
