package com.market.financial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.financial.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
        // Busca por ID do Ativo (String)
    List<Transaction> findByAssetId(String assetId);
    
    // Busca por ID do Tipo de Operação (Integer)
    List<Transaction> findByOperationTypeIdOperation(Integer operationTypeId);
    
    // Busca por Status Ativo (Integer: 0 ou 1)
    List<Transaction> findByActive(Integer active);

    List<com.market.financial.model.Transaction> findByDate(java.time.LocalDate date);

}

