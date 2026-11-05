package com.nextit.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
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
import java.util.Properties;

public final class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final Properties DOT_ENV = loadDotEnv();

    private EmailService() {}

    public static boolean isConfigured() {
        String user = getSetting("SMTP_USER", null);
        String pass = getSetting("SMTP_PASSWORD", null);
        return user != null && !user.isBlank() && pass != null && !pass.isBlank();
    }

    public static boolean sendOtpEmail(String toEmail, String otpCode) {
        if (!isConfigured()) {
            log.warn("SMTP not configured in .env. Skipping real email dispatch. OTP for {}: {}", toEmail, otpCode);
            return false;
        }

        String host = getSetting("SMTP_HOST", "smtp.gmail.com");
        String port = getSetting("SMTP_PORT", "587");
        String username = getSetting("SMTP_USER", "");
        String password = getSetting("SMTP_PASSWORD", "");
        String fromAddress = getSetting("SMTP_FROM", username);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress, "NextIT Verification"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your NextIT Verification Code: " + otpCode);

            String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 540px; margin: 0 auto; padding: 32px 24px; background: #F1F2FA; border-radius: 24px; color: #383139;">
                  <div style="background: linear-gradient(135deg, #FFCBE1 0%, #F6A3CA 60%, #EA8CB8 100%); padding: 18px 24px; border-radius: 20px; text-align: center; margin-bottom: 24px;">
                    <h1 style="margin: 0; font-size: 24px; color: #383139; font-weight: 800;">NextIT</h1>
                    <p style="margin: 4px 0 0 0; font-size: 13px; color: #48404B; font-weight: 600;">Track Your Progress, Build Your Future</p>
                  </div>
                  <div style="background: #FFFDF7; padding: 28px 24px; border-radius: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); text-align: center;">
                    <h2 style="font-size: 20px; margin-top: 0; color: #383139;">Verify Your Account</h2>
                    <p style="font-size: 14px; color: #5E5360; line-height: 1.5;">Please use the following 6-digit confirmation code to complete your NextIT account onboarding:</p>
                    <div style="display: inline-block; background: #F1F2FA; padding: 14px 28px; border-radius: 16px; font-size: 32px; font-weight: 900; letter-spacing: 6px; color: #383139; margin: 20px 0; border: 2px solid #F6A3CA;">
                      %s
                    </div>
                    <p style="font-size: 12px; color: #7A727D; margin-bottom: 0;">This code will expire in 10 minutes. If you did not request this, please ignore this email.</p>
                  </div>
                </div>
                """.formatted(otpCode);

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);

            log.info("Successfully sent verification OTP code to {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification email to " + toEmail, e);
            return false;
        }
    }

    private static String getSetting(String key, String fallback) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env;
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) return prop;
        String dotEnvVal = DOT_ENV.getProperty(key);
        return dotEnvVal == null || dotEnvVal.isBlank() ? fallback : dotEnvVal;
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

