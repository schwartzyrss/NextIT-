package com.nextit;

import com.nextit.model.Role;
import com.nextit.model.User;
import com.nextit.security.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTest {

    @AfterEach
    void clearSession() {
        Session.logout();
    }

    @Test
    void startsLoggedOut() {
        assertFalse(Session.isLoggedIn());
        assertNull(Session.currentUser());
    }

    @Test
    void loginStoresTheCurrentUser() {
        User user = new User();
        user.setUserId(7);
        user.setFirstName("Maria");
        user.setLastName("Santos");
        user.setRole(Role.STUDENT);

        Session.login(user);

        assertTrue(Session.isLoggedIn());
        assertSame(user, Session.currentUser());
        assertEquals("Maria Santos", Session.currentUser().getFullName());
        assertEquals(Role.STUDENT, Session.currentUser().getRole());
    }

    @Test
    void logoutClearsTheCurrentUser() {
        Session.login(new User());
        Session.logout();

        assertFalse(Session.isLoggedIn());
        assertNull(Session.currentUser());
    }

    @Test
    void everyRoleIsRetainedForRoleBasedNavigation() {
        for (Role role : Role.values()) {
            User user = new User();
            user.setRole(role);
            Session.login(user);
            assertEquals(role, Session.currentUser().getRole());
        }
    }
}
