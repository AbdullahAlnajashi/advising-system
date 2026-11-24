package Phase2;

public class TimeSlot implements ITimeSlot {

    private IDateTime start;
    private IDateTime end;

    public TimeSlot(IDateTime start, IDateTime end) {
        if (start == null || end == null)
            throw new IllegalArgumentException("start and end must not be null");
        if (start.compareTo(end) >= 0)
            throw new IllegalArgumentException("start must be before end");
        this.start = start;
        this.end = end;
    }

    @Override
    public IDateTime getStart() {
        return start;
    }

    @Override
    public IDateTime getEnd() {
        return end;
    }

    @Override
    public int compareTo(ITimeSlot other) {

        if (this.end.compareTo(other.getStart()) <= 0)
            return -1;

        if (this.start.compareTo(other.getEnd()) >= 0)
            return 1;

        return 0;
    }
}