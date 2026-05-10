package scms.application;

import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Business logic around club members.
 *
 * <p>Enforces SRS-SCMS-001.2 (8 numeric digit Student ID) and SRS-SCMS-001.3
 * (uniqueness) at this layer so the rules are testable in isolation, then
 * delegates persistence to {@link MemberDAO}.</p>
 */
public class MemberManager
{
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^\\d{8}$");

    private final MemberDAO memberDAO;

    public MemberManager()
    {
        this.memberDAO = new MemberDAO();
    }

    public MemberManager(MemberDAO memberDAO)
    {
        this.memberDAO = memberDAO;
    }

    public boolean registerMember(Member member) throws SQLException
    {
        if (member == null)
        {
            throw new IllegalArgumentException("Member cannot be null");
        }

        String studentId = member.getStudentId();
        if (studentId == null || !STUDENT_ID_PATTERN.matcher(studentId).matches())
        {
            throw new IllegalArgumentException("Student ID must be exactly 8 numeric digits");
        }

        if (!memberDAO.isIdUnique(studentId))
        {
            throw new IllegalStateException("Student ID already exists");
        }

        return memberDAO.insertMember(member);
    }

    public List<Member> getAllMembers() throws SQLException
    {
        return memberDAO.getAllMembers();
    }

    public Member findMemberById(String studentId) throws SQLException
    {
        return memberDAO.findMemberById(studentId);
    }

    /**
     * Permanently delete the member with the given Student ID.
     *
     * <p>To prevent an administrator from accidentally locking themselves out
     * of the system, {@code currentUserStudentId} is checked against the
     * target; deleting yourself is rejected.</p>
     *
     * @param studentId           the Student ID of the member to delete
     * @param currentUserStudentId the Student ID of the admin issuing the call
     * @return {@code true} if a row was removed
     * @throws IllegalArgumentException if {@code studentId} is missing or
     *         the member doesn't exist
     * @throws IllegalStateException    if {@code studentId} equals
     *         {@code currentUserStudentId}
     */
    public boolean deleteMember(String studentId, String currentUserStudentId) throws SQLException
    {
        if (studentId == null || studentId.isBlank())
        {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (studentId.equals(currentUserStudentId))
        {
            throw new IllegalStateException("You cannot delete your own account");
        }
        if (memberDAO.findMemberById(studentId) == null)
        {
            throw new IllegalArgumentException("Member not found");
        }
        return memberDAO.deleteMember(studentId);
    }
}
