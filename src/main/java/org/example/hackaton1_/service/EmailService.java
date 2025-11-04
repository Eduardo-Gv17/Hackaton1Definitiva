package org.example.hackaton1_.service;

import org.example.hackaton1_.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Envío de correo de texto plano
    public void sendEmail(String to, String subject, String body) {
        try {
            if (to == null || to.isBlank()) {
                throw new ServiceUnavailableException("Dirección de correo destino vacía.");
            }

            System.out.println("Preparando envío de correo...");
            System.out.println("Remitente: " + from);
            System.out.println("Destinatario: " + to);
            System.out.println("Asunto: " + subject);

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);

            mailSender.send(msg);
            System.out.println("Correo enviado exitosamente a " + to);

        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            throw new ServiceUnavailableException("Error al enviar correo: " + e.getMessage());
        }
    }

    // Envío de correo HTML con adjunto PDF (para versión premium)
    public void sendMimeEmail(String to, String subject, String htmlBody, byte[] pdfAttachment) {
        try {
            if (to == null || to.isBlank()) {
                throw new ServiceUnavailableException("Dirección de correo destino vacía.");
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (pdfAttachment != null && pdfAttachment.length > 0) {
                helper.addAttachment("ReporteOreo.pdf", new ByteArrayResource(pdfAttachment));
                System.out.println("PDF adjuntado correctamente.");
            } else {
                System.out.println("No se adjuntó ningún PDF.");
            }

            mailSender.send(mimeMessage);
            System.out.println("Correo premium enviado correctamente a " + to);

        } catch (Exception e) {
            System.err.println("Error al enviar correo premium: " + e.getMessage());
            throw new ServiceUnavailableException("Error al enviar correo premium: " + e.getMessage());
        }
    }
}
