package scms.data.dao;

import scms.application.model.Event;
import scms.data.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for the {@code events} and {@code event_signups} tables.
 *
 * <p>All queries use {@link PreparedStatement} with bound parameters
 * (SRS-SCMS-NF-03).</p>
 */
public class EventDAO
{
    public boolean insertEvent(Event event) throws SQLException
    {
        final String sql =
            "INSERT INTO events (event_id, name, date, location, quota, current_attendees) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, event.getEventId());
            preparedStatement.setString(2, event.getName());
            preparedStatement.setDate(3, new java.sql.Date(event.getDate().getTime()));
            preparedStatement.setString(4, event.getLocation());
            preparedStatement.setInt(5, event.getQuota());
            preparedStatement.setInt(6, event.getCurrentAttendees());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Event> getUpcomingEvents(Date currentDate) throws SQLException
    {
        final String sql =
            "SELECT event_id, name, date, location, quota, current_attendees "
                + "FROM events WHERE date >= ? ORDER BY date ASC";
        List<Event> events = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setDate(1, new java.sql.Date(currentDate.getTime()));
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    Event event = new Event(
                        resultSet.getInt("event_id"),
                        resultSet.getString("name"),
                        resultSet.getDate("date"),
                        resultSet.getString("location"),
                        resultSet.getInt("quota"),
                        resultSet.getInt("current_attendees")
                    );
                    events.add(event);
                }
            }
        }
        return events;
    }

    public int getAttendeeCount() throws SQLException
    {
        final String sql = "SELECT COALESCE(SUM(current_attendees), 0) AS total_attendees FROM events";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery())
        {
            if (resultSet.next())
            {
                return resultSet.getInt("total_attendees");
            }
        }
        return 0;
    }

    public Event findEventById(int eventId) throws SQLException
    {
        final String sql =
            "SELECT event_id, name, date, location, quota, current_attendees "
                + "FROM events WHERE event_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, eventId);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    return new Event(
                        resultSet.getInt("event_id"),
                        resultSet.getString("name"),
                        resultSet.getDate("date"),
                        resultSet.getString("location"),
                        resultSet.getInt("quota"),
                        resultSet.getInt("current_attendees")
                    );
                }
            }
        }
        return null;
    }

    public boolean incrementAttendeeCount(int eventId) throws SQLException
    {
        final String sql = "UPDATE events SET current_attendees = current_attendees + 1 WHERE event_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, eventId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    /** Returns true if the member has an existing sign-up record for the event. */
    public boolean hasMemberSignedUp(int eventId, String studentId) throws SQLException
    {
        final String sql = "SELECT 1 FROM event_signups WHERE event_id = ? AND student_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, eventId);
            preparedStatement.setString(2, studentId);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                return resultSet.next();
            }
        }
    }

    /** Persists a (event_id, student_id) signup record. */
    public boolean recordSignUp(int eventId, String studentId) throws SQLException
    {
        final String sql = "INSERT INTO event_signups (event_id, student_id) VALUES (?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, eventId);
            preparedStatement.setString(2, studentId);
            return preparedStatement.executeUpdate() > 0;
        }
    }
}
