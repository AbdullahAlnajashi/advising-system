package Phase2;

public class AdvisingSystemPhase2 implements IAdvisingSystemPhase2 {

    private BSTSet<IAdvisor> advisors;
    private BSTSet<IStudent> students;
    private BSTSet<ILocation> locations;

    private BSTSet<IMeeting> meetings;
    private BSTSet<IWorkshop> workshops;
    private BSTSet<IEvent> events;

    private int nextEventId;

    public AdvisingSystemPhase2() {
        advisors = new BSTSet<IAdvisor>();
        students = new BSTSet<IStudent>();
        locations = new BSTSet<ILocation>();

        meetings = new BSTSet<IMeeting>();
        workshops = new BSTSet<IWorkshop>();
        events = new BSTSet<IEvent>();

        nextEventId = 1;
    }

    @Override
    public Set<IAdvisor> getAdvisors() {
        return advisors;
    }

    @Override
    public Set<IStudent> getStudents() {
        return students;
    }

    @Override
    public Set<ILocation> getLocations() {
        return locations;
    }

    @Override
    public Set<IMeeting> getMeetings() {
        return meetings;
    }

    @Override
    public Set<IWorkshop> getWorkshops() {
        return workshops;
    }

    @Override
    public Set<IEvent> getEvents() {
        return events;
    }

    // ------------------------------------------------------------------ people & locations

    @Override
    public boolean addAdvisor(IAdvisor advisor) {
        return advisor != null && advisor.getSchedule() != null && advisors.insert(advisor);
    }

    // All lookups below use a "probe" object with the wanted ID, since the sets compare by ID only.
    @Override
    public IAdvisor searchAdvisorById(int advisorId) {
        return advisors.find(new Advisor(advisorId, null, null, null, null));
    }

    @Override
    public boolean addStudent(IStudent student) {
        return student != null && student.getSchedule() != null && students.insert(student);
    }

    @Override
    public IStudent searchStudentById(int studentId) {
        return students.find(new Student(studentId, null, null, null, null));
    }

    @Override
    public boolean addLocation(ILocation location) {
        if (location == null || (location.isReservable() && location.getSchedule() == null))
            return false;
        return locations.insert(location);
    }

    @Override
    public ILocation searchLocationById(int locationId) {
        return locations.find(new Location(locationId, null, false, false, 0, null));
    }

    private IWorkshop findWorkshop(int workshopId) {
        return workshops.find(new Workshop(workshopId, null, null, null));
    }

    private IMeeting findMeeting(int meetingId) {
        return meetings.find(new Meeting(meetingId, null, null, 0, 0));
    }

    @Override
    public boolean deleteStudent(int studentId) {
        IStudent student = searchStudentById(studentId);
        if (student == null)
            return false;

        // Iterate over a snapshot of the student's own event IDs instead of every event in the system.
        List<Integer> eventIds = student.getSchedule().getEventIds().getKeys();
        if (!eventIds.empty()) {
            eventIds.findFirst();
            while (true) {
                int eventId = eventIds.retrieve();
                if (findMeeting(eventId) != null) {
                    cancelMeeting(eventId);
                } else if (findWorkshop(eventId) != null) {
                    try {
                        removeStudentFromWorkshop(eventId, studentId);
                    } catch (SchedulingException e) {
                        // cannot happen: the student is in this workshop
                    }
                }
                if (eventIds.last())
                    break;
                eventIds.findNext();
            }
        }

        students.remove(student);
        return true;
    }

    // ------------------------------------------------------------------ scheduling

    @Override
    public int scheduleMeeting(ITimeSlot timeSlot, int advisorId, int studentId) throws SchedulingException {
        if (timeSlot == null)
            throw new IllegalArgumentException("timeSlot must not be null");

        IAdvisor advisor = searchAdvisorById(advisorId);
        if (advisor == null)
            throw new SchedulingException(ScheduleFailureReason.ADVISOR_NOT_FOUND);

        IStudent student = searchStudentById(studentId);
        if (student == null)
            throw new SchedulingException(ScheduleFailureReason.STUDENT_NOT_FOUND);

        if (advisor.getSchedule().conflicts(timeSlot))
            throw new SchedulingException(ScheduleFailureReason.CONFLICT_ADVISOR);

        if (student.getSchedule().conflicts(timeSlot))
            throw new SchedulingException(ScheduleFailureReason.CONFLICT_STUDENT);

        int id = nextEventId++;
        Meeting meeting = new Meeting(id, timeSlot, advisor.getOffice(), advisorId, studentId);

        meetings.insert(meeting);
        events.insert(meeting);
        advisor.getSchedule().add(id, timeSlot);
        student.getSchedule().add(id, timeSlot);
        return id;
    }

