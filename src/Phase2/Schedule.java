package Phase2;

public class Schedule implements ISchedule {

    private BSTSet<Integer> eventIds;
    private BSTMap<ITimeSlot, Integer> events;
    private BSTMap<Integer, ITimeSlot> slotsByEventId;

    public Schedule() {

        eventIds = new BSTSet<Integer>();
        events = new BSTMap<ITimeSlot, Integer>();
        slotsByEventId =new BSTMap<Integer, ITimeSlot>();
    }

    @Override
    public int size() {
        return eventIds.size();
    }

    @Override
    public boolean empty() {
        return eventIds.empty();
    }

    @Override
    public void clear() {

        eventIds.clear();
        events.clear();
        slotsByEventId.clear();
    }

    @Override
    public boolean contains(int eventId) {
        return eventIds.contains(eventId);
    }

    @Override
    public boolean conflicts(ITimeSlot timeSlot) {
        // TimeSlot.compareTo returns 0 on overlap, so a lookup finds any overlapping slot.
        return events.get(timeSlot) != null;
    }
    @Override
    public boolean add(int eventId, ITimeSlot timeSlot) {

        if (contains(eventId))
            return false;

        if (conflicts(timeSlot))
            return false;

        eventIds.insert(eventId);

        events.insert(timeSlot, eventId);
        
        slotsByEventId.insert(eventId,timeSlot);

        return true;
    }
    /*
     * Removes an event from the schedule
     * using direct BSTMap lookup by eventId.
     */
    @Override
    public boolean remove(int eventId) {

        ITimeSlot slot =slotsByEventId.get(eventId);

        if (slot == null)
            return false;

        events.remove(slot);

        slotsByEventId.remove(eventId);

        eventIds.remove(eventId);

        return true;
    }
    @Override
    public Set<Integer> getEventIds() {
        return eventIds;
    }

    @Override
    public Map<ITimeSlot, Integer> getEvents() {
        return events;
    }
}