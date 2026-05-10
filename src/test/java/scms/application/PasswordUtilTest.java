package scms.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest
{
    @Test
    void hash_isDeterministicAndLowercaseHex()
    {
        String h1 = PasswordUtil.hash("admin123");
        String h2 = PasswordUtil.hash("admin123");

        assertEquals(h1, h2, "Hashing the same input must return the same digest");
        assertEquals(64, h1.length(), "SHA-256 hex must be 64 characters long");
        assertTrue(h1.matches("[0-9a-f]{64}"));
    }

    @Test
    void hash_changesWhenInputChanges()
    {
        assertNotEquals(PasswordUtil.hash("foo"), PasswordUtil.hash("Foo"));
        assertNotEquals(PasswordUtil.hash("foo"), PasswordUtil.hash("foo "));
    }

    @Test
    void verify_acceptsMatching()
    {
        String stored = PasswordUtil.hash("hunter2");
        assertTrue(PasswordUtil.verify("hunter2", stored));
    }

    @Test
    void verify_rejectsMismatching()
    {
        String stored = PasswordUtil.hash("hunter2");
        assertFalse(PasswordUtil.verify("hunter3", stored));
    }

    @Test
    void verify_rejectsNullOrEmptyStored()
    {
        assertFalse(PasswordUtil.verify("anything", null));
        assertFalse(PasswordUtil.verify("anything", ""));
    }

    @Test
    void verify_knownAdminHashMatchesAdmin123()
    {
        // Sanity check: keep schema.sql seed data in sync with this hash.
        String storedInSchema = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        assertTrue(PasswordUtil.verify("admin123", storedInSchema),
            "Seed admin password must hash to the value in schema.sql");
    }
}
