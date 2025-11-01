package org.example.hackaton1_.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = Map.of(
                "error", "BAD_REQUEST",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now(),
                "path", "N/A"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler({ConflictException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request.getRequestURI());
    }

    // Mapeo 403 Forbidden (p.ej. BRANCH intentando acceder a otra sucursal)
    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<Map<String, Object>> handleForbidden(RuntimeException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "No tiene permisos para esta acción.", request.getRequestURI());
    }

    // Mapeo 503 Service Unavailable (p.ej. LLM o Mail caído)
    @ExceptionHandler({ServiceUnavailableException.class})
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable(RuntimeException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", ex.getMessage(), request.getRequestURI());
    }

    // Método auxiliar para construir la respuesta estandarizada
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String errorType, String message, String path) {
        Map<String, Object> body = Map.of(
                "error", errorType,
                "message", message,
                "timestamp", LocalDateTime.now().toString(),
                "path", path
        );
        return ResponseEntity.status(status).body(body);
    }
}
