package org.example.hackaton1_.githubmodel;

import org.example.hackaton1_.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class GitHubModelsClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl;
    private final String apiToken;
    private final String modelId;

    public GitHubModelsClient(
            @Value("${GITHUB_MODELS_URL}") String apiUrl,
            @Value("${GITHUB_TOKEN}") String apiToken,
            @Value("${MODEL_ID}") String modelId
    ) {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalArgumentException("El token de autenticación no está configurado.");
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalArgumentException("La URL del modelo no está configurada.");
        }

        this.apiUrl = apiUrl.trim();
        this.apiToken = apiToken.trim();
        this.modelId = modelId != null ? modelId.trim() : "default-model";
    }

    public String generateSummary(Map<String, Object> summaryData) {
        try {
            double revenue = parseRevenue(summaryData.get("totalRevenue"));

            String prompt = String.format(
                    "Con estos datos: totalUnits=%s, totalRevenue=%.2f, topSku=%s, topBranch=%s. " +
                            "Devuelve un resumen breve y claro en español (máx. 120 palabras).",
                    summaryData.getOrDefault("totalUnits", "N/A"),
                    revenue,
                    summaryData.getOrDefault("topSku", "N/A"),
                    summaryData.getOrDefault("topBranch", "N/A")
            );

            // 1️⃣ Construcción dinámica del cuerpo
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", modelId);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "Eres un analista que redacta resúmenes ejecutivos en español."),
                    Map.of("role", "user", "content", prompt)
            ));
            body.put("max_tokens", 200);

            // 2️⃣ Construcción dinámica de encabezados
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // Detecta automáticamente el prefijo del token
            if (apiToken.startsWith("ghp_")) {
                headers.set("Authorization", "Bearer " + apiToken); // GitHub
            } else if (apiToken.startsWith("sk-")) {
                headers.set("Authorization", "Bearer " + apiToken); // OpenAI
            } else if (apiToken.length() > 40) {
                headers.set("Authorization", "Bearer " + apiToken); // Azure o general
            } else {
                headers.set("Authorization", "token " + apiToken);  // Casos personalizados
            }

            headers.set("User-Agent", "OreoInsightFactory/UniversalClient");
            headers.set("Accept-Charset", "UTF-8");

            // Ejecutar request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

            // Procesar respuesta
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> respBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            return generarFallback(summaryData, "Respuesta inesperada del modelo.");

        } catch (HttpClientErrorException e) {
            // Si hay error HTTP, genera un resumen manual y continúa
            return generarFallback(summaryData, "Error HTTP: " + e.getStatusCode());
        } catch (Exception e) {
            // Cualquier otra excepción
            return generarFallback(summaryData, "Fallo en la conexión al modelo: " + e.getMessage());
        }
    }

    private double parseRevenue(Object rev) {
        if (rev instanceof Number) return ((Number) rev).doubleValue();
        if (rev != null) {
            try {
                return Double.parseDouble(rev.toString());
            } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }

    private String generarFallback(Map<String, Object> summaryData, String motivo) {
        return String.format(
                "[Resumen generado localmente]\n" +
                        "Motivo: %s\n" +
                        "Unidades totales: %s\n" +
                        "Ingresos: %s\n" +
                        "Producto destacado: %s\n" +
                        "Sucursal líder: %s",
                motivo,
                summaryData.getOrDefault("totalUnits", "N/A"),
                summaryData.getOrDefault("totalRevenue", "N/A"),
                summaryData.getOrDefault("topSku", "N/A"),
                summaryData.getOrDefault("topBranch", "N/A")
        );
    }
}