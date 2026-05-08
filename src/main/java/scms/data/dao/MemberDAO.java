package scms.data.dao;

import scms.application.model.Member;
import scms.data.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberDAO
{
    public boolean insertMember(Member member) throws SQLException
    {
        final String sql = "INSERT INTO members (name, student_id, email, role) VALUES (?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, member.getName());
            preparedStatement.setString(2, member.getStudentId());
            preparedStatement.setString(3, member.getEmail());
            preparedStatement.setString(4, member.getRole());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public Member findMemberById(String id) throws SQLException
    {
        final String sql = "SELECT name, student_id, email, role FROM members WHERE student_id = ?";
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
                            resultSet.getString("role")
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

    public java.util.List<Member> getAllMembers() throws SQLException
    {
        final String sql = "SELECT name, student_id, email, role FROM members";
        java.util.List<Member> members = new java.util.ArrayList<>();
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
