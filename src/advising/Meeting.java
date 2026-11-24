package advising;

public class Meeting extends Event implements IMeeting {

    private IStudent student;
 // Creates a new meeting with the given details and associated student.
    public Meeting(int eventId, String title, IDateTime start, IDateTime end, String location, IStudent student) {
        super(eventId, title, start, end, location);
        this.student = student;
    }
 // Returns the student assigned to this meeting.
    @Override
    public IStudent getStudent() {
        return student;
    }
 // Sets the student assigned to this meeting.
    @Override
    public void setStudent(IStudent student) {
        this.student = student;
    }
 // Returns true if the given student ID matches the meeting's student.
    @Override
    public boolean hasStudent(int studentId) {
        return student.getStudentId() == studentId;
    }
}