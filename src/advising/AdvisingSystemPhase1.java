package advising;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AdvisingSystemPhase1 implements IAdvisingSystem {

    private StudentList students;
    private EventList events;
    private int nextEventId = 1;

    public AdvisingSystemPhase1() {
        students = new StudentList();
        events = new EventList();
    }

    // Adds a student to the system. Null students and duplicate IDs are rejected.
    @Override
    public boolean addStudent(IStudent student) {
        return students.add(student);
    }

    // Searches for a student by their unique ID.
    @Override
    public IStudent searchStudentById(int studentId) {
        return students.findById(studentId);
    }

    // Searches for a student by their email address.
    @Override
    public IStudent searchStudentByEmail(String email) {
        return students.findByEmail(email);
    }

    // Prints all students stored in the system.
    @Override
    public void printAllStudents() {
        printAll(students.getAll());
    }

    // ------------------------------------------------------------------ CSV loading

    // Loads student records from a CSV file.
    // Malformed rows and duplicate IDs are skipped; returns false only if the file cannot be read.
    @Override
    public boolean loadStudentsFromCSV(String studentsFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(studentsFilePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(",", -1); // -1 keeps trailing empty fields (e.g. empty notes)
                if (parts.length < 6)
                    continue;
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    int yearLevel = Integer.parseInt(parts[4].trim());
                    students.add(new Student(parts[1].trim(), id, parts[2].trim(), parts[3].trim(),
                            yearLevel, parts[5].trim()));
                } catch (NumberFormatException e) {
                    // skip malformed row
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Loads events from a CSV file. Each row links one student to an event; workshop rows that
    // share an event_id are merged into one workshop. Rows that reference unknown students, are
    // malformed, or would create a schedule conflict for the student are skipped.
    @Override
    public boolean loadEventsFromCSV(String eventsFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(eventsFilePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] data = line.split(",", -1);
                if (data.length < 7)
                    continue;
                try {
                    loadEventRow(data);
                } catch (RuntimeException e) {
                    // skip malformed row
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void loadEventRow(String[] data) {
        int eventId = Integer.parseInt(data[0].trim());
        String title = data[1].trim();
        String type = data[2].trim();
        int studentId = Integer.parseInt(data[3].trim());
        IDateTime start = parseDateTime(data[4]);
        IDateTime end = parseDateTime(data[5]);
        String location = data[6].trim();

        IStudent student = students.findById(studentId);
        if (student == null || !isValidInterval(start, end))
            return;

        // Generated IDs must never collide with IDs read from the file.
        if (eventId >= nextEventId)
            nextEventId = eventId + 1;

        IEvent existing = findEventById(eventId);

        if (type.equalsIgnoreCase("Meeting")) {
            if (existing != null || hasConflict(student, start, end))
                return;
            Meeting meeting = new Meeting(eventId, title, start, end, location, student);
            events.addEvent(meeting);
            student.getSchedule().insert(meeting);
        } else if (type.equalsIgnoreCase("Workshop")) {
            if (existing != null && !(existing instanceof Workshop))
                return;
            Workshop workshop = (Workshop) existing;
            if (workshop != null && workshop.hasStudent(studentId))
                return;
            IDateTime wStart = workshop == null ? start : workshop.getStartDateTime();
            IDateTime wEnd = workshop == null ? end : workshop.getEndDateTime();
            if (hasConflict(student, wStart, wEnd))
                return;
            if (workshop == null) {
                workshop = new Workshop(eventId, title, start, end, location);
                events.addEvent(workshop);
            }
            workshop.addParticipant(student);
            student.getSchedule().insert(workshop);
        }
    }

    // Parses "M/D/YYYY H:MM".
    private static IDateTime parseDateTime(String text) {
        String[] p = text.trim().split("[/ :]+");
        return new DateTime(Integer.parseInt(p[2]), Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                Integer.parseInt(p[3]), Integer.parseInt(p[4]));
    }

    // ------------------------------------------------------------------ helpers

    private static boolean isValidInterval(IDateTime start, IDateTime end) {
        return start != null && end != null && start.compareTo(end) < 0;
    }

    // True if [start, end) overlaps any event already in the student's schedule.
    private static boolean hasConflict(IStudent student, IDateTime start, IDateTime end) {
        LinkedList<IEvent> schedule = student.getSchedule();
        if (schedule.empty())
            return false;
        schedule.findFirst();
        while (true) {
            IEvent e = schedule.retrieve();
            if (start.compareTo(e.getEndDateTime()) < 0 && e.getStartDateTime().compareTo(end) < 0)
                return true;
            if (schedule.last())
                return false;
            schedule.findNext();
        }
    }

    private IEvent findEventById(int eventId) {
        LinkedList<IEvent> all = events.getAllAlphabetically();
        if (all.empty())
            return null;
        all.findFirst();
        while (true) {
            IEvent e = all.retrieve();
            if (e.getEventId() == eventId)
                return e;
            if (all.last())
                return null;
            all.findNext();
        }
    }

    private static <T> void printAll(LinkedList<T> list) {
        if (list.empty())
            return;
        list.findFirst();
        while (true) {
            System.out.println(list.retrieve());
            if (list.last())
                break;
            list.findNext();
        }
    }

    // ------------------------------------------------------------------ queries

    // Prints all students in the given academic year level.
    @Override
    public void printStudentsByYearLevel(int yearLevel) {
        printAll(students.findByYearLevel(yearLevel));
    }

    // Prints all students whose full name matches the given name (case-insensitive).
    @Override
    public void printStudentsByName(String name) {
        LinkedList<IStudent> all = students.getAll();
        if (all.empty() || name == null)
            return;
        all.findFirst();
        while (true) {
            IStudent student = all.retrieve();
            if (student.getName().equalsIgnoreCase(name.trim()))
                System.out.println(student);
            if (all.last())
                break;
            all.findNext();
        }
    }

    // Prints all students whose full name contains the given text (case-insensitive).
    @Override
    public void printStudentsByPartialName(String name) {
        if (name == null)
            return;
        printAll(students.findByNameContains(name.trim()));
    }

    // Prints all students enrolled in the given major.
    @Override
    public void printStudentsByMajor(String major) {
        printAll(students.findByMajor(major));
    }

    // ------------------------------------------------------------------ deletion

    // Deletes a student, deletes their meetings, removes them from workshops,
    // and deletes workshops left with no participants.
    @Override
    public boolean deleteStudent(int studentId) {
        IStudent student = students.findById(studentId);
        if (student == null)
            return false;

        LinkedList<Integer> eventIds = new LinkedList<>();
        LinkedList<IEvent> allEvents = events.getAllAlphabetically();

        if (!allEvents.empty()) {
            allEvents.findFirst();
            while (true) {
                IEvent event = allEvents.retrieve();
                if (event instanceof Meeting) {
                    if (((Meeting) event).getStudent().getStudentId() == studentId)
                        eventIds.insert(event.getEventId());
                } else if (event instanceof Workshop) {
                    Workshop workshop = (Workshop) event;
                    if (workshop.removeParticipantById(studentId) && workshop.isEmpty())
                        eventIds.insert(workshop.getEventId());
                }
                if (allEvents.last())
                    break;
                allEvents.findNext();
            }
        }

        if (!eventIds.empty()) {
            eventIds.findFirst();
            while (true) {
                events.removeEventById(eventIds.retrieve());
                if (eventIds.last())
                    break;
                eventIds.findNext();
            }
        }

        return students.deleteById(studentId);
    }

    // ------------------------------------------------------------------ scheduling

    // Schedules a meeting for one student. Returns false if the student is not found,
    // the interval is invalid, or it conflicts with the student's schedule.
    @Override
    public boolean scheduleMeeting(String title, IDateTime start, IDateTime end, String location, int studentId) {
        IStudent student = students.findById(studentId);
        if (student == null || !isValidInterval(start, end) || hasConflict(student, start, end))
            return false;

        IEvent meeting = new Meeting(nextEventId++, title, start, end, location, student);
        student.getSchedule().insert(meeting);
        events.addEvent(meeting);
        return true;
    }

    // Schedules a workshop for multiple students. Every student is validated before anything is
    // changed, so a failure leaves no partial state. Returns false if the list is empty, any
    // student is missing or listed twice, or any student has a conflict.
    @Override
    public boolean scheduleWorkshop(String title, IDateTime start, IDateTime end, String location, int[] studentIds) {
        if (studentIds == null || studentIds.length == 0 || !isValidInterval(start, end))
            return false;

        IStudent[] participants = new IStudent[studentIds.length];
        for (int i = 0; i < studentIds.length; i++) {
            for (int j = 0; j < i; j++)
                if (studentIds[j] == studentIds[i])
                    return false;
            IStudent student = students.findById(studentIds[i]);
            if (student == null || hasConflict(student, start, end))
                return false;
            participants[i] = student;
        }

        Workshop workshop = new Workshop(nextEventId++, title, start, end, location);
        for (IStudent student : participants) {
            workshop.addParticipant(student);
            student.getSchedule().insert(workshop);
        }
        events.addEvent(workshop);
        return true;
    }

    // ------------------------------------------------------------------ event output

    // Prints all events whose title matches the given title (case-insensitive).
    @Override
    public void printEventDetailsByTitle(String title) {
        printAll(events.findByTitle(title));
    }

    // Prints all events involving a student with the given name.
    @Override
    public void printEventDetailsByStudentName(String name) {
        printAll(events.findByStudentName(name));
    }

    // Prints all participants of every workshop with the given title.
    @Override
    public void printWorkshopStudents(String title) {
        LinkedList<IEvent> all = events.getAllAlphabetically();
        if (all.empty())
            return;
        all.findFirst();
        while (true) {
            IEvent event = all.retrieve();
            if (event instanceof Workshop && event.getTitle().equalsIgnoreCase(title))
                printAll(((Workshop) event).getParticipants());
            if (all.last())
                break;
            all.findNext();
        }
    }

    // Prints all events in alphabetical order by title.
    @Override
    public void printAllEventsAlphabetically() {
        printAll(events.getAllAlphabetically());
    }
}