    @Override
    public int scheduleWorkshop(String title, ITimeSlot timeSlot, int locationId, int[] advisorIds,
                                int[] studentIds) throws SchedulingException {
        if (timeSlot == null)
            throw new IllegalArgumentException("timeSlot must not be null");
        if (advisorIds == null)
            advisorIds = new int[0];
        if (studentIds == null)
            studentIds = new int[0];

        ILocation location = searchLocationById(locationId);
        if (location == null)
            throw new SchedulingException(ScheduleFailureReason.LOCATION_NOT_FOUND);
        if (!location.isReservable())
            throw new SchedulingException(ScheduleFailureReason.LOCATION_NOT_RESERVABLE);
        if (location.getSchedule().conflicts(timeSlot))
            throw new SchedulingException(ScheduleFailureReason.CONFLICT_LOCATION);

        // Validate everyone first (collecting unique IDs) so a failure changes nothing.
        BSTSet<Integer> advisorSet = new BSTSet<Integer>();
        for (int advisorId : advisorIds) {
            IAdvisor advisor = searchAdvisorById(advisorId);
            if (advisor == null)
                throw new SchedulingException(ScheduleFailureReason.ADVISOR_NOT_FOUND);
            if (advisor.getSchedule().conflicts(timeSlot))
                throw new SchedulingException(ScheduleFailureReason.CONFLICT_ADVISOR);
            advisorSet.insert(advisorId);
        }

        BSTSet<Integer> studentSet = new BSTSet<Integer>();
        for (int studentId : studentIds) {
            IStudent student = searchStudentById(studentId);
            if (student == null)
                throw new SchedulingException(ScheduleFailureReason.STUDENT_NOT_FOUND);
            if (student.getSchedule().conflicts(timeSlot))
                throw new SchedulingException(ScheduleFailureReason.CONFLICT_STUDENT);
            studentSet.insert(studentId);
        }

        if (advisorSet.empty() && studentSet.empty())
            throw new IllegalArgumentException("a workshop needs at least one participant");

        if (exceedsCapacity(location, advisorSet.size() + studentSet.size()))
            throw new SchedulingException(ScheduleFailureReason.CAPACITY_EXCEEDED);

        // Commit.
        int id = nextEventId++;
        Workshop workshop = new Workshop(id, title, timeSlot, location);

        for (int advisorId : advisorIds) {
            if (workshop.getAdvisorIds().insert(advisorId)) {
                workshop.getParticipantIds().insert(advisorId);
                searchAdvisorById(advisorId).getSchedule().add(id, timeSlot);
            }
        }
        for (int studentId : studentIds) {
            if (workshop.getStudentIds().insert(studentId)) {
                workshop.getParticipantIds().insert(studentId);
                searchStudentById(studentId).getSchedule().add(id, timeSlot);
            }
        }

        workshops.insert(workshop);
        events.insert(workshop);
        location.getSchedule().add(id, timeSlot);
        return id;
    }

    /** Capacity -1 means unlimited. */
    private static boolean exceedsCapacity(ILocation location, int participants) {
        int capacity = location.getCapacity();
        return capacity >= 0 && participants > capacity;
    }

    // ------------------------------------------------------------------ cancellation

    @Override
    public boolean cancelMeeting(int meetingId) {
        IMeeting meeting = findMeeting(meetingId);
        if (meeting == null)
            return false;

        meetings.remove(meeting);
        events.remove(meeting);

        IAdvisor advisor = searchAdvisorById(meeting.getAdvisorId());
        if (advisor != null)
            advisor.getSchedule().remove(meetingId);

        IStudent student = searchStudentById(meeting.getStudentId());
        if (student != null)
            student.getSchedule().remove(meetingId);

        return true;
    }

