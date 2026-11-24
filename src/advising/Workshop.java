package advising;

public class Workshop extends Event implements IWorkshop {

    private LinkedList<IStudent> participants;
 // Creates a new workshop and initializes an empty participants list.
    public Workshop(int eventId, String title, IDateTime start, IDateTime end, String location) {
        super(eventId, title, start, end, location);
        participants = new LinkedList<>();
    }
 // Returns the list of all workshop participants.
    @Override
    public LinkedList<IStudent> getParticipants() {
        return participants;
    }
 // Adds a student to the workshop participants list.
    @Override
    public boolean addParticipant(IStudent student) {
        if (student == null || hasStudent(student.getStudentId()))
            return false;
        participants.insert(student);
        return true;
    }
 // Removes a participant by their student ID. Returns false if not found.
    @Override
    public boolean removeParticipantById(int studentId) {
        if (participants.empty())
            return false;

        participants.findFirst();
        while (true) {
            IStudent current = participants.retrieve();

            if (current.getStudentId() == studentId) {
                participants.remove();
                return true;
            }

            if (participants.last())
                break;

            participants.findNext();
        }

        return false;
    }
 // Returns true if the workshop has no participants.
    @Override
    public boolean isEmpty() {
        return participants.empty();
    }
 // Returns true if a student with the given ID is a participant in this workshop.
    @Override
    public boolean hasStudent(int studentId) {
        if (participants.empty())
            return false;

        participants.findFirst();
        while (true) {
            IStudent current = participants.retrieve();

            if (current.getStudentId() == studentId)
                return true;

            if (participants.last())
                break;

            participants.findNext();
        }

        return false;
    }
}