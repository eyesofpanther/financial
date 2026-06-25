package com.market.financial.dto;

import com.market.financial.model.User;

public record UserResponseDTO(
    Long id,
    String username,
    String fullName,
    boolean isActive,
    Long createdAt
) {
    public UserResponseDTO(User user) {
        this(user.getId(), user.getUsername(), user.getFullName(), user.isActive(), user.getCreatedAt());
    }
}
