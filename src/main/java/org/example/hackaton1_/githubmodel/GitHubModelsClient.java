package org.example.hackaton1_.githubmodel;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import org.example.hackaton1_.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class GitHubModelsClient {

    private final ChatCompletionsClient client;
    private final String modelId;

    public GitHubModelsClient(@Value("${GITHUB_TOKEN}") String githubToken,
                              @Value("${GITHUB_MODELS_URL}") String endpoint,
                              @Value("${MODEL_ID}") String modelId) {

        if (githubToken == null || githubToken.isBlank()) {
            throw new IllegalArgumentException("GITHUB_TOKEN no está configurado.");
        }

        this.client = new ChatCompletionsClientBuilder()
                .credential(new AzureKeyCredential(githubToken))
                .endpoint(endpoint)
                .buildClient();
        this.modelId = modelId;
    }

    public String generateSummary(Map<String, Object> summaryData) {
        String prompt = String.format(
                "Con estos datos: totalUnits=%s, totalRevenue=%.2f, topSku=%s, topBranch=%s. " +
                        "Devuelve un resumen breve y claro en español (máx. 120 palabras).",
                summaryData.get("totalUnits"),
                (Double) summaryData.get("totalRevenue"), // Asegurar formato de moneda
                summaryData.get("topSku"),
                summaryData.get("topBranch")
        );

        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage("Eres un analista que escribe resúmenes breves y claros para emails corporativos."),
                new ChatRequestUserMessage(prompt)
        );

        ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages)
                .setModel(modelId)
                .setMaxTokens(200);

        try {
            ChatCompletions completions = client.complete(options);

            // --- CORRECCIÓN AQUÍ ---
            // Cambiar .getChoice() por .getChoices().get(0)
            return completions.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            // Manejo de error 503
            throw new ServiceUnavailableException("Fallo al contactar el modelo LLM: " + e.getMessage());
        }
    }
}