package org.example.hackaton1_.security;

import jakarta.validation.Valid; // Importar
import org.example.hackaton1_.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated; // Importar
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated // Habilitar validación en este controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) { // Usar DTO y @Valid
        AuthResponse response = authService.register(
                req.getUsername(),
                req.getEmail(),
                req.getPassword(),
                req.getRole(),
                req.getBranch()
        );
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        // (AuthRequest no necesita validación @Valid aquí, el AuthService maneja el fallo)
        return ResponseEntity.ok(authService.login(req.getUsername(), req.getPassword()));
    }
}