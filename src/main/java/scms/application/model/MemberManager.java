package scms.application;

import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.sql.SQLException;
import java.util.regex.Pattern;

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
}
