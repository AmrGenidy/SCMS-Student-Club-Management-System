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

/**
 * Covers STP T-SRS-SCMS-001 (happy path) and T-SRS-SCMS-005 (length + duplicate).
 */
class MemberManagerTest
{
    @Test
    void testMemberRegistration_Success() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        MemberManager memberManager = new MemberManager(memberDAO);
        Member member = new Member("Alice Stone", "12345678", "alice@scms.edu", "MEMBER",
            PasswordUtil.hash("pw1"));

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
    void testMemberRegistration_NonNumericId()
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        MemberManager memberManager = new MemberManager(memberDAO);
        Member member = new Member("Bob Reed", "abcdefgh", "bob@scms.edu", "MEMBER");

        assertThrows(IllegalArgumentException.class,
            () -> memberManager.registerMember(member));
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

    @Test
    void testMemberRegistration_NullMember_Throws()
    {
        MemberManager memberManager = new MemberManager(mock(MemberDAO.class));

        assertThrows(IllegalArgumentException.class,
            () -> memberManager.registerMember(null));
    }

    // ----- deleteMember -----

    @Test
    void testDeleteMember_Success() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        MemberManager memberManager = new MemberManager(memberDAO);
        Member existing = new Member("Bob", "12345678", "bob@x.com", "MEMBER");

        when(memberDAO.findMemberById("12345678")).thenReturn(existing);
        when(memberDAO.deleteMember("12345678")).thenReturn(true);

        assertTrue(memberManager.deleteMember("12345678", "99999999"));
        verify(memberDAO).deleteMember("12345678");
    }

    @Test
    void testDeleteMember_RejectsSelfDelete()
    {
        MemberManager memberManager = new MemberManager(mock(MemberDAO.class));

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> memberManager.deleteMember("11111111", "11111111")
        );
        assertEquals("You cannot delete your own account", ex.getMessage());
    }

    @Test
    void testDeleteMember_NotFound_Throws() throws SQLException
    {
        MemberDAO memberDAO = mock(MemberDAO.class);
        MemberManager memberManager = new MemberManager(memberDAO);

        when(memberDAO.findMemberById("00000000")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> memberManager.deleteMember("00000000", "11111111")
        );
        assertEquals("Member not found", ex.getMessage());
    }

    @Test
    void testDeleteMember_NullId_Throws()
    {
        MemberManager memberManager = new MemberManager(mock(MemberDAO.class));

        assertThrows(IllegalArgumentException.class,
            () -> memberManager.deleteMember(null, "11111111"));
        assertThrows(IllegalArgumentException.class,
            () -> memberManager.deleteMember("  ", "11111111"));
    }
}
