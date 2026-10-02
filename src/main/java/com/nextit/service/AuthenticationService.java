package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.User;
import com.nextit.repository.UserRepository;
import com.nextit.security.Session;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank() || password == null || password.isBlank()) {
            throw new AppException("Username and password are required.");
        }
        Optional<User> found = userRepository.findByUsername(usernameOrEmail.trim());
        if (found.isEmpty()) {
            found = findByEmail(usernameOrEmail.trim());
        }
        User user = found.orElseThrow(() -> new AppException("Invalid credentials."));
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new AppException("Invalid credentials.");
        }
        Session.login(user);
        return user;
    }

    public void logout() {
        Session.logout();
    }

    public static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public static boolean verifyPassword(String plain, String hash) {
        return hash != null && plain != null && BCrypt.checkpw(plain, hash);
    }

    private Optional<User> findByEmail(String email) {
        return userRepository.findAll().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }
}
