package com.kroxaboom.skazka.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * RU: Генерация PKCE/state без provider-specific логики.
 * EN: PKCE/state generation without provider-specific logic.
 */
public final class Pkce {
    private static final SecureRandom RANDOM = new SecureRandom();

    private Pkce() {}

    public static Pair create() {
        String verifier = randomToken(48);
        return new Pair(verifier, challenge(verifier));
    }

    public static String newState() {
        return randomToken(32);
    }

    public static String challenge(String verifier) {
        if (verifier == null || verifier.length() < 43) {
            throw new IllegalArgumentException("PKCE verifier is too short");
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }

    public static boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null || expected.length() != actual.length()) {
            return false;
        }

        int diff = 0;
        for (int i = 0; i < expected.length(); i++) {
            diff |= expected.charAt(i) ^ actual.charAt(i);
        }
        return diff == 0;
    }

    private static String randomToken(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public record Pair(String verifier, String challenge) {}
}
