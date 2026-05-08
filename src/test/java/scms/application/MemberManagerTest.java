package scms.application;

import org.junit.jupiter.api.Test;
import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberManagerTest
{
    @Test
    void testMemberRegistration_Success() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        MemberManager memberManager = new MemberManager(memberDAO);
        Member member = new Member("Alice Stone", "12345678", "alice@scms.edu", "MEMBER");

        when(memberDAO.isIdUnique("12345678")).thenReturn(true);
        when(memberDAO.insertMember(member)).thenReturn(true);

        boolean result = memberManager.registerMember(member);

        assertTrue(result);
        verify(memberDAO).isIdUnique("12345678");
        verify(memberDAO).insertMember(member);
    }

    @Test
    void testMemberRegistration_InvalidIdLength()
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        MemberManager memberManager = new MemberManager(memberDAO);
        Member member = new Member("Bob Reed", "12345", "bob@scms.edu", "MEMBER");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> memberManager.registerMember(member)
        );

        assertEquals("Student ID must be exactly 8 numeric digits", exception.getMessage());
    }

    @Test
    void testMemberRegistration_DuplicateId() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        MemberManager memberManager = new MemberManager(memberDAO);
        Member member = new Member("Cara Lane", "87654321", "cara@scms.edu", "MEMBER");

        when(memberDAO.isIdUnique("87654321")).thenReturn(false);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> memberManager.registerMember(member)
        );

        assertEquals("Student ID already exists", exception.getMessage());
        verify(memberDAO).isIdUnique("87654321");
    }
}