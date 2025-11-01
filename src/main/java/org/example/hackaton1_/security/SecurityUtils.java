
package org.example.hackaton1_.security;

import org.example.hackaton1_.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new IllegalStateException("Usuario no autenticado o no es una instancia de User");
    }
}