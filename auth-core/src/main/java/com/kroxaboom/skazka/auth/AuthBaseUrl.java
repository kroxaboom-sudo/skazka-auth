package com.kroxaboom.skazka.auth;

import java.net.URI;

/**
 * RU: Нормализует только безопасный HTTPS base URL без path/query/fragment.
 * EN: Normalizes a safe HTTPS base URL with no path/query/fragment.
 */
public final class AuthBaseUrl {
    private AuthBaseUrl() {}

    public static String normalize(String value) {
        String candidate = value == null ? "" : value.trim();
        while (candidate.endsWith("/")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }

        try {
            URI uri = URI.create(candidate);
            String path = uri.getPath();
            boolean rootPath = path == null || path.isEmpty() || "/".equals(path);

            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null
                    || uri.getQuery() != null
                    || uri.getFragment() != null
                    || !rootPath) {
                throw new IllegalArgumentException("Auth base must be a root HTTPS URL");
            }
            return candidate;
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Auth base must be a root HTTPS URL", error);
        }
    }
}
