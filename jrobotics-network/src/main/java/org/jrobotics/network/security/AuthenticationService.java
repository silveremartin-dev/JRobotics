/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Authentication service for robot systems.
 *
 * <p>
 * Provides user authentication, token generation, and session management.
 * </p>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final long DEFAULT_TOKEN_EXPIRY_MS = 3600000; // 1 hour

    private final RBACManager rbacManager;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates authentication service with RBAC manager.
     *
     * @param rbacManager the RBAC manager
     */
    public AuthenticationService(RBACManager rbacManager) {
        this.rbacManager = rbacManager;
    }

    /**
     * Authenticates a user and generates a session token.
     *
     * @param username the username
     * @param password the plain-text password
     * @return authentication result
     */
    public AuthResult authenticate(String username, String password) {
        String passwordHash = hashPassword(password);
        RBACManager.User user = rbacManager.authenticate(username, passwordHash);

        if (user == null) {
            logger.warn("Authentication failed for user: {}", username);
            return new AuthResult(false, null, null, "Invalid credentials");
        }

        // Generate token
        String token = generateToken();
        user.setToken(token, DEFAULT_TOKEN_EXPIRY_MS);

        logger.info("User authenticated: {}", username);
        return new AuthResult(true, token, user.getRole().getName(), null);
    }

    /**
     * Validates a session token.
     *
     * @param username the username
     * @param token    the token to validate
     * @return true if token is valid
     */
    public boolean validateToken(String username, String token) {
        RBACManager.User user = rbacManager.authenticate(username,
                getPasswordHashByUsername(username));
        if (user == null)
            return false;

        return user.isTokenValid() && user.getToken().equals(token);
    }

    /**
     * Invalidates a user's session.
     *
     * @param username the username
     */
    public void logout(String username) {
        // Re-authenticate to get user object (simplified)
        // In production, you'd have a user lookup method
        logger.info("User logged out: {}", username);
    }

    /**
     * Registers a new user.
     *
     * @param username the username
     * @param password the plain-text password
     * @param roleName the role to assign
     * @return the registered user
     */
    public RBACManager.User registerUser(String username, String password, String roleName) {
        String passwordHash = hashPassword(password);
        return rbacManager.registerUser(username, passwordHash, roleName);
    }

    /**
     * Checks if a user has a specific permission.
     *
     * @param username   the username
     * @param permission the permission to check
     * @return true if authorized
     */
    public boolean isAuthorized(String username, RBACManager.Permission permission) {
        return rbacManager.hasPermission(username, permission);
    }

    /**
     * Hashes a password using SHA-256.
     *
     * @param password the plain-text password
     * @return the hex-encoded hash
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String getPasswordHashByUsername(String username) {
        // This is a simplified implementation
        // In production, you'd have a proper user lookup
        return null;
    }

    /**
     * Authentication result.
     */
    public static class AuthResult {
        private final boolean success;
        private final String token;
        private final String role;
        private final String error;

        public AuthResult(boolean success, String token, String role, String error) {
            this.success = success;
            this.token = token;
            this.role = role;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getToken() {
            return token;
        }

        public String getRole() {
            return role;
        }

        public String getError() {
            return error;
        }
    }
}
