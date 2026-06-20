package com.ucu.ticketing.controller;

import com.ucu.ticketing.dto.request.LoginRequest;
import com.ucu.ticketing.dto.request.RegisterRequest;
import com.ucu.ticketing.dto.response.AuthResponse;
import com.ucu.ticketing.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// expone los endpoints publicos de registro y login
// estas rutas no requieren autenticacion (configurado en SecurityConfig)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // registra un nuevo usuario general y devuelve un token jwt
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // valida credenciales y devuelve un token jwt
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}