package com.market.financial.dto;

import com.market.financial.model.AssetType;

public record AssetTypeResponseDTO(
    Integer id, // Alterado para casar com o atributo 'id' da sua Entidade
    String description
) {
    public AssetTypeResponseDTO(AssetType entity) {
        this(entity.getId(), entity.getDescription());
    }
}
