package com.market.financial.service;

import com.market.financial.dto.UserRequestDTO;
import com.market.financial.dto.UserResponseDTO;
import com.market.financial.infra.exception.ResourceNotFoundException;
import com.market.financial.model.User;
import com.market.financial.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("Username já cadastrado no sistema."); // Capturado como 422
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setFullName(dto.fullName());
        user.setPasswordHash(passwordEncoder.encode(dto.password())); // Criptografia BCrypt

        User savedUser = userRepository.save(user);
        return new UserResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        return new UserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.getUsername().equals(dto.username()) && userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("Username já está em uso.");
        }

        user.setUsername(dto.username());
        user.setFullName(dto.fullName());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        return new UserResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        userRepository.delete(user);
    }
}
