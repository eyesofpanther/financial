package com.market.financial.dto;

public record LoginResponseDTO(
    UserResponseDTO user,
    String token
) {}