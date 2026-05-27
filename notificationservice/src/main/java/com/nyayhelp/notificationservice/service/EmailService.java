package com.nyayhelp.notificationservice.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailTemplates templates;

    @Value("${nyayhelp.notification.from}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public boolean send(String to, String event, java.util.Map<String, Object> data) {
        if (to == null || to.isBlank()) {
            log.warn("Skipping email - no recipient (event={})", event);
            return false;
        }
        if (mailUsername == null || mailUsername.isBlank()) {
            log.warn("Skipping email - MAIL_USERNAME not configured (event={}, to={})", event, to);
            return false;
        }

        EmailTemplates.Rendered rendered = templates.render(event, data);
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(rendered.subject);
            helper.setText(rendered.body, true);
            mailSender.send(msg);
            log.info("Email sent (event={}, to={})", event, to);
            return true;
        } catch (Exception e) {
            log.error("Email send failed (event={}, to={}): {}", event, to, e.getMessage());
            return false;
        }
    }
}
