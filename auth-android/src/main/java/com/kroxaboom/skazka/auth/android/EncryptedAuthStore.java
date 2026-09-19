package com.kroxaboom.skazka.auth.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.kroxaboom.skazka.auth.AuthBaseUrl;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * RU: Хранит bearer token и pending PKCE локально; пароли и cookies провайдера сюда не попадают.
 * EN: Stores the bearer token and pending PKCE state locally; provider passwords and cookies never enter this store.
 */
public final class EncryptedAuthStore {
    private static final String TOKEN = "sessionToken";

    private final Context context;
    private final SharedPreferences preferences;
    private final String keyAlias;

    public EncryptedAuthStore(Context context, String preferencesName, String keyAlias) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }
        if (preferencesName == null || preferencesName.isBlank()) {
            throw new IllegalArgumentException("Preferences name must not be blank");
        }
        if (keyAlias == null || keyAlias.isBlank()) {
            throw new IllegalArgumentException("Key alias must not be blank");
        }

        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        this.keyAlias = keyAlias;
    }

    public boolean hasLiveSession(long now) {
        return preferences.getLong("expiresAt", 0L) > now && preferences.contains(TOKEN);
    }

    public Session session() {
        String encoded = preferences.getString(TOKEN, "");
        if (encoded.isEmpty()) {
            return null;
        }

        try {
            String[] parts = encoded.split("\\.", 2);
            if (parts.length != 2) {
                throw new IllegalStateException("Invalid token envelope");
            }

            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP | Base64.URL_SAFE);
            byte[] encrypted = Base64.decode(parts[1], Base64.NO_WRAP | Base64.URL_SAFE);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            cipher.updateAAD(context.getPackageName().getBytes(StandardCharsets.UTF_8));

            String token = new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
            return new Session(
                    preferences.getString("sessionBase", ""),
                    token,
                    preferences.getLong("expiresAt", 0L)
            );
        } catch (Exception damaged) {
            clearSession();
            return null;
        }
    }

    public void saveSession(String base, String token, long expiresAt) throws Exception {
        String normalizedBase = AuthBaseUrl.normalize(base);
        if (token == null || token.length() < 20 || token.length() > 16000
                || token.indexOf('\n') >= 0 || token.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Invalid session token");
        }
        if (expiresAt <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Session is already expired");
        }

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key());
        cipher.updateAAD(context.getPackageName().getBytes(StandardCharsets.UTF_8));

        byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
        String envelope = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP | Base64.URL_SAFE)
                + "."
                + Base64.encodeToString(encrypted, Base64.NO_WRAP | Base64.URL_SAFE);

        boolean committed = preferences.edit()
                .putString(TOKEN, envelope)
                .putString("sessionBase", normalizedBase)
                .putLong("expiresAt", expiresAt)
                .commit();

        if (!committed) {
            throw new IllegalStateException("Failed to persist auth session");
        }
    }

    public void savePending(String base, String state, String verifier, String provider, long issuedAt) {
        String normalizedBase = AuthBaseUrl.normalize(base);
        if (state == null || state.isBlank() || verifier == null || verifier.length() < 43) {
            throw new IllegalArgumentException("Invalid pending PKCE state");
        }

        preferences.edit()
                .putString("pendingBase", normalizedBase)
                .putString("pendingState", state)
                .putString("pendingVerifier", verifier)
                .putString("pendingProvider", provider == null ? "" : provider)
                .putLong("pendingAt", issuedAt)
                .apply();
    }

    public Pending pending() {
        String state = preferences.getString("pendingState", "");
        if (state.isEmpty()) {
            return null;
        }

        return new Pending(
                preferences.getString("pendingBase", ""),
                state,
                preferences.getString("pendingVerifier", ""),
                preferences.getString("pendingProvider", ""),
                preferences.getLong("pendingAt", 0L)
        );
    }

    public void clearPending() {
        preferences.edit()
                .remove("pendingBase")
                .remove("pendingState")
                .remove("pendingVerifier")
                .remove("pendingProvider")
                .remove("pendingAt")
                .apply();
    }

    public void clearSession() {
        preferences.edit()
                .remove(TOKEN)
                .remove("sessionBase")
                .remove("expiresAt")
                .apply();
    }

    public void clearAll() {
        clearSession();
        clearPending();
    }

    private SecretKey key() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);

        java.security.Key existing = store.getKey(keyAlias, null);
        if (existing instanceof SecretKey secretKey) {
            return secretKey;
        }

        KeyGenerator generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
        );
        generator.init(new KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    public record Session(String base, String token, long expiresAt) {}

    public record Pending(String base, String state, String verifier, String provider, long issuedAt) {}
}
