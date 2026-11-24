package advising;

public abstract class Event implements IEvent {

    protected int eventId;
    protected String title;
    protected IDateTime start;
    protected IDateTime end;
    protected String location;

    public Event(int eventId, String title, IDateTime start, IDateTime end, String location) {
        this.eventId = eventId;
        this.title = title;
        this.start = start;
        this.end = end;
        this.location = location;
    }

    @Override
    public int getEventId() {
        return eventId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public IDateTime getStartDateTime() {
        return start;
    }

    @Override
    public IDateTime getEndDateTime() {
        return end;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public int compareTo(IEvent other) {
        return this.title.compareTo(other.getTitle());
    }

    @Override
    public String toString() {
        return "Title: " + title
                + ", Start: " + start.format()
                + ", End: " + end.format()
                + ", Location: " + location;
    }
}