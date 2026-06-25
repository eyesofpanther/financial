package com.market.financial.service;

import com.market.financial.dto.LoginRequestDTO;
import com.market.financial.dto.UserResponseDTO;
import com.market.financial.model.User;
import com.market.financial.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO authenticate(LoginRequestDTO dto) {
        // 1. Busca o usuário pelo username
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas: usuário ou senha incorretos.")); // Retorna 422 global

        // 2. Verifica se o usuário está ativo no sistema
        if (!user.isActive()) {
            throw new IllegalArgumentException("Acesso negado: esta conta de usuário está inativa.");
        }

        // 3. Compara a senha em texto limpo com o Hash seguro salvo no banco
        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas: usuário ou senha incorretos.");
        }

        // 4. Autenticação bem-sucedida, retorna os dados do usuário para o Front-end
        return new UserResponseDTO(user);
    }
}
