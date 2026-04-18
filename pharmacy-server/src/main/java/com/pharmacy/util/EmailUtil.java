package com.pharmacy.util;

import com.pharmacy.shared.config.AppConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public final class EmailUtil {

    private EmailUtil() {
    }

    public static void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", AppConfig.get("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", AppConfig.get("mail.smtp.starttls.enable"));
        props.put("mail.smtp.host", AppConfig.get("mail.smtp.host"));
        props.put("mail.smtp.port", AppConfig.get("mail.smtp.port"));

        final String username = AppConfig.get("mail.user");
        final String password = AppConfig.get("mail.password");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConfig.get("mail.from")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Gửi email thất bại", e);
        }
    }
}

