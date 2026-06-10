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

    public TransactionService(TransactionRepository transactionRepository, AssetRepository assetRepository, OperationTypeRepository operationTypeRepository) {
        this.transactionRepository = transactionRepository;
        this.assetRepository = assetRepository;
        this.operationTypeRepository = operationTypeRepository;
    }

    @Transactional
    public TransactionResponseDTO processPurchaseTransaction(TransactionRequestDTO request) {
        Asset purchasedAsset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.assetId()));

        OperationType buyOp = new OperationType();
        buyOp.setIdOperation(1);

        Transaction purchaseTx = new Transaction();
        purchaseTx.setDate(request.date());
        purchaseTx.setAsset(purchasedAsset);
        purchaseTx.setOperationType(buyOp);
        
        BigDecimal stockScaled = request.stock().setScale(2, RoundingMode.HALF_UP);
        purchaseTx.setStock(stockScaled);
        purchaseTx.setUnitValue(request.unitValue().setScale(2, RoundingMode.HALF_UP));
        purchaseTx.setFee(request.fee() != null ? request.fee().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2));
        purchaseTx.setMemo(request.memo());
        purchaseTx.setActive(1);
        
        // CORREÇÃO: Alinha quantidade disponível com o total comprado
        purchaseTx.setAvailableQuantity(stockScaled);
        
        purchaseTx.setRefCompra(null);
        purchaseTx.setDateSales(null);

        Transaction savedPurchase = transactionRepository.save(purchaseTx);

        Asset spaxxAsset = assetRepository.findById("SPAXX")
                .orElseThrow(() -> new ResourceNotFoundException("Fixed cash asset 'SPAXX' not found in database."));

        OperationType cashOp = new OperationType();
        cashOp.setIdOperation(4);

        Transaction cashTx = new Transaction();
        cashTx.setDate(savedPurchase.getDate());
        cashTx.setAsset(spaxxAsset);
        cashTx.setOperationType(cashOp);

        BigDecimal totalSpent = savedPurchase.getStock().multiply(savedPurchase.getUnitValue());
        BigDecimal totalSpentScaled = totalSpent.setScale(2, RoundingMode.HALF_UP);
        
        cashTx.setStock(totalSpentScaled);
        cashTx.setUnitValue(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP));
        cashTx.setFee(null);
        cashTx.setMemo("Compra " + purchasedAsset.getId());
        cashTx.setActive(1);
        
        // CORREÇÃO: Caixa SPAXX espelha o valor total movimentado
        cashTx.setAvailableQuantity(totalSpentScaled);
        
        cashTx.setRefCompra(null);
        cashTx.setDateSales(null);

        transactionRepository.save(cashTx);

        return convertToResponseDTO(savedPurchase);
    }

    @Transactional
    public TransactionResponseDTO processSaleTransaction(TransactionRequestDTO request) {
        Asset soldAsset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.assetId()));

        OperationType sellOp = new OperationType();
        sellOp.setIdOperation(2);

        Transaction saleTx = new Transaction();
        saleTx.setDate(request.date());
        saleTx.setAsset(soldAsset);
        saleTx.setOperationType(sellOp);
        saleTx.setStock(request.stock().setScale(2, RoundingMode.HALF_UP));
        saleTx.setUnitValue(request.unitValue().setScale(2, RoundingMode.HALF_UP));
        saleTx.setFee(request.fee() != null ? request.fee().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2));
        saleTx.setMemo(request.memo());
        saleTx.setActive(0);
        saleTx.setAvailableQuantity(BigDecimal.ZERO.setScale(2));
        saleTx.setRefCompra(request.refCompra());
        saleTx.setDateSales(request.date());

        Transaction savedSale = transactionRepository.save(saleTx);

        Asset spaxxAsset = assetRepository.findById("SPAXX")
                .orElseThrow(() -> new ResourceNotFoundException("Fixed cash asset 'SPAXX' not found in database."));

        OperationType cashCreditOp = new OperationType();
        cashCreditOp.setIdOperation(3);

        Transaction cashTx = new Transaction();
        cashTx.setDate(savedSale.getDate());
        cashTx.setAsset(spaxxAsset);
        cashTx.setOperationType(cashCreditOp);

        BigDecimal grossAmount = savedSale.getStock().multiply(savedSale.getUnitValue());
        BigDecimal feeAmount = savedSale.getFee() != null ? savedSale.getFee() : BigDecimal.ZERO;
        BigDecimal netReceived = grossAmount.subtract(feeAmount);
        BigDecimal netReceivedScaled = netReceived.setScale(2, RoundingMode.HALF_UP);

        cashTx.setStock(netReceivedScaled);
        cashTx.setUnitValue(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP));
        cashTx.setFee(null);
        cashTx.setMemo("Venda " + soldAsset.getId());
        cashTx.setActive(1);
        cashTx.setAvailableQuantity(netReceivedScaled);
        cashTx.setRefCompra(null);
        cashTx.setDateSales(null);

        transactionRepository.save(cashTx);

        List<Transaction> activeTransactions = transactionRepository.findActivePurchasesFIFO(soldAsset.getId());
        BigDecimal totalRestanteVenda = savedSale.getStock();

        for (Transaction activeTx : activeTransactions) {
            if (totalRestanteVenda.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal saldoDisponivelLote = activeTx.getAvailableQuantity();

            if (saldoDisponivelLote.compareTo(totalRestanteVenda) >= 0) {
                BigDecimal novoSaldoLote = saldoDisponivelLote.subtract(totalRestanteVenda);
                activeTx.setAvailableQuantity(novoSaldoLote.setScale(2, RoundingMode.HALF_UP));
                if (novoSaldoLote.compareTo(BigDecimal.ZERO) == 0) {
                    activeTx.setActive(0);
                    activeTx.setDateSales(savedSale.getDate());
                }
                transactionRepository.save(activeTx);
                totalRestanteVenda = BigDecimal.ZERO;
            } else {
                totalRestanteVenda = totalRestanteVenda.subtract(saldoDisponivelLote);
                activeTx.setAvailableQuantity(BigDecimal.ZERO.setScale(2));
                activeTx.setActive(0);
                activeTx.setDateSales(savedSale.getDate());
                transactionRepository.save(activeTx);
            }
        }

        if (totalRestanteVenda.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Erro: Saldo insuficiente de cotas em lotes ativos para realizar essa venda.");
        }

        return convertToResponseDTO(savedSale);
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
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Operação não encontrado: " + request.operationTypeId()));

        transaction.setDate(request.date());
        transaction.setAsset(asset);
        transaction.setOperationType(opType);
        
        BigDecimal stockScaled = request.stock() != null ? request.stock().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        transaction.setStock(stockScaled);
        transaction.setUnitValue(request.unitValue() != null ? request.unitValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        transaction.setFee(request.fee() != null ? request.fee().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        transaction.setMemo(request.memo());
        transaction.setActive(request.active());
        transaction.setRefCompra(request.refCompra());
        transaction.setDateSales(request.dateSales());

        // CORREÇÃO: Proteção para cadastros manuais ou edições via endpoint padrão
        if (opType.getIdOperation() == 1 || opType.getIdOperation() == 3 || opType.getIdOperation() == 4) {
            transaction.setAvailableQuantity(stockScaled);
        } else {
            transaction.setAvailableQuantity(BigDecimal.ZERO.setScale(2));
        }
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
                transaction.getDateSales()
        );
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

    public List<TransactionResponseDTO> findByDate(java.time.LocalDate date) {
        List<Transaction> transactions = transactionRepository.findByDate(date);
        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma transação financeira foi localizada na data " + date + ".");
        }
        return transactions.stream().map(this::convertToResponseDTO).toList();
    }
}
