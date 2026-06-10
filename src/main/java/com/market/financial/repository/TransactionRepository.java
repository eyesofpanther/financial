package com.market.financial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;

//import com.market.financial.model.Asset;
import com.market.financial.model.Transaction;

//@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
        // Busca por ID do Ativo (String)
    List<Transaction> findByAssetId(String assetId);
    
    // Busca por ID do Tipo de Operação (Integer)
    List<Transaction> findByOperationTypeIdOperation(Integer operationTypeId);
    
    // Busca por Status Ativo (Integer: 0 ou 1)
    List<Transaction> findByActive(Integer active);

    List<com.market.financial.model.Transaction> findByDate(java.time.LocalDate date);

    // Método para buscar os registros ativos do ativo específico
   List<Transaction> findByAssetIdAndActive(String assetId, Integer active);

    // Busca compras ativas ordenadas por data e ID (Critério FIFO)
    // Ajuste o ID da Operação de Compra conforme seu banco (ex: 1)
    @Query("SELECT t FROM Transaction t WHERE t.asset.id = :assetId " +
           "AND t.operationType.id = 1 " + // ID 1 = Compra (Op 1/4)
           "AND t.active = 1 " +
           "ORDER BY t.date ASC, t.id ASC")
    List<Transaction> findActivePurchasesFIFO(@Param("assetId") String assetId);
}

