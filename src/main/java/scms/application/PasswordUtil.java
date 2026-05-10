package scms.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Small helper around SHA-256 used for hashing user passwords.
 *
 * <p>This is intentionally simple: SHA-256 hex digest of the UTF-8 bytes of the
 * plain password. For a production system a salted/iterated KDF such as
 * BCrypt or Argon2 would be preferable; for the scope of the SCMS course
 * project SHA-256 with a fixed scheme is sufficient and deterministic, which
 * also makes seed data in {@code schema.sql} reproducible.</p>
 */
public final class PasswordUtil
{
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private PasswordUtil()
    {
    }

    /**
     * Returns the lowercase 64-character SHA-256 hex digest of {@code plain}.
     * Treats {@code null} as the empty string so callers don't need to
     * branch on null themselves.
     */
    public static String hash(String plain)
    {
        String safe = plain == null ? "" : plain;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(safe.getBytes(StandardCharsets.UTF_8));
            char[] out = new char[bytes.length * 2];
            for (int i = 0; i < bytes.length; i++)
            {
                int v = bytes[i] & 0xFF;
                out[i * 2]     = HEX[v >>> 4];
                out[i * 2 + 1] = HEX[v & 0x0F];
            }
            return new String(out);
        }
        catch (NoSuchAlgorithmException e)
        {
            // SHA-256 is mandated by every conformant JRE; if it's missing the
            // platform is broken in a way we cannot recover from at runtime.
            throw new IllegalStateException("SHA-256 not available on this JVM", e);
        }
    }

    /**
     * Constant-time-ish verification of a plain password against its stored
     * hex hash. We use String.equals — sufficient for the threat model of
     * this academic project; for a production system replace with
     * {@code MessageDigest.isEqual} on byte arrays.
     */
    public static boolean verify(String plain, String expectedHash)
    {
        if (expectedHash == null || expectedHash.isEmpty())
        {
            return false;
        }
        return hash(plain).equalsIgnoreCase(expectedHash);
    }
}
