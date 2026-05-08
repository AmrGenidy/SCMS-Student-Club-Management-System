package scms.application;

import org.junit.jupiter.api.Test;
import scms.application.exception.OverQuotaException;
import scms.application.model.Event;
import scms.data.dao.EventDAO;

import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventManagerTest
{
    @Test
    void testEventCreation_PastDateRejected()
    {
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);
        Date yesterday = new Date(System.currentTimeMillis() - 24L * 60L * 60L * 1000L);
        Event event = new Event(1, "Retro Meeting", yesterday, "Room A", 10, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventManager.createEvent(event)
        );

        assertEquals("Cannot schedule an event in the past", exception.getMessage());
    }

    @Test
    void testEventSignUp_OverQuotaRejected() throws SQLException
    {
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);
        Date tomorrow = new Date(System.currentTimeMillis() + 24L * 60L * 60L * 1000L);
        Event fullEvent = new Event(7, "Hackathon", tomorrow, "Main Hall", 1, 1);

        when(eventDAO.findEventById(7)).thenReturn(fullEvent);

        OverQuotaException exception = assertThrows(
                OverQuotaException.class,
                () -> eventManager.signMemberUp(7)
        );

        assertEquals("Event is full", exception.getMessage());
    }
}