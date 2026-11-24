package advising;

public class EventList implements IEventList {

    private LinkedList<IEvent> list;

    public EventList() {
        list = new LinkedList<>();
    }
    /*
     * Inserts events alphabetically by title.
     */
    @Override
    public boolean addEvent(IEvent event) {
        if (list.empty()) {
            list.insert(event);
            return true;
        }
        list.findFirst();
        while (true) {
            IEvent current =list.retrieve();
            if (current.getTitle().compareTo(event.getTitle()) > 0) {

                IEvent temp =list.retrieve();
                list.update(event);
                list.insert(temp);
                return true;
            }
            if (list.last())
                break;
            list.findNext();
        }
        list.insert(event);
        return true;
    }

    
 // Removes an event from the list by its unique event ID. Returns false if not found.
    @Override
    public boolean removeEventById(int eventId) {
        if (list.empty())
            return false;

        list.findFirst();
        while (true) {
            IEvent current = list.retrieve();

            if (current.getEventId() == eventId) {
                list.remove();
                return true;
            }

            if (list.last())
                break;

            list.findNext();
        }

        return false;
    }
 // Returns the full list of events in alphabetical order.
    @Override
    public LinkedList<IEvent> getAllAlphabetically() {
        return list;
    }
 // Returns the total number of events stored.
    @Override
    public int size() {
        int count = 0;

        if (list.empty())
            return 0;

        list.findFirst();
        while (true) {
            count++;

            if (list.last())
                break;

            list.findNext();
        }

        return count;
    }

 // Returns all events whose title matches the given title (case-insensitive).
    @Override
    public LinkedList<IEvent> findByTitle(String title) {

        LinkedList<IEvent> result =
                new LinkedList<>();

        if (list.empty())
            return result;

        list.findFirst();

        while (true) {

            IEvent current =
                    list.retrieve();

            if (current.getTitle()
                    .equalsIgnoreCase(title)) {

                result.insert(current);
            }

            if (list.last())
                break;

            list.findNext();
        }

        return result;
    }
 // Returns all events involving a student with the given full name.
    @Override
    public LinkedList<IEvent> findByStudentName(String studentFullName) {
        LinkedList<IEvent> result =new LinkedList<>();

        if (list.empty())
            return result;
        list.findFirst();
        while (true) {
            IEvent current =list.retrieve();
            if (current instanceof Meeting) {
                Meeting meeting =(Meeting) current;

                if (meeting.getStudent().getName().equalsIgnoreCase(studentFullName)) {
                    result.insert(current);
                }
            }
            else if (current instanceof Workshop) {
                Workshop workshop =(Workshop) current;
                LinkedList<IStudent> participants =workshop.getParticipants();
                if (!participants.empty()) {
                    participants.findFirst();
                    while (true) {
                        IStudent student =participants.retrieve();
                        if (student.getName().equalsIgnoreCase(studentFullName)) {
                            result.insert(current);
                            break;
                        }

                        if (participants.last())
                            break;

                       participants.findNext();
                }
        }
         }
            if (list.last())
                break;
            list.findNext();
        }
        return result;
    }
}