package com.PlayForYouApp.project.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PlayForYouApp.project.dto.auth.AuthResponse;
import com.PlayForYouApp.project.dto.auth.LoginRequest;
import com.PlayForYouApp.project.dto.auth.RegisterRequest;
import com.PlayForYouApp.project.entities.User;
import com.PlayForYouApp.project.enums.Role;
import com.PlayForYouApp.project.exception.ApiException;
import com.PlayForYouApp.project.repositories.UserRepository;
import com.PlayForYouApp.project.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MusicMapper musicMapper;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        MusicMapper musicMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.musicMapper = musicMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_USER);

        User savedUser = userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(savedUser), musicMapper.toUserDto(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return new AuthResponse(jwtService.generateToken(user), musicMapper.toUserDto(user));
    }
}
