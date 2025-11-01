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


        var summary = aggregationService.calculateSummary(req.getFrom(), req.getTo(), req.getBranch());


        String aiSummary = githubClient.generateSummary(summary);


        emailService.sendEmail(req.getEmailTo(),
                "Reporte Semanal Oreo",
                aiSummary);
    }
}
