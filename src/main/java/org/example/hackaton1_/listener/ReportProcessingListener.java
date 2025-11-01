package org.example.hackaton1_.listener;

import org.example.hackaton1_.dto.SalesSummaryRequest;
import org.example.hackaton1_.event.ReportRequestedEvent;
import org.example.hackaton1_.githubmodel.GitHubModelsClient;
import org.example.hackaton1_.service.EmailService;
import org.example.hackaton1_.service.SalesAggregationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class ReportProcessingListener {

    private final SalesAggregationService aggregationService;
    private final GitHubModelsClient githubClient;
    private final EmailService emailService;

    public ReportProcessingListener(SalesAggregationService aggregationService,
                                    GitHubModelsClient githubClient,
                                    EmailService emailService) {
        this.aggregationService = aggregationService;
        this.githubClient = githubClient;
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handleReportRequest(ReportRequestedEvent event) {
        SalesSummaryRequest req = event.getRequest();

        // 1. Cálculo de agregados
        var summary = aggregationService.calculateSummary(req.getFrom(), req.getTo(), req.getBranch());

        // 2. Generación de resumen con LLM
        String aiSummary = githubClient.generateSummary(summary);

        // 3. Formato del Asunto
        String subject = String.format("Reporte Semanal Oreo - %s a %s",
                req.getFrom().toString(), req.getTo().toString());

        // 4. Envío de Email
        emailService.sendEmail(req.getEmailTo(),
                subject,
                aiSummary + "\n\nDatos Agregados:\n" + summary); // Incluye los agregados en el cuerpo
    }
}
