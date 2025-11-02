package org.example.hackaton1_.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.example.hackaton1_.exception.ServiceUnavailableException;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfService {

    public byte[] generateReportPdf(Map<String, Object> summaryData, String aiSummary) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("--- Reporte Semanal Oreo ---"));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Resumen del Analista IA:"));
            document.add(new Paragraph(aiSummary));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("--- Datos Clave ---"));
            document.add(new Paragraph("Unidades Totales: " + summaryData.get("totalUnits")));
            document.add(new Paragraph(String.format("Ingresos Totales: S/ %.2f", (Double) summaryData.get("totalRevenue"))));
            document.add(new Paragraph("SKU Top: " + summaryData.get("topSku")));
            document.add(new Paragraph("Sucursal Top: " + summaryData.get("topBranch")));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ServiceUnavailableException("Error al generar PDF: " + e.getMessage());
        }
    }
}