package com.market.financial.service;

import com.market.financial.dto.MonthlyConsolidatedRequestDTO;
import com.market.financial.dto.MonthlyConsolidatedResponseDTO;
import com.market.financial.infra.exception.ResourceNotFoundException;
import com.market.financial.model.Asset;
import com.market.financial.model.MonthlyConsolidated;
import com.market.financial.repository.AssetRepository;
import com.market.financial.repository.MonthlyConsolidatedRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class MonthlyConsolidatedService {

    private final MonthlyConsolidatedRepository repository;
    private final AssetRepository assetRepository;

    public MonthlyConsolidatedService(MonthlyConsolidatedRepository repository, AssetRepository assetRepository) {
        this.repository = repository;
        this.assetRepository = assetRepository;
    }

    public List<MonthlyConsolidatedResponseDTO> findAll() {
        return repository.findAll().stream().map(this::convertToResponseDTO).toList();
    }

    public MonthlyConsolidatedResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Consolidado não encontrado com ID: " + id));
    }

    public List<MonthlyConsolidatedResponseDTO> findByAssetId(String assetId) {
        List<MonthlyConsolidated> list = repository.findByAssetId(assetId);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum consolidado mensal encontrado para o ativo: " + assetId);
        }
        return list.stream().map(this::convertToResponseDTO).toList();
    }

    public List<MonthlyConsolidatedResponseDTO> findByDate(LocalDate date) {
        List<MonthlyConsolidated> list = repository.findByDate(date);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum consolidado mensal encontrado para a data: " + date);
        }
        return list.stream().map(this::convertToResponseDTO).toList();
    }

    public MonthlyConsolidatedResponseDTO save(MonthlyConsolidatedRequestDTO request) {
        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset não encontrado: " + request.assetId()));

        MonthlyConsolidated consolidated = new MonthlyConsolidated();
        consolidated.setDate(request.date());
        consolidated.setAsset(asset);
        consolidated.setStock(request.stock());

        return convertToResponseDTO(repository.save(consolidated));
    }

    public MonthlyConsolidatedResponseDTO update(Long id, MonthlyConsolidatedRequestDTO request) {
        MonthlyConsolidated consolidated = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consolidado não encontrado com ID: " + id));

        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset não encontrado: " + request.assetId()));

        consolidated.setDate(request.date());
        consolidated.setAsset(asset);
        consolidated.setStock(request.stock());

        return convertToResponseDTO(repository.save(consolidated));
    }

    public void delete(Long id) {
        MonthlyConsolidated consolidated = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consolidado não encontrado com ID: " + id));
        repository.delete(consolidated);
    }

    private MonthlyConsolidatedResponseDTO convertToResponseDTO(MonthlyConsolidated entity) {
        return new MonthlyConsolidatedResponseDTO(
                entity.getId(),
                entity.getDate(),
                entity.getAsset() != null ? entity.getAsset().getId() : null,
                entity.getStock()
        );
    }

    
}
