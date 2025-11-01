package org.example.hackaton1_.githubmodel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Component
public class GitHubModelsClient {

    @Value("${GITHUB_MODELS_URL}")
    private String githubModelsUrl;

    @Value("${GITHUB_TOKEN}")
    private String githubToken;

    @Value("${MODEL_ID}")
    private String modelId;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateSummary(Map<String, Object> summaryData) {

        String prompt = String.format(
                "Con estos datos: totalUnits=%s, totalRevenue=%s, topSku=%s, topBranch=%s. " +
                        "Devuelve un resumen corto y claro en español.",
                summaryData.get("totalUnits"),
                summaryData.get("totalRevenue"),
                summaryData.get("topSku"),
                summaryData.get("topBranch")
        );

        Map<String, Object> body = Map.of(
                "model", modelId,
                "messages", new Object[]{
                        Map.of("role", "system", "content", "Eres un analista que escribe resúmenes breves y claros para emails corporativos."),
                        Map.of("role", "user", "content", prompt)
                },
                "max_tokens", 200
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(githubToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(githubModelsUrl, request, Map.class);

        Map<?, ?> firstChoice = (Map<?, ?>) ((java.util.List<?>) response.getBody().get("choices")).get(0);
        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
        return message.get("content").toString();
    }
}
