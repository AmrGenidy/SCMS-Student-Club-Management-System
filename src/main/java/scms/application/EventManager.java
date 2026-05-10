package scms.application;

import scms.application.exception.OverQuotaException;
import scms.application.model.Event;
import scms.data.dao.EventDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Business logic around club events: creation, sign-ups and listing.
 *
 * <p>Past-date detection is done at <em>day</em> granularity (not full
 * timestamp). The previous implementation used {@code date.before(new Date())}
 * which rejected any event scheduled for today because midnight today is
 * "before" the wall-clock right now.</p>
 */
public class EventManager
{
    private final EventDAO eventDAO;

    public EventManager()
    {
        this.eventDAO = new EventDAO();
    }

    public EventManager(EventDAO eventDAO)
    {
        this.eventDAO = eventDAO;
    }

    public boolean createEvent(Event event) throws SQLException
    {
        if (event == null)
        {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (event.getName() == null || event.getName().isBlank())
        {
            throw new IllegalArgumentException("Event name is required");
        }
        if (event.getLocation() == null || event.getLocation().isBlank())
        {
            throw new IllegalArgumentException("Event location is required");
        }
        if (event.getQuota() <= 0)
        {
            throw new IllegalArgumentException("Event quota must be greater than zero");
        }
        if (event.getDate() == null)
        {
            throw new IllegalArgumentException("Event date is required");
        }

        LocalDate eventDay = toLocalDate(event.getDate());
        LocalDate today    = LocalDate.now();
        if (eventDay.isBefore(today))
        {
            throw new IllegalArgumentException("Cannot schedule an event in the past");
        }

        return eventDAO.insertEvent(event);
    }

    /**
     * Records that {@code memberStudentId} signed up to {@code eventId}.
     * Enforces (a) quota and (b) one-sign-up-per-member.
     */
    public boolean signMemberUp(int eventId, String memberStudentId)
        throws SQLException, OverQuotaException
    {
        if (memberStudentId == null || memberStudentId.isBlank())
        {
            throw new IllegalArgumentException("Member Student ID is required");
        }

        Event event = eventDAO.findEventById(eventId);
        if (event == null)
        {
            throw new IllegalArgumentException("Event not found");
        }

        if (eventDAO.hasMemberSignedUp(eventId, memberStudentId))
        {
            throw new IllegalStateException("You are already signed up for this event");
        }

        if (event.getCurrentAttendees() >= event.getQuota())
        {
            throw new OverQuotaException("Event is full");
        }

        eventDAO.recordSignUp(eventId, memberStudentId);
        return eventDAO.incrementAttendeeCount(eventId);
    }

    /**
     * Legacy single-arg sign-up kept for backward compatibility with tests
     * that don't care about per-member tracking. It still applies the quota
     * check but cannot enforce duplicate prevention.
     */
    public boolean signMemberUp(int eventId) throws SQLException, OverQuotaException
    {
        Event event = eventDAO.findEventById(eventId);
        if (event == null)
        {
            throw new IllegalArgumentException("Event not found");
        }
        if (event.getCurrentAttendees() >= event.getQuota())
        {
            throw new OverQuotaException("Event is full");
        }
        return eventDAO.incrementAttendeeCount(eventId);
    }

    public List<Event> getUpcomingEvents(Date currentDate) throws SQLException
    {
        return eventDAO.getUpcomingEvents(currentDate);
    }

    private static LocalDate toLocalDate(Date date)
    {
        if (date instanceof java.sql.Date)
        {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
