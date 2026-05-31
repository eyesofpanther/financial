package com.market.financial.repository;

import com.market.financial.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {
    // O Spring Data gera os selects automaticamente aqui
}
