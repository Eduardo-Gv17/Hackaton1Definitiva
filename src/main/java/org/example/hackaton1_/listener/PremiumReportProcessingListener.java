package org.example.hackaton1_.listener;

import org.example.hackaton1_.dto.PremiumSalesSummaryRequest;
import org.example.hackaton1_.event.PremiumReportRequestedEvent;
import org.example.hackaton1_.githubmodel.GitHubModelsClient;
import org.example.hackaton1_.service.EmailService;
import org.example.hackaton1_.service.HtmlTemplateService;
import org.example.hackaton1_.service.PdfService;
import org.example.hackaton1_.service.SalesAggregationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PremiumReportProcessingListener {

    private final SalesAggregationService aggregationService;
    private final GitHubModelsClient githubClient;
    private final EmailService emailService;
    private final HtmlTemplateService htmlTemplateService;
    private final PdfService pdfService;

    public PremiumReportProcessingListener(SalesAggregationService aggregationService,
                                           GitHubModelsClient githubClient,
                                           EmailService emailService,
                                           HtmlTemplateService htmlTemplateService,
                                           PdfService pdfService) {
        this.aggregationService = aggregationService;
        this.githubClient = githubClient;
        this.emailService = emailService;
        this.htmlTemplateService = htmlTemplateService;
        this.pdfService = pdfService;
    }

    @Async
    @EventListener
    public void handlePremiumReportRequest(PremiumReportRequestedEvent event) {
        PremiumSalesSummaryRequest req = event.getRequest();

        // 1. Calcular agregados
        var summary = aggregationService.calculateSummary(req.getFrom(), req.getTo(), req.getBranch());

        // 2. Generar resumen IA
        String aiSummary = githubClient.generateSummary(summary);

        // 3. Generar URL de Gráfico (Pista #1 del README)
        String chartUrl = null;
        if (req.isIncludeCharts()) {
            // Usamos QuickChart.io para un gráfico simple
            chartUrl = String.format(
                    "https://quickchart.io/chart?c={type:'bar',data:{labels:['%s','%s'],datasets:[{label:'Unidades',data:[%s,%s]}]}}",
                    summary.get("topSku"), "Otros", summary.get("totalUnits"), 0 // (Simplificado)
            );
        }

        // 4. Generar HTML
        String htmlBody = htmlTemplateService.generateReportHtml(summary, aiSummary, chartUrl);

        // 5. Generar PDF (si se solicita)
        byte[] pdfBytes = null;
        if (req.isAttachPdf()) {
            pdfBytes = pdfService.generateReportPdf(summary, aiSummary);
        }

        // 6. Enviar Email
        String subject = String.format("Reporte Premium Oreo - %s a %s", req.getFrom(), req.getTo());
        emailService.sendMimeEmail(req.getEmailTo(), subject, htmlBody, pdfBytes);
    }
}