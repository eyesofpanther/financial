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
        purchaseTx.setFee(
                request.fee() != null ? request.fee().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2));
        purchaseTx.setMemo(request.memo());
        purchaseTx.setActive(1);
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
        saleTx.setFee(
                request.fee() != null ? request.fee().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2));
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

    @Transactional
    public TransactionResponseDTO save(TransactionRequestDTO request) {
        // 🛡️ Blindagem: Impede o uso do fluxo genérico para operações estruturais de
        // estoque
        if (request.operationTypeId() == 1 || request.operationTypeId() == 2) {
            throw new IllegalStateException(
                    "Operação não permitida: Compras (1) e Vendas (2) possuem regras complexas de lote e " +
                            "não podem usar o fluxo genérico. Utilize os endpoints dedicados /purchase ou /sale.");
        }

        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset não encontrado: " + request.assetId()));
        OperationType opType = operationTypeRepository.findById(request.operationTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de Operação não encontrado: " + request.operationTypeId()));

        Transaction transaction = new Transaction();
        mapRequestToEntity(request, transaction, asset, opType);

        return convertToResponseDTO(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponseDTO update(Long id, TransactionRequestDTO request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));

        // Validação preventiva contra mutabilidade de campos que quebram o FIFO
        if (hasCriticalChanges(transaction, request)) {
            throw new IllegalStateException(
                    "Alterações estruturais (ativo, quantidade ou data) não são permitidas via update " +
                            "direto para proteger a integridade do motor FIFO. Use a exclusão restrita ou estorno.");
        }

        // Permite apenas edição de campos neutros e metadados
        transaction.setMemo(request.memo());
        if (request.fee() != null) {
            transaction.setFee(request.fee().setScale(2, RoundingMode.HALF_UP));
        }

        return convertToResponseDTO(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));

        int idOp = transaction.getOperationType().getIdOperation();

        // Regra de Ouro do FIFO: Compras (ID 1) só podem ser excluídas se o lote
        // estiver 100% intacto
        if (idOp == 1) {
            if (transaction.getAvailableQuantity().compareTo(transaction.getStock()) != 0) {
                throw new IllegalStateException(
                        "Operação abortada: Esta compra não pode ser excluída porque seu lote " +
                                "de ativos já foi parcial ou totalmente consumido por uma ou mais vendas subsequentes.");
            }
        }
        // Vendas (ID 2) exigem estorno e recálculo cronológico completo para devolver
        // cotas aos lotes antigos
        else if (idOp == 2) {
            throw new IllegalStateException(
                    "Operação abortada: A exclusão direta de vendas quebraria o histórico do FIFO. " +
                            "Para corrigir esta operação, desfaça as transações dependentes ou utilize um lançamento de ajuste.");
        }

        transactionRepository.delete(transaction);
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
        // Alinhado com o padrão REST: retorna lista vazia estável se não houver
        // registros
        return transactionRepository.findByDate(date).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    private void mapRequestToEntity(TransactionRequestDTO request, Transaction transaction, Asset asset,
            OperationType opType) {
        transaction.setDate(request.date());
        transaction.setAsset(asset);
        transaction.setOperationType(opType);
        transaction.setMemo(request.memo());
        transaction.setRefCompra(request.refCompra());
        transaction.setDateSales(request.dateSales());

        BigDecimal stockScaled = request.stock() != null ? request.stock().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        transaction.setStock(stockScaled);

        transaction.setUnitValue(
                request.unitValue() != null ? request.unitValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        transaction.setFee(request.fee() != null ? request.fee().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // Switch Expression (Java 25): Identifica operações de incremento de estoque
        boolean incrementsInventory = switch (opType.getIdOperation()) {
            case 1, 3, 4, 5, 6 -> true;
            default -> false;
        };

        if (incrementsInventory) {
            transaction.setAvailableQuantity(stockScaled);
            transaction.setActive(1);
        } else {
            transaction.setAvailableQuantity(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            transaction.setActive(request.active() != null ? request.active() : 0);
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
                transaction.getDateSales());
    }

    private boolean hasCriticalChanges(Transaction tx, TransactionRequestDTO req) {
        return tx.getStock().compareTo(req.stock()) != 0 ||
                tx.getUnitValue().compareTo(req.unitValue()) != 0 ||
                !tx.getDate().equals(req.date()) ||
                !tx.getAsset().getId().equals(req.assetId());
    }
}
