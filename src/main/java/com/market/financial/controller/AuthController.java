package com.market.financial.controller;

import com.market.financial.dto.LoginRequestDTO;
import com.market.financial.dto.UserResponseDTO;
import com.market.financial.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        UserResponseDTO response = authService.authenticate(dto);
        return ResponseEntity.ok(response);
    }
}
