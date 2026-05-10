package scms.application.model;

import java.util.Date;

public class Event
{
    private int eventId;
    private String name;
    private Date date;
    private String location;
    private int quota;
    private int currentAttendees;

    public Event(int eventId, String name, Date date, String location, int quota, int currentAttendees)
    {
        this.eventId = eventId;
        this.name = name;
        this.date = date;
        this.location = location;
        this.quota = quota;
        this.currentAttendees = currentAttendees;
    }

    public int getEventId()
    {
        return eventId;
    }

    public void setEventId(int eventId)
    {
        this.eventId = eventId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public int getQuota()
    {
        return quota;
    }

    public void setQuota(int quota)
    {
        this.quota = quota;
    }

    public int getCurrentAttendees()
    {
        return currentAttendees;
    }

    public void setCurrentAttendees(int currentAttendees)
    {
        this.currentAttendees = currentAttendees;
    }
}
