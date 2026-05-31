package com.market.financial.service;

import com.market.financial.dto.OperationTypeRequestDTO;
import com.market.financial.infra.exception.ResourceNotFoundException;
import com.market.financial.model.OperationType;
import com.market.financial.repository.OperationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OperationTypeService {

    @Autowired
    private OperationTypeRepository repository;

    public List<OperationType> findAll() {
        return repository.findAll();
    }

    public Optional<OperationType> findById(Integer id) {
        return repository.findById(id);
    }

    public OperationType save(OperationTypeRequestDTO dto) {
        OperationType entity = new OperationType();
        entity.setDescription(dto.description());
        entity.setIo(dto.io());
        return repository.save(entity);
    }

    public OperationType update(Integer id, OperationTypeRequestDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setDescription(dto.description());
            existing.setIo(dto.io());
            return repository.save(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("Operation Type não encontrado com o ID: " + id));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Operation Type não encontrado com o ID: " + id);
        }
        repository.deleteById(id);
    }
}
