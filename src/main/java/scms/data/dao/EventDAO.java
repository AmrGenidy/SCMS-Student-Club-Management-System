package scms.data.dao;

import scms.application.model.Event;
import scms.data.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO
{
    public boolean createEvent(Event event) throws SQLException
    {
        final String sql = "INSERT INTO events (event_id, title, date, location) VALUES (?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = connection.prepareStatement(sql))
        {
            pstmt.setInt(1, event.getEventId());
            pstmt.setString(2, event.getTitle());
            pstmt.setDate(3, new java.sql.Date(event.getDate().getTime()));
            pstmt.setString(4, event.getLocation());
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Event> fetchAllEvents() throws SQLException
    {
        final String sql = "SELECT * FROM events ORDER BY date ASC";
        List<Event> events = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
                events.add(new Event(
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getDate("date"),
                        rs.getString("location")
                ));
            }
        }
        return events;
    }
}