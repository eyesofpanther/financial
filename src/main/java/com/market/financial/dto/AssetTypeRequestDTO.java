package com.market.financial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssetTypeRequestDTO(
    @NotBlank(message = "A descrição do tipo de ativo é obrigatória")
    @Size(max = 50, message = "A descrição deve ter no máximo 50 caracteres")
    String description
) {}
