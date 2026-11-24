package Phase2;

/** Small demo of the Phase 2 advising system: one meeting and one workshop. */
public class MainPhase2 {

    public static void main(String[] args) {
        AdvisingSystemPhase2 system = new AdvisingSystemPhase2();

        ILocation office = new Location(2, "Office B12", false, false, 1, null);
        ILocation room = new Location(1, "Room A101", true, false, 30, new Schedule());

        IAdvisor advisor = new Advisor(100, "Dr. Ahmed", "ahmed@example.com", new Schedule(), office);
        IStudent student1 = new Student(200, "Ali", "ali@example.com", new Schedule(), "CS");
        IStudent student2 = new Student(201, "Sara", "sara@example.com", new Schedule(), "SE");

        system.addLocation(office);
        system.addLocation(room);
        system.addAdvisor(advisor);
        system.addStudent(student1);
        system.addStudent(student2);

        ITimeSlot meetingSlot = new TimeSlot(new DateTime(2026, 5, 20, 10, 0), new DateTime(2026, 5, 20, 11, 0));
        try {
            int meetingId = system.scheduleMeeting(meetingSlot, 100, 200);
            System.out.println("Meeting scheduled successfully!");
            System.out.println("Meeting ID: " + meetingId);
        } catch (SchedulingException e) {
            System.out.println("Meeting failed: " + e.getReason());
        }

        ITimeSlot workshopSlot = new TimeSlot(new DateTime(2026, 5, 20, 12, 0), new DateTime(2026, 5, 20, 13, 0));
        try {
            int workshopId = system.scheduleWorkshop("AI Workshop", workshopSlot, 1,
                    new int[]{100}, new int[]{200, 201});
            System.out.println("Workshop scheduled successfully!");
            System.out.println("Workshop ID: " + workshopId);
        } catch (SchedulingException e) {
            System.out.println("Workshop failed: " + e.getReason());
        }

        // A second meeting at the same time must be rejected.
        try {
            system.scheduleMeeting(meetingSlot, 100, 201);
        } catch (SchedulingException e) {
            System.out.println("Overlapping meeting rejected: " + e.getReason());
        }
    }
}
