package com.nextit.security;

import com.nextit.model.User;

public final class Session {
    private static User currentUser;

    private Session() {}

    public static void login(User user) { currentUser = user; }
    public static void logout() { currentUser = null; }
    public static User currentUser() { return currentUser; }
    public static boolean isLoggedIn() { return currentUser != null; }
}
