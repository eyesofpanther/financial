package com.market.financial.dto;

import com.market.financial.model.Asset;

public record AssetResponseDTO(
    String id,
    String description,
    AssetTypeResponseDTO assetType
) {
    public AssetResponseDTO(Asset entity) {
        this(
            entity.getId(),
            entity.getDescription(),
            entity.getAssetType() != null ? new AssetTypeResponseDTO(entity.getAssetType()) : null
        );
    }
}
