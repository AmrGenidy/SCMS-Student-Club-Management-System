package scms.application;

import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.sql.SQLException;

public class SessionManager
{
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

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

    public boolean login(String studentId, String role) throws SQLException
    {
        if (!ROLE_ADMIN.equalsIgnoreCase(role) && !ROLE_MEMBER.equalsIgnoreCase(role))
        {
            throw new IllegalArgumentException("Role must be ADMIN or MEMBER");
        }

        Member member = memberDAO.findMemberById(studentId);
        if (member == null)
        {
            return false;
        }

        currentUser = member;
        currentUser.setRole(role.toUpperCase());
        return true;
    }

    public void logout()
    {
        currentUser = null;
    }

    public String getCurrentUserRole()
    {
        return currentUser == null ? null : currentUser.getRole();
    }

    public Member getCurrentUser()
    {
        return currentUser;
    }
}
