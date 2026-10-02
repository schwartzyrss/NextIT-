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
}
