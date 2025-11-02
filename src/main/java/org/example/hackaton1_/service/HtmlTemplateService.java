package org.example.hackaton1_.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Map;

@Service
public class HtmlTemplateService {

    private final TemplateEngine templateEngine;

    public HtmlTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateReportHtml(Map<String, Object> summaryData, String aiSummary, String chartUrl) {
        Context context = new Context();
        context.setVariable("summary", summaryData);
        context.setVariable("aiSummary", aiSummary);
        context.setVariable("chartUrl", chartUrl);
        // El template se llamará 'email-template.html'
        return templateEngine.process("email-template", context);
    }
}