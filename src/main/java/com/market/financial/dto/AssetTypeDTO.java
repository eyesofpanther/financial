package com.market.financial.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetTypeDTO(
    Integer id,
    @NotBlank(message = "O nome é obrigatório") String name
) {}
