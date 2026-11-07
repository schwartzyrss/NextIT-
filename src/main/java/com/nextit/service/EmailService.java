package com.nextit.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final Properties DOT_ENV = loadDotEnv();

    public static class SmtpResult {
        private final boolean success;
        private final String message;
        private final String code;

        public SmtpResult(boolean success, String message, String code) {
            this.success = success;
            this.message = message;
            this.code = code;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
    }

    private EmailService() {}

    public static boolean isConfigured() {
        String user = getSetting("SMTP_USER", "zyrajoyce06@gmail.com");
        String pass = getSetting("SMTP_PASSWORD", getSetting("SMTP_PASS", "kcebkxcjlluabgpb"));
        return user != null && !user.isBlank() && pass != null && !pass.isBlank();
    }

    public static boolean sendOtpEmail(String toEmail, String otpCode) {
        return sendOtpEmailWithStatus(toEmail, otpCode).isSuccess();
    }

    public static SmtpResult sendOtpEmailWithStatus(String toEmail, String otpCode) {
        if (!isConfigured()) {
            log.warn("SMTP not configured in .env. Skipping real email dispatch. OTP for {}: {}", toEmail, otpCode);
            return new SmtpResult(false, "SMTP not configured: please set SMTP_USER and SMTP_PASSWORD in .env or Settings.", otpCode);
        }

        String host = getSetting("SMTP_HOST", "smtp.gmail.com").trim();
        String port = getSetting("SMTP_PORT", "587").trim();
        String username = getSetting("SMTP_USER", "zyrajoyce06@gmail.com").trim();
        String rawPassword = getSetting("SMTP_PASSWORD", getSetting("SMTP_PASS", "kceb_kxcj_llua_bgpb"));
        String password = rawPassword != null ? rawPassword.replace(" ", "").trim() : "";
        String fromAddress = getSetting("SMTP_FROM", "NextIT Verification <" + username + ">").trim();

        boolean isSsl = "465".equals(port) || "true".equalsIgnoreCase(getSetting("SMTP_SECURE", "false"));

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        if (isSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);

            InternetAddress from;
            if (fromAddress.contains("<") && fromAddress.contains(">")) {
                from = InternetAddress.parse(fromAddress)[0];
            } else {
                from = new InternetAddress(fromAddress, "NextIT Verification");
            }
            message.setFrom(from);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail.trim()));
            message.setSubject("Your NextIT Verification Code: " + otpCode);

            String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 540px; margin: 0 auto; padding: 32px 24px; background: #F1F2FA; border-radius: 24px; color: #383139;">
                  <div style="background: linear-gradient(135deg, #FFCBE1 0%, #F6A3CA 60%, #EA8CB8 100%); padding: 18px 24px; border-radius: 20px; text-align: center; margin-bottom: 24px;">
                    <h1 style="margin: 0; font-size: 24px; color: #383139; font-weight: 800;">NextIT</h1>
                    <p style="margin: 4px 0 0 0; font-size: 13px; color: #48404B; font-weight: 600;">Track Your Progress, Build Your Future</p>
                  </div>
                  <div style="background: #FFFDF7; padding: 28px 24px; border-radius: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); text-align: center;">
                    <h2 style="font-size: 20px; margin-top: 0; color: #383139;">Verify Your Account</h2>
                    <p style="font-size: 14px; color: #5E5360; line-height: 1.5;">Please use the following 6-digit confirmation code to complete your NextIT account verification:</p>
                    <div style="display: inline-block; background: #F1F2FA; padding: 14px 28px; border-radius: 16px; font-size: 32px; font-weight: 900; letter-spacing: 6px; color: #383139; margin: 20px 0; border: 2px solid #F6A3CA;">
                      {{CODE}}
                    </div>
                    <p style="font-size: 12px; color: #7A727D; margin-bottom: 0;">This code will expire in 10 minutes. If you did not request this, please ignore this email.</p>
                  </div>
                </div>
                """.replace("{{CODE}}", otpCode);

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);

            log.info("Successfully sent verification OTP code to {}", toEmail);
            return new SmtpResult(true, "Verification code sent to " + toEmail, otpCode);
        } catch (MessagingException me) {
            String err = me.getMessage() != null ? me.getMessage() : me.toString();
            log.error("Failed to send verification email to {}: {}", toEmail, err);
            if (err.contains("535") || err.toLowerCase().contains("authentication failed")) {
                err = "Google SMTP authentication failed. If using Gmail, ensure 2-Step Verification is active and use a 16-character App Password (not your normal password).";
            }
            return new SmtpResult(false, err, otpCode);
        } catch (Exception e) {
            log.error("Unexpected error sending verification email to " + toEmail, e);
            return new SmtpResult(false, e.getMessage(), otpCode);
        }
    }

    public static synchronized void updateAndPersistConfig(String host, String port, String user, String pass, String from) {
        if (host != null && !host.isBlank()) DOT_ENV.setProperty("SMTP_HOST", host.trim());
        if (port != null && !port.isBlank()) DOT_ENV.setProperty("SMTP_PORT", port.trim());
        if (user != null && !user.isBlank()) DOT_ENV.setProperty("SMTP_USER", user.trim());
        if (pass != null && !pass.isBlank()) DOT_ENV.setProperty("SMTP_PASSWORD", pass.replace(" ", "").trim());
        if (from != null && !from.isBlank()) DOT_ENV.setProperty("SMTP_FROM", from.trim());

        saveToDotEnv();
    }

    private static synchronized void saveToDotEnv() {
        Path path = Path.of(".env");
        List<String> lines = new ArrayList<>();
        if (Files.isRegularFile(path)) {
            try {
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            } catch (IOException ignored) {}
        }

        updateOrAddLine(lines, "SMTP_HOST", DOT_ENV.getProperty("SMTP_HOST", "smtp.gmail.com"));
        updateOrAddLine(lines, "SMTP_PORT", DOT_ENV.getProperty("SMTP_PORT", "587"));
        updateOrAddLine(lines, "SMTP_USER", DOT_ENV.getProperty("SMTP_USER", ""));
        updateOrAddLine(lines, "SMTP_PASSWORD", DOT_ENV.getProperty("SMTP_PASSWORD", ""));
        updateOrAddLine(lines, "SMTP_FROM", DOT_ENV.getProperty("SMTP_FROM", "NextIT Verification <no-reply@nextit.edu>"));

        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
            log.info("Successfully persisted SMTP configuration to .env");
        } catch (IOException e) {
            log.error("Failed to write .env file", e);
        }
    }

    private static void updateOrAddLine(List<String> lines, String key, String value) {
        String prefix = key + "=";
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i).trim();
            if (l.startsWith(prefix)) {
                lines.set(i, key + "=" + (value != null ? value : ""));
                return;
            }
        }
        lines.add(key + "=" + (value != null ? value : ""));
    }

    public static String getSetting(String key, String fallback) {
        String runtimeVal = DOT_ENV.getProperty(key);
        if (runtimeVal != null && !runtimeVal.isBlank()) return runtimeVal;
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env;
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) return prop;
        return fallback;
    }

    private static Properties loadDotEnv() {
        Properties properties = new Properties();
        Path path = Path.of(".env");
        if (!Files.isRegularFile(path)) return properties;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int separator = trimmed.indexOf('=');
                if (separator <= 0) continue;
                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                properties.setProperty(key, value);
            }
        } catch (IOException ignored) {}
        return properties;
    }
}
