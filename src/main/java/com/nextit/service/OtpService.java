package com.nextit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final long OTP_TTL_MS = 10 * 60 * 1000L; // 10 minutes
    private static final long COOLDOWN_MS = 45 * 1000L;      // 45 seconds
    private static final int MAX_FAILED_ATTEMPTS = 5;

    public record OtpRecord(String code, long expiresAt, long resendAvailableAt, int[] failedAttempts) {
        public OtpRecord(String code, long expiresAt, long resendAvailableAt) {
            this(code, expiresAt, resendAvailableAt, new int[]{0});
        }
    }

    public record OtpResponse(boolean sent, boolean configured, String code, boolean rateLimited, String message) {}

    private static final Map<String, OtpRecord> ACTIVE_OTPS = new ConcurrentHashMap<>();

    private OtpService() {}

    public static OtpResponse requestOtp(String email) {
        String cleanEmail = (email != null) ? email.trim().toLowerCase() : "";
        if (cleanEmail.isBlank()) {
            return new OtpResponse(false, false, "", false, "Email address cannot be empty.");
        }

        long now = System.currentTimeMillis();
        OtpRecord existing = ACTIVE_OTPS.get(cleanEmail);
        if (existing != null && now < existing.resendAvailableAt()) {
            long waitSec = (existing.resendAvailableAt() - now) / 1000;
            return new OtpResponse(false, EmailService.isConfigured(), existing.code(), true,
                    "Please wait " + waitSec + " seconds before requesting another code.");
        }

        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        ACTIVE_OTPS.put(cleanEmail, new OtpRecord(code, now + OTP_TTL_MS, now + COOLDOWN_MS));

        EmailService.SmtpResult result = EmailService.sendOtpEmailWithStatus(cleanEmail, code);
        boolean sent = result.isSuccess();
        boolean configured = EmailService.isConfigured();

        log.info("Requested OTP for {}: code={}, sent={}, configured={}, msg={}", cleanEmail, code, sent, configured, result.getMessage());
        return new OtpResponse(sent, configured, code, false, result.getMessage());
    }

    public static boolean verifyOtp(String email, String enteredCode) {
        if (email == null || enteredCode == null) return false;
        String cleanEmail = email.trim().toLowerCase();
        String code = enteredCode.trim();
        long now = System.currentTimeMillis();

        // Sandbox / Development fallback code
        if (!EmailService.isConfigured() && ("123456".equals(code) || "000000".equals(code))) {
            ACTIVE_OTPS.remove(cleanEmail);
            return true;
        }

        OtpRecord record = ACTIVE_OTPS.get(cleanEmail);
        if (record == null) {
            if (!EmailService.isConfigured() && ("123456".equals(code) || "000000".equals(code))) {
                return true;
            }
            return false;
        }

        if (now > record.expiresAt() || record.failedAttempts()[0] >= MAX_FAILED_ATTEMPTS) {
            ACTIVE_OTPS.remove(cleanEmail);
            return false;
        }

        if (record.code().equals(code) || (!EmailService.isConfigured() && ("123456".equals(code) || "000000".equals(code)))) {
            ACTIVE_OTPS.remove(cleanEmail); // Single use
            return true;
        }

        record.failedAttempts()[0]++;
        if (record.failedAttempts()[0] >= MAX_FAILED_ATTEMPTS) {
            ACTIVE_OTPS.remove(cleanEmail);
        }
        return false;
    }
}
