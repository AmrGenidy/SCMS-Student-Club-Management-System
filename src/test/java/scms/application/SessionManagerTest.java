package scms.application;

import org.junit.jupiter.api.Test;
import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Covers STP T-SRS-SCMS-004 (role-based access) at the application layer.
 *
 * <p>Specifically verifies that the user's role <em>cannot</em> be supplied
 * by the caller — it always comes from the database row — and that login
 * fails when the supplied password does not match the stored hash.</p>
 */
class SessionManagerTest
{
    @Test
    void testLogin_Success_RoleComesFromDatabase() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);

        // The DB row says this user is a MEMBER.
        Member memberRow = new Member(
            "Alice", "12345678", "alice@test.com", "MEMBER",
            PasswordUtil.hash("hunter2")
        );
        when(memberDAO.findMemberById("12345678")).thenReturn(memberRow);

        boolean result = sessionManager.login("12345678", "hunter2");

        assertTrue(result);
        assertEquals("MEMBER", sessionManager.getCurrentUserRole(),
            "Role must come from the DB row, not from any caller-supplied value.");
        assertTrue(sessionManager.isMember());
        assertFalse(sessionManager.isAdmin());
    }

    @Test
    void testLogin_AdminRoleSurvivesAuth() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);

        Member adminRow = new Member(
            "Admin", "11111111", "admin@test.com", "ADMIN",
            PasswordUtil.hash("admin123")
        );
        when(memberDAO.findMemberById("11111111")).thenReturn(adminRow);

        assertTrue(sessionManager.login("11111111", "admin123"));
        assertTrue(sessionManager.isAdmin());
    }

    @Test
    void testLogin_WrongPassword_Fails() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);

        Member memberRow = new Member(
            "Alice", "12345678", "alice@test.com", "MEMBER",
            PasswordUtil.hash("right-password")
        );
        when(memberDAO.findMemberById("12345678")).thenReturn(memberRow);

        boolean result = sessionManager.login("12345678", "wrong-password");

        assertFalse(result);
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    void testLogin_MissingPassword_Throws()
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);

        assertThrows(IllegalArgumentException.class,
            () -> sessionManager.login("12345678", ""));
    }

    @Test
    void testLogin_MissingStudentId_Throws()
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);

        assertThrows(IllegalArgumentException.class,
            () -> sessionManager.login("", "anything"));
    }

    @Test
    void testLogin_MemberNotFound() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);

        when(memberDAO.findMemberById("99999999")).thenReturn(null);

        boolean result = sessionManager.login("99999999", "whatever");

        assertFalse(result);
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    void testLogout() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);
        Member memberRow = new Member(
            "Alice", "12345678", "alice@test.com", "MEMBER",
            PasswordUtil.hash("pw")
        );
        when(memberDAO.findMemberById("12345678")).thenReturn(memberRow);

        sessionManager.login("12345678", "pw");
        sessionManager.logout();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getCurrentUserRole());
    }
}
