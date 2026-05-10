package scms.application;

import org.junit.jupiter.api.Test;
import scms.application.exception.OverQuotaException;
import scms.application.model.Event;
import scms.data.dao.EventDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Covers STP T-SRS-SCMS-002 (creation) and T-SRS-SCMS-006 (past date / over-quota / duplicate).
 */
class EventManagerTest
{
    private static Date date(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void testEventCreation_TodayAllowed() throws SQLException
    {
        // Regression test: midnight-today was previously rejected because
        // it was technically "before now". An event scheduled for today
        // is in the present, not the past, and must be accepted.
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);
        Event todayEvent = new Event(1, "Lunch", date(LocalDate.now()), "Cafeteria", 20, 0);

        when(eventDAO.insertEvent(todayEvent)).thenReturn(true);

        assertTrue(eventManager.createEvent(todayEvent));
        verify(eventDAO).insertEvent(todayEvent);
    }

    @Test
    void testEventCreation_FutureDateAllowed() throws SQLException
    {
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);
        Event future = new Event(2, "Future Talk", date(LocalDate.now().plusDays(7)),
            "Auditorium", 100, 0);

        when(eventDAO.insertEvent(future)).thenReturn(true);

        assertTrue(eventManager.createEvent(future));
    }

    @Test
    void testEventCreation_PastDateRejected()
    {
        // STP T-SRS-SCMS-006 Input A
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);
        Event past = new Event(1, "Old Event", date(LocalDate.of(2020, 1, 1)),
            "Room A", 50, 0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> eventManager.createEvent(past)
        );

        assertEquals("Cannot schedule an event in the past", exception.getMessage());
    }

    @Test
    void testEventCreation_NullFields_Rejected()
    {
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);

        assertThrows(IllegalArgumentException.class,
            () -> eventManager.createEvent(null));
        assertThrows(IllegalArgumentException.class,
            () -> eventManager.createEvent(new Event(1, "", date(LocalDate.now()), "Hall", 10, 0)));
        assertThrows(IllegalArgumentException.class,
            () -> eventManager.createEvent(new Event(1, "X", date(LocalDate.now()), "", 10, 0)));
        assertThrows(IllegalArgumentException.class,
            () -> eventManager.createEvent(new Event(1, "X", date(LocalDate.now()), "Hall", 0, 0)));
    }

    @Test
    void testEventSignUp_OverQuotaRejected() throws SQLException
    {
        // STP T-SRS-SCMS-006 Input B
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);
        Event fullEvent = new Event(7, "Hackathon", date(LocalDate.now().plusDays(1)),
            "Main Hall", 1, 1);

        when(eventDAO.findEventById(7)).thenReturn(fullEvent);
        when(eventDAO.hasMemberSignedUp(7, "11111111")).thenReturn(false);

        OverQuotaException exception = assertThrows(
            OverQuotaException.class,
            () -> eventManager.signMemberUp(7, "11111111")
        );

        assertEquals("Event is full", exception.getMessage());
    }

    @Test
    void testEventSignUp_DuplicateRejected() throws SQLException
    {
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);
        Event event = new Event(8, "Workshop", date(LocalDate.now().plusDays(1)),
            "Lab", 10, 1);

        when(eventDAO.findEventById(8)).thenReturn(event);
        when(eventDAO.hasMemberSignedUp(8, "22222222")).thenReturn(true);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> eventManager.signMemberUp(8, "22222222")
        );

        assertEquals("You are already signed up for this event", exception.getMessage());
    }

    @Test
    void testEventSignUp_HappyPath() throws Exception
    {
        EventDAO eventDAO = mock(EventDAO.class);
        EventManager eventManager = new EventManager(eventDAO);
        Event event = new Event(9, "Concert", date(LocalDate.now().plusDays(2)),
            "Stadium", 100, 5);

        when(eventDAO.findEventById(9)).thenReturn(event);
        when(eventDAO.hasMemberSignedUp(9, "33333333")).thenReturn(false);
        when(eventDAO.recordSignUp(9, "33333333")).thenReturn(true);
        when(eventDAO.incrementAttendeeCount(9)).thenReturn(true);

        assertTrue(eventManager.signMemberUp(9, "33333333"));
        verify(eventDAO).recordSignUp(9, "33333333");
        verify(eventDAO).incrementAttendeeCount(9);
    }
}
