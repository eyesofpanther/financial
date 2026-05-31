package com.market.financial.dto;

import com.market.financial.model.OperationType;

public record OperationTypeResponseDTO(
    Integer idOperation,
    String description,
    Integer io
) {
    // Construtor de conveniência para mapear a Entidade para DTO
    public OperationTypeResponseDTO(OperationType entity) {
        this(entity.getIdOperation(), entity.getDescription(), entity.getIo());
    }
}
