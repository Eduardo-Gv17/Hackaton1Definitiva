package org.example.hackaton1_.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.example.hackaton1_.exception.ServiceUnavailableException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;


@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_USERNAME}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }

    public void sendMimeEmail(String to, String subject, String htmlBody, byte[] pdfAttachment) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // true = multipart (para adjuntos), "UTF-8" para encoding
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = es HTML

            if (pdfAttachment != null) {
                // Adjuntar el PDF
                helper.addAttachment("ReporteOreo.pdf", new ByteArrayResource(pdfAttachment));
            }

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Error al enviar email premium: " + e.getMessage());
        }
    }
}
