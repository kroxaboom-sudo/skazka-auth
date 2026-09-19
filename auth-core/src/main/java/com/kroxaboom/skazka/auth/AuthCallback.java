package com.kroxaboom.skazka.auth;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RU: Проверяет callback до обмена authorization code на сессию.
 * EN: Validates an auth callback before exchanging the authorization code for a session.
 */
public final class AuthCallback {
    private AuthCallback() {}

    public static Result validate(
            URI callback,
            String scheme,
            String host,
            String path,
            String expectedState,
            String verifier,
            long issuedAt,
            long now,
            long ttlMillis
    ) {
        if (callback == null
                || !equalsIgnoreCase(scheme, callback.getScheme())
                || !equalsIgnoreCase(host, callback.getHost())
                || !safe(path).equals(callback.getPath())) {
            return Result.invalid(Error.WRONG_TARGET);
        }

        long age = now - issuedAt;
        if (issuedAt <= 0 || age < 0 || age > ttlMillis) {
            return Result.invalid(Error.EXPIRED);
        }

        Map<String, String> query = query(callback.getRawQuery());
        String state = query.getOrDefault("state", "");
        if (!Pkce.constantTimeEquals(safe(expectedState), state)) {
            return Result.invalid(Error.STATE_MISMATCH);
        }

        String providerError = query.getOrDefault("error", "");
        if (!providerError.isEmpty()) {
            return Result.cancelled(providerError);
        }

        if (verifier == null || verifier.length() < 43) {
            return Result.invalid(Error.INVALID_VERIFIER);
        }

        String code = query.getOrDefault("code", "");
        if (code.isEmpty() || code.length() > 4096) {
            return Result.invalid(Error.MISSING_CODE);
        }

        return Result.success(code);
    }

    private static Map<String, String> query(String rawQuery) {
        Map<String, String> values = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return values;
        }

        for (String part : rawQuery.split("&")) {
            int separator = part.indexOf('=');
            String key = separator < 0 ? part : part.substring(0, separator);
            String value = separator < 0 ? "" : part.substring(separator + 1);
            values.put(decode(key), decode(value));
        }
        return values;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static boolean equalsIgnoreCase(String expected, String actual) {
        return expected != null && actual != null && expected.equalsIgnoreCase(actual);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public enum Status {
        SUCCESS,
        CANCELLED,
        INVALID
    }

    public enum Error {
        NONE,
        WRONG_TARGET,
        EXPIRED,
        STATE_MISMATCH,
        INVALID_VERIFIER,
        MISSING_CODE,
        PROVIDER_ERROR
    }

    public record Result(Status status, Error error, String code, String providerError) {
        static Result success(String code) {
            return new Result(Status.SUCCESS, Error.NONE, code, "");
        }

        static Result cancelled(String providerError) {
            return new Result(Status.CANCELLED, Error.PROVIDER_ERROR, "", providerError);
        }

        static Result invalid(Error error) {
            return new Result(Status.INVALID, error, "", "");
        }
    }
}
