package scms.application;

import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.sql.SQLException;

/**
 * Tracks the currently authenticated user.
 *
 * <p>Authentication compares the SHA-256 hash of the supplied password to the
 * one stored in the database. The user's role is <em>always</em> read from the
 * database — it is never accepted as input from the login screen. This closes
 * the privilege-escalation hole that existed in the previous version, where
 * any user could elevate themselves to ADMIN by picking a role from a combo
 * box at login.</p>
 */
public class SessionManager
{
    /** Public role constants so controllers don't pass magic strings around. */
    public static final String ROLE_ADMIN  = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBER";

    private final MemberDAO memberDAO;
    private Member currentUser;

    public SessionManager()
    {
        this.memberDAO = new MemberDAO();
    }

    public SessionManager(MemberDAO memberDAO)
    {
        this.memberDAO = memberDAO;
    }

    /**
     * Authenticate using Student ID + plaintext password. The role is read
     * from the database; callers cannot influence it.
     *
     * @return true on successful login, false if the ID is unknown or the
     *         password does not match.
     */
    public boolean login(String studentId, String plainPassword) throws SQLException
    {
        if (studentId == null || studentId.isBlank())
        {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (plainPassword == null || plainPassword.isEmpty())
        {
            throw new IllegalArgumentException("Password is required");
        }

        Member member = memberDAO.findMemberById(studentId);
        if (member == null)
        {
            return false;
        }

        if (!PasswordUtil.verify(plainPassword, member.getPasswordHash()))
        {
            return false;
        }

        // Authoritative role comes from the DB row, never from the caller.
        this.currentUser = member;
        return true;
    }

    public void logout()
    {
        currentUser = null;
    }

    public boolean isLoggedIn()
    {
        return currentUser != null;
    }

    public String getCurrentUserRole()
    {
        return currentUser == null ? null : currentUser.getRole();
    }

    public Member getCurrentUser()
    {
        return currentUser;
    }

    public boolean isAdmin()
    {
        return currentUser != null && ROLE_ADMIN.equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isMember()
    {
        return currentUser != null && ROLE_MEMBER.equalsIgnoreCase(currentUser.getRole());
    }
}