    @Override
    public boolean cancelWorkshop(int workshopId) {
        IWorkshop workshop = findWorkshop(workshopId);
        if (workshop == null)
            return false;

        workshops.remove(workshop);
        events.remove(workshop);

        // Release the room.
        ILocation location = workshop.getLocation();
        if (location != null && location.getSchedule() != null)
            location.getSchedule().remove(workshopId);

        List<Integer> advisorList = workshop.getAdvisorIds().getKeys();
        if (!advisorList.empty()) {
            advisorList.findFirst();
            while (true) {
                IAdvisor advisor = searchAdvisorById(advisorList.retrieve());
                if (advisor != null)
                    advisor.getSchedule().remove(workshopId);
                if (advisorList.last())
                    break;
                advisorList.findNext();
            }
        }

        List<Integer> studentList = workshop.getStudentIds().getKeys();
        if (!studentList.empty()) {
            studentList.findFirst();
            while (true) {
                IStudent student = searchStudentById(studentList.retrieve());
                if (student != null)
                    student.getSchedule().remove(workshopId);
                if (studentList.last())
                    break;
                studentList.findNext();
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ workshop membership

    private IWorkshop requireWorkshop(int workshopId) throws SchedulingException {
        IWorkshop workshop = findWorkshop(workshopId);
        if (workshop == null)
            throw new SchedulingException(ScheduleFailureReason.EVENT_NOT_FOUND);
        return workshop;
    }

    @Override
    public void addStudentToWorkshop(int workshopId, int studentId) throws SchedulingException {
        IStudent student = searchStudentById(studentId);
        if (student == null)
            throw new SchedulingException(ScheduleFailureReason.STUDENT_NOT_FOUND);

        IWorkshop workshop = requireWorkshop(workshopId);

        // Already a participant: the workshop is on the student's schedule, i.e. a conflict.
        if (workshop.getStudentIds().contains(studentId)
                || student.getSchedule().conflicts(workshop.getTimeSlot()))
            throw new SchedulingException(ScheduleFailureReason.CONFLICT_STUDENT);

        if (exceedsCapacity(workshop.getLocation(), workshop.getParticipantIds().size() + 1))
            throw new SchedulingException(ScheduleFailureReason.CAPACITY_EXCEEDED);

        workshop.getStudentIds().insert(studentId);
        workshop.getParticipantIds().insert(studentId);
        student.getSchedule().add(workshopId, workshop.getTimeSlot());
    }

    @Override
    public void removeStudentFromWorkshop(int workshopId, int studentId) throws SchedulingException {
        IWorkshop workshop = requireWorkshop(workshopId);

        if (!workshop.getStudentIds().contains(studentId))
            throw new SchedulingException(ScheduleFailureReason.STUDENT_NOT_FOUND);

        workshop.getStudentIds().remove(studentId);
        workshop.getParticipantIds().remove(studentId);

        IStudent student = searchStudentById(studentId);
        if (student != null)
            student.getSchedule().remove(workshopId);

        if (workshop.getStudentIds().empty() && workshop.getAdvisorIds().empty())
            cancelWorkshop(workshopId);
    }

    @Override
    public void addAdvisorToWorkshop(int workshopId, int advisorId) throws SchedulingException {
        IAdvisor advisor = searchAdvisorById(advisorId);
        if (advisor == null)
            throw new SchedulingException(ScheduleFailureReason.ADVISOR_NOT_FOUND);

        IWorkshop workshop = requireWorkshop(workshopId);

        if (workshop.getAdvisorIds().contains(advisorId)
                || advisor.getSchedule().conflicts(workshop.getTimeSlot()))
            throw new SchedulingException(ScheduleFailureReason.CONFLICT_ADVISOR);

        if (exceedsCapacity(workshop.getLocation(), workshop.getParticipantIds().size() + 1))
            throw new SchedulingException(ScheduleFailureReason.CAPACITY_EXCEEDED);

        workshop.getAdvisorIds().insert(advisorId);
        workshop.getParticipantIds().insert(advisorId);
        advisor.getSchedule().add(workshopId, workshop.getTimeSlot());
    }

    @Override
    public void removeAdvisorFromWorkshop(int workshopId, int advisorId) throws SchedulingException {
        IWorkshop workshop = requireWorkshop(workshopId);

        if (searchAdvisorById(advisorId) == null || !workshop.getAdvisorIds().contains(advisorId))
            throw new SchedulingException(ScheduleFailureReason.ADVISOR_NOT_FOUND);

        workshop.getAdvisorIds().remove(advisorId);
        workshop.getParticipantIds().remove(advisorId);
        searchAdvisorById(advisorId).getSchedule().remove(workshopId);

        if (workshop.getStudentIds().empty() && workshop.getAdvisorIds().empty())
            cancelWorkshop(workshopId);
    }
}
