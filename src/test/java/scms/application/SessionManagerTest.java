package scms.application;

import org.junit.jupiter.api.Test;
import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionManagerTest
{
    @Test
    void testLogin_Success() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);
        Member member = new Member("Alice", "12345678", "alice@test.com", "MEMBER");

        when(memberDAO.findMemberById("12345678")).thenReturn(member);

        boolean result = sessionManager.login("12345678", "MEMBER");

        assertTrue(result);
        assertEquals(member, sessionManager.getCurrentUser());
        assertEquals("MEMBER", sessionManager.getCurrentUserRole());
    }

    @Test
    void testLogin_InvalidRole()
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);

        assertThrows(IllegalArgumentException.class, () -> {
            sessionManager.login("12345678", "GUEST");
        });
    }

    @Test
    void testLogin_MemberNotFound() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);

        when(memberDAO.findMemberById("99999999")).thenReturn(null);

        boolean result = sessionManager.login("99999999", "ADMIN");

        assertFalse(result);
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    void testLogout() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        SessionManager sessionManager = new SessionManager(memberDAO);
        Member member = new Member("Alice", "12345678", "alice@test.com", "MEMBER");

        when(memberDAO.findMemberById("12345678")).thenReturn(member);
        sessionManager.login("12345678", "MEMBER");

        sessionManager.logout();

        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getCurrentUserRole());
    }
}
