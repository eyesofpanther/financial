package com.market.financial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetRequestDTO(
    @NotBlank(message = "O ID do ativo é obrigatório")
    @Size(max = 10, message = "O ID do ativo deve ter no máximo 10 caracteres")
    String id,

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    String description,

    @NotNull(message = "O ID do tipo de ativo é obrigatório")
    Integer assetTypeId
) {}
