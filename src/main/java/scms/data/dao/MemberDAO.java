package scms.data.dao;

import scms.application.model.Member;
import scms.data.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code members} table.
 *
 * <p>Every method uses {@link PreparedStatement} with bound parameters
 * (SRS-SCMS-NF-03) so user-controlled values are always treated as data,
 * never as SQL syntax.</p>
 */
public class MemberDAO
{
    public boolean insertMember(Member member) throws SQLException
    {
        final String sql =
            "INSERT INTO members (name, student_id, email, role, password_hash) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, member.getName());
            preparedStatement.setString(2, member.getStudentId());
            preparedStatement.setString(3, member.getEmail());
            preparedStatement.setString(4, member.getRole());
            preparedStatement.setString(5, member.getPasswordHash() == null ? "" : member.getPasswordHash());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public Member findMemberById(String id) throws SQLException
    {
        final String sql =
            "SELECT name, student_id, email, role, password_hash "
                + "FROM members WHERE student_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    return new Member(
                        resultSet.getString("name"),
                        resultSet.getString("student_id"),
                        resultSet.getString("email"),
                        resultSet.getString("role"),
                        resultSet.getString("password_hash")
                    );
                }
            }
        }
        return null;
    }

    public boolean isIdUnique(String id) throws SQLException
    {
        final String sql = "SELECT COUNT(*) FROM members WHERE student_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    return resultSet.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    public boolean deleteMember(String studentId) throws SQLException
    {
        final String sql = "DELETE FROM members WHERE student_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, studentId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Member> getAllMembers() throws SQLException
    {
        // Intentionally excludes password_hash from the projection so it can
        // never end up rendered in a JavaFX TableView by accident.
        final String sql = "SELECT name, student_id, email, role FROM members";
        List<Member> members = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery())
        {
            while (resultSet.next())
            {
                members.add(new Member(
                    resultSet.getString("name"),
                    resultSet.getString("student_id"),
                    resultSet.getString("email"),
                    resultSet.getString("role")
                ));
            }
        }
        return members;
    }
}
