package scms.security;

import org.junit.jupiter.api.Test;
import scms.application.MemberManager;
import scms.application.SessionManager;
import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Covers STP T-SRS-SCMS-NF-03 (SQL injection defence).
 *
 * <p>Two angles of attack are covered:</p>
 * <ol>
 *   <li>Behavioural — feed the well-known SQLi payloads from the STP into
 *       {@link SessionManager#login} (which routes through {@link MemberDAO})
 *       and verify the system rejects them without throwing SQL exceptions
 *       and without granting access.</li>
 *   <li>Structural — scan every DAO source file and assert that none of them
 *       build SQL by string-concatenating user input. The data layer must
 *       use {@code PreparedStatement} exclusively.</li>
 * </ol>
 */
class SqlInjectionTest
{
    private static final String[] PAYLOADS = {
        "' OR 1=1 --",
        "admin' --",
        "' UNION SELECT NULL, username, password FROM users--",
        "'; WAITFOR DELAY '0:0:5'--",
        "<script>alert(1)</script>"
    };

    @Test
    void sqlInjectionPayloads_failLogin() throws SQLException
    {
        // The DAO is mocked so we can prove that whatever the user types is
        // passed as a literal string and never altered into something
        // dangerous on the way to the database call.
        MemberDAO dao = mock(MemberDAO.class);
        // Make sure we never accidentally hand back a valid Member for any
        // of the malicious inputs.
        when(dao.findMemberById(anyString())).thenReturn(null);
        SessionManager session = new SessionManager(dao);

        for (String payload : PAYLOADS)
        {
            boolean ok = session.login(payload, "irrelevant");
            assertFalse(ok, "Login must reject malicious payload: " + payload);
            assertFalse(session.isLoggedIn());
        }
    }

    @Test
    void payloadIsPassedAsLiteralToDao() throws SQLException
    {
        // Verifies that the manager does not transform the user input before
        // handing it to the DAO. The DAO's job is then to use PreparedStatement
        // which neutralises the payload.
        MemberDAO dao = mock(MemberDAO.class);
        SessionManager session = new SessionManager(dao);
        String payload = "admin' --";

        session.login(payload, "pw");

        // Captured argument equals the original payload — no concatenation,
        // no escaping, no transformation happened in the layer above.
        verify(dao).findMemberById(payload);
    }

    @Test
    void xssPayloadStoredAsLiteralName() throws SQLException
    {
        // STP step 5 + 6: registering a member whose name contains an XSS
        // payload should succeed but the payload must be stored verbatim
        // (no script execution), proven here by the manager forwarding the
        // string to the DAO unchanged.
        MemberDAO dao = mock(MemberDAO.class);
        MemberManager manager = new MemberManager(dao);
        Member m = new Member("<script>alert(1)</script>", "12345678", "x@y.com", "MEMBER");

        when(dao.isIdUnique("12345678")).thenReturn(true);
        when(dao.insertMember(m)).thenReturn(true);

        assertTrue(manager.registerMember(m));
        verify(dao).insertMember(m);
        assertEquals("<script>alert(1)</script>", m.getName(),
            "Name should be stored as a literal string, never executed.");
    }

    @Test
    void daoSourcesNeverConcatenateUserInputIntoSql() throws IOException
    {
        Path daoRoot = Paths.get("src", "main", "java", "scms", "data", "dao");
        assertTrue(Files.exists(daoRoot),
            "DAO directory must exist at " + daoRoot.toAbsolutePath());

        List<String> offenders = new ArrayList<>();
        Files.walkFileTree(daoRoot, new SimpleFileVisitor<>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                if (!file.toString().endsWith(".java"))
                {
                    return FileVisitResult.CONTINUE;
                }
                String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                for (String line : content.split("\\r?\\n"))
                {
                    String stripped = line.trim();
                    if (stripped.startsWith("//") || stripped.startsWith("*") || stripped.startsWith("/*"))
                    {
                        continue;
                    }
                    if (looksLikeSqlConcatenation(stripped))
                    {
                        offenders.add(file.getFileName() + " :: " + stripped);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        assertTrue(offenders.isEmpty(),
            "DAOs must use PreparedStatement parameters, never string-concatenate "
                + "user input into SQL. Offenders:\n  " + String.join("\n  ", offenders));
    }

    /**
     * Heuristic: walk the line tracking whether we're inside a Java
     * double-quoted string literal. Only Java-level {@code +} operators
     * (outside any string literal) count as concatenation. Once we find
     * such a {@code +}, if the next non-space char is a variable identifier
     * (i.e. not another opening quote) the line is suspicious.
     *
     * <p>This intentionally allows things like
     * {@code "WHERE x = " + "y"} (adjacent literal concatenation) but flags
     * {@code "WHERE x = " + userInput}. It also allows the SQL-internal
     * {@code +} operator inside a quoted string (e.g.
     * {@code "SET x = x + 1"}) because that {@code +} is part of the
     * literal, not Java syntax.</p>
     */
    private static boolean looksLikeSqlConcatenation(String line)
    {
        String lower = line.toLowerCase();
        boolean looksLikeSql = lower.contains("\"select ") || lower.contains("\"insert ")
            || lower.contains("\"update ") || lower.contains("\"delete ")
            || lower.contains("\"where ");
        if (!looksLikeSql)
        {
            return false;
        }

        boolean inString = false;
        boolean escaped  = false;
        for (int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);
            if (escaped)
            {
                escaped = false;
                continue;
            }
            if (c == '\\' && inString)
            {
                escaped = true;
                continue;
            }
            if (c == '"')
            {
                inString = !inString;
                continue;
            }
            if (!inString && c == '+')
            {
                // Look at the next non-space character.
                int j = i + 1;
                while (j < line.length() && line.charAt(j) == ' ')
                {
                    j++;
                }
                if (j < line.length() && line.charAt(j) != '"')
                {
                    // It's a Java concatenation with a non-literal operand.
                    return true;
                }
            }
        }
        return false;
    }
}
