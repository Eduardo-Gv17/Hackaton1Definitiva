package org.example.hackaton1_.security;

import org.example.hackaton1_.exception.BadRequestException;
import org.example.hackaton1_.exception.ConflictException;
import org.example.hackaton1_.exception.InvalidCredentialsException;
import org.example.hackaton1_.model.Role;
import org.example.hackaton1_.model.User;
import org.example.hackaton1_.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inyectado desde SecurityConfig
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(String username, String email, String password, Role role, String branch) {
        if (userRepository.existsByEmail(email)) {
            // Usar 409
            throw new ConflictException("El email ya está registrado.");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            // Usar 409
            throw new ConflictException("El username ya está registrado.");
        }

        if (role == Role.BRANCH && (branch == null || branch.isBlank())) {
            // Usar 400
            throw new BadRequestException("El campo 'branch' es obligatorio para usuarios BRANCH.");
        }
        if (role == Role.CENTRAL && branch != null && !branch.isBlank()) {
            branch = null; // Ignorar branch para CENTRAL
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBranch(branch);

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        // El README especifica un response de login, no uno de registro. Adaptamos.
        return new AuthResponse(token, 3600, role.name(), branch);
    }

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                // Usar 401
                .orElseThrow(() -> new InvalidCredentialsException("Usuario o contraseña incorrectos."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            // Usar 401
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos.");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, 3600, user.getRole().name(), user.getBranch());
    }
}