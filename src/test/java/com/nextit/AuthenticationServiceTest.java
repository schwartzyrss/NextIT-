package com.nextit;

import com.nextit.service.AuthenticationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    @Test
    void hashAndVerifyPassword() {
        String hash = AuthenticationService.hashPassword("password123");
        assertNotNull(hash);
        assertNotEquals("password123", hash);
        assertTrue(AuthenticationService.verifyPassword("password123", hash));
        assertFalse(AuthenticationService.verifyPassword("wrong", hash));
    }

    @Test
    void demoSeedHashMatchesDocumentedPassword() {
        String seedHash = "$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO";
        assertTrue(AuthenticationService.verifyPassword("password123", seedHash));
    }

    @Test
    void malformedStoredHashFailsVerification() {
        assertFalse(AuthenticationService.verifyPassword("password123", "not-a-bcrypt-hash"));
        assertFalse(AuthenticationService.verifyPassword("password123", null));
    }
}
