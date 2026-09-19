import com.kroxaboom.skazka.auth.AuthBaseUrl;
import com.kroxaboom.skazka.auth.AuthCallback;
import com.kroxaboom.skazka.auth.Pkce;

import java.net.URI;

public final class AuthCoreSelfTest {
    public static void main(String[] args) {
        Pkce.Pair pair = Pkce.create();
        check(pair.verifier().length() >= 43, "verifier length");
        check(pair.challenge().length() == 43, "S256 challenge length");

        String state = Pkce.newState();
        long now = 1_700_000_000_000L;
        URI callback = URI.create("skazkahub://auth/callback?state=" + state + "&code=abc123");

        AuthCallback.Result valid = AuthCallback.validate(
                callback,
                "skazkahub",
                "auth",
                "/callback",
                state,
                pair.verifier(),
                now - 1000,
                now,
                15 * 60 * 1000L
        );
        check(valid.status() == AuthCallback.Status.SUCCESS, "valid callback");
        check("abc123".equals(valid.code()), "authorization code");

        AuthCallback.Result wrongState = AuthCallback.validate(
                callback,
                "skazkahub",
                "auth",
                "/callback",
                "different-state",
                pair.verifier(),
                now - 1000,
                now,
                15 * 60 * 1000L
        );
        check(wrongState.error() == AuthCallback.Error.STATE_MISMATCH, "state mismatch");

        AuthCallback.Result expired = AuthCallback.validate(
                callback,
                "skazkahub",
                "auth",
                "/callback",
                state,
                pair.verifier(),
                now - 20 * 60 * 1000L,
                now,
                15 * 60 * 1000L
        );
        check(expired.error() == AuthCallback.Error.EXPIRED, "expired attempt");

        check(
                "https://auth.example.com".equals(AuthBaseUrl.normalize("https://auth.example.com/")),
                "HTTPS base normalization"
        );

        boolean rejected = false;
        try {
            AuthBaseUrl.normalize("http://auth.example.com");
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, "HTTP base rejected");

        System.out.println("PASS: Skazka Auth Core PKCE/callback/base validation");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
