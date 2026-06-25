package com.market.financial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
    @NotBlank(message = "O username não pode estar em branco")
    @Size(min = 3, max = 50, message = "O username deve ter entre 3 e 50 caracteres")
    String username,

    @NotBlank(message = "O nome completo não pode estar em branco")
    String fullName, // Novo Campo

    @NotBlank(message = "A senha não pode estar em branco")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    String password
) {}
