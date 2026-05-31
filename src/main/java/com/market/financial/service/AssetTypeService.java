package com.market.financial.service;

import com.market.financial.dto.AssetTypeRequestDTO;
import com.market.financial.infra.exception.ResourceNotFoundException;
import com.market.financial.model.AssetType;
import com.market.financial.repository.AssetTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AssetTypeService {

    @Autowired
    private AssetTypeRepository repository;

    public List<AssetType> findAll() {
        return repository.findAll();
    }

    public Optional<AssetType> findById(Integer id) {
        return repository.findById(id);
    }

    public AssetType save(AssetTypeRequestDTO dto) {
        AssetType entity = new AssetType();
        entity.setDescription(dto.description());
        return repository.save(entity);
    }

    public AssetType update(Integer id, AssetTypeRequestDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setDescription(dto.description());
            return repository.save(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("Asset Type não encontrado com o ID: " + id));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Asset Type não encontrado com o ID: " + id);
        }
        repository.deleteById(id);
    }
}
