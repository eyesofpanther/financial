package com.market.financial.service;

import com.market.financial.dto.AssetRequestDTO;
import com.market.financial.infra.exception.ResourceAlreadyExistsException;
import com.market.financial.infra.exception.ResourceNotFoundException;
import com.market.financial.model.Asset;
import com.market.financial.model.AssetType;
import com.market.financial.repository.AssetRepository;
import com.market.financial.repository.AssetTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    @Autowired
    private AssetRepository repository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    public List<Asset> findAll() {
        return repository.findAll();
    }

    public Optional<Asset> findById(String id) {
        return repository.findById(id);
    }

    public Asset save(AssetRequestDTO dto) {
        if (repository.existsById(dto.id())) {
            // Mudança aqui: de RuntimeException para ResourceAlreadyExistsException
            throw new ResourceAlreadyExistsException("Já existe um ativo cadastrado com o ID: " + dto.id());
        }

        AssetType assetType = assetTypeRepository.findById(dto.assetTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Type não encontrado com o ID: " + dto.assetTypeId()));

        Asset entity = new Asset(dto.id(), dto.description(), assetType);
        return repository.save(entity);
    }

    public Asset update(String id, AssetRequestDTO dto) {
        return repository.findById(id).map(existing -> {
            AssetType assetType = assetTypeRepository.findById(dto.assetTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Asset Type não encontrado com o ID: " + dto.assetTypeId()));

            existing.setDescription(dto.description());
            existing.setAssetType(assetType);
            return repository.save(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("Asset não encontrado com o ID: " + id));
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Asset não encontrado com o ID: " + id);
        }
        repository.deleteById(id);
    }
}
