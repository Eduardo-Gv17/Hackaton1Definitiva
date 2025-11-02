package org.example.hackaton1_.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.hackaton1_.model.Role;

public class RegisterRequest {

    @NotBlank(message = "Username es obligatorio")
    @Size(min = 3, max = 30, message = "Username debe tener entre 3 y 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_.]*$", message = "Username solo puede contener alfanuméricos, _ y .")
    private String username;

    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Email debe tener un formato válido")
    private String email;

    @NotBlank(message = "Password es obligatorio")
    @Size(min = 8, message = "Password debe tener mínimo 8 caracteres")
    private String password;

    @NotNull(message = "Role es obligatorio")
    private Role role;

    private String branch; // La lógica de validación (si es obligatorio o no) está en el AuthService

    // Getters y Setters (o usa Lombok)
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
}