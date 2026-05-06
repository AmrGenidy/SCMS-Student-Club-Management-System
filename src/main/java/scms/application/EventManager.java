package scms.application;

import scms.application.exception.OverQuotaException;
import scms.application.model.Event;
import scms.data.dao.EventDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

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

        Date now = new Date();
        if (event.getDate() == null || event.getDate().before(now))
        {
            throw new IllegalArgumentException("Cannot schedule an event in the past");
        }

        return eventDAO.insertEvent(event);
    }

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
}