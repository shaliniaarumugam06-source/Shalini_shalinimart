package com.shalini.smartmart.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a plaintext password using SHA-256 with a unique 16-byte cryptographic salt.
     * Output format: <saltHex>:<hashHex>
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return "";
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        String hashHex = hashWithSalt(plainPassword, salt);
        return saltHex + ":" + hashHex;
    }

    /**
     * Verifies a plaintext password against a stored hashed password.
     * Supports both <salt>:<hash> and legacy plaintext passwords (with automatic match).
     */
    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.contains(":")) {
            String[] parts = storedPassword.split(":", 2);
            if (parts.length == 2 && parts[0].length() == 32) {
                try {
                    byte[] salt = HexFormat.of().parseHex(parts[0]);
                    String expectedHash = parts[1];
                    String actualHash = hashWithSalt(plainPassword, salt);
                    return MessageDigest.isEqual(expectedHash.getBytes(), actualHash.getBytes());
                } catch (Exception ignored) {
                    // fall back to string comparison if hex parsing fails
                }
            }
        }

        // Backward compatibility for legacy pre-seeded plaintext accounts
        return plainPassword.equals(storedPassword);
    }

    private static String hashWithSalt(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 digest algorithm not available", e);
        }
    }
}
