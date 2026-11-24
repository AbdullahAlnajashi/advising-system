package Phase2;

public class Meeting extends Event implements IMeeting {

    private int advisorId;
    private int studentId;

    public Meeting(int id, ITimeSlot timeSlot, ILocation location, int advisorId, int studentId) {

        super(id,timeSlot,location,new BSTSet<Integer>());

        this.advisorId = advisorId;
        this.studentId = studentId;

        participantIds.insert(advisorId);
        participantIds.insert(studentId);
    }

    @Override
    public int getAdvisorId() {
        return advisorId;
    }

    @Override
    public int getStudentId() {
        return studentId;
    }
}