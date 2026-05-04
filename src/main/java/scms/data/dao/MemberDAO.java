package scms.data.dao;

import scms.application.model.Member;
import scms.data.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {
    public boolean addMember(Member member) throws SQLException
    {
        final String sql = "INSERT INTO members (member_id, name, email) VALUES (?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, member.getMemberId());
            preparedStatement.setString(2, member.getName());
            preparedStatement.setString(3, member.getEmail());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Member> fetchAllMembers() throws SQLException {
        final String sql = "SELECT member_id, name, email FROM members";
        List<Member> members = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery())
        {
            while (resultSet.next())
            {
                members.add(new Member(
                        resultSet.getInt("member_id"),
                        resultSet.getString("name"),
                        resultSet.getString("email")
                ));
            }
        }
        return members;
    }
}