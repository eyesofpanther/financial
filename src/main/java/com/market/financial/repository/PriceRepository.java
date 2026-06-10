package com.market.financial.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;

import com.market.financial.model.Price;

//@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    // O Spring Data JPA monta o SQL automaticamente baseado no nome do método
    List<Price> findByAssetId(String assetId);

    // Nova consulta combinada (Retorna um Optional pois o preço pode não existir na
    // data)
    Optional<Price> findByAssetIdAndDate(String assetId, LocalDate date);

    List<com.market.financial.model.Price> findByDate(java.time.LocalDate date);

    // Busca a cotação mais recente disponível até a data limite informada
    java.util.Optional<com.market.financial.model.Price> findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(
            String assetId, java.time.LocalDate date);

}
