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

        try {
            System.out.println("Solicitud de reporte recibida. Preparando envío de Gmail...");

            // 1. Calcular datos agregados
            var summary = aggregationService.calculateSummary(req.getFrom(), req.getTo(), req.getBranch());
            System.out.println("Datos agregados generados correctamente.");

            // 2. Generar resumen con IA (si falla, continuar igual)
            String aiSummary;
            try {
                aiSummary = githubClient.generateSummary(summary);
                System.out.println("Resumen IA generado correctamente.");
            } catch (Exception e) {
                aiSummary = "No se pudo generar resumen automático. Se envían solo los datos agregados.";
                System.err.println("Error en generación de resumen IA: " + e.getMessage());
            }

            // 3. Crear asunto y cuerpo del correo
            String subject = String.format("Reporte Semanal Oreo - %s a %s",
                    req.getFrom().toString(), req.getTo().toString());

            String body = "Hola,\n\n"
                    + "Tu reporte semanal está listo.\n\n"
                    + aiSummary + "\n\n"
                    + "Datos agregados:\n" + summary + "\n\n"
                    + "Atentamente,\n"
                    + "Equipo Oreo Insight Factory";

            // 4. Enviar correo garantizado
            emailService.sendEmail(req.getEmailTo(), subject, body);

            System.out.println("Correo enviado correctamente. Flujo completado.");

        } catch (Exception e) {
            System.err.println("Error general en flujo de envío Gmail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}