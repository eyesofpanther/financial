package com.market.financial.repository;

import com.market.financial.model.MonthlyConsolidated;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

//@Repository
public interface MonthlyConsolidatedRepository extends JpaRepository<MonthlyConsolidated, Long> {
    List<MonthlyConsolidated> findByAssetId(String assetId);
    List<MonthlyConsolidated> findByDate(LocalDate date);
}
