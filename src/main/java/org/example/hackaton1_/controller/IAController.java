package org.example.hackaton1_.controller;

import org.example.hackaton1_.githubmodel.GitHubModelsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    private final GitHubModelsClient gitHubModelsClient;

    @Autowired
    public IAController(GitHubModelsClient gitHubModelsClient) {
        this.gitHubModelsClient = gitHubModelsClient;
    }

    @PostMapping("/summary")
    public ResponseEntity<Map<String, String>> generateSummary(@RequestBody Map<String, Object> requestData) {
        try {
            String summary = gitHubModelsClient.generateSummary(requestData);
            return ResponseEntity.ok(Map.of("summary", summary));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
