package advising;

import static testutil.TestRunner.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import testutil.TestRunner;

public class Phase1Tests {

    private static DateTime dt(int y, int mo, int d, int h, int mi) {
        return new DateTime(y, mo, d, h, mi);
    }

    private static Student student(int id, String name) {
        return new Student(name, id, id + "@example.com", "CS", 2, "");
    }

    private static String tempCsv(String content) throws Exception {
        File f = File.createTempFile("advising", ".csv");
        f.deleteOnExit();
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return f.getPath();
    }

    private static int count(LinkedList<?> list) {
        if (list.empty())
            return 0;
        int n = 0;
        list.findFirst();
        while (true) {
            n++;
            if (list.last())
                break;
            list.findNext();
        }
        return n;
    }

    private static String capture(Runnable action) {
        PrintStream oldOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            action.run();
        } finally {
            System.setOut(oldOut);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static String runMain(String stdin) {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static final String STUDENTS_HEADER = "student_id,name,email,major,year_level,notes\n";
    private static final String EVENTS_HEADER = "event_id,title,type,student_id,start_date_time,end_date_time,location\n";

    public static int run() {
        TestRunner t = new TestRunner();

        t.test("DateTime.format pads to MM/DD/YYYY HH:MM", () -> {
            assertEquals("05/07/2026 09:05", dt(2026, 5, 7, 9, 5).format(), "format");
        });

        t.test("addStudent(null) returns false instead of crashing", () -> {
            assertFalse(new AdvisingSystemPhase1().addStudent(null), "null student must be rejected");
        });

        t.test("loadStudentsFromCSV accepts a row with empty notes", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            String path = tempCsv(STUDENTS_HEADER + "1,Ali Saleh,ali@x.com,CS,2,\n");
            assertTrue(sys.loadStudentsFromCSV(path), "load should succeed");
            assertTrue(sys.searchStudentById(1) != null, "student with empty notes should be loaded");
        });

        t.test("loadStudentsFromCSV skips a malformed row and keeps loading", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            String path = tempCsv(STUDENTS_HEADER
                    + "1,Ali Saleh,ali@x.com,CS,2,n\n"
                    + "oops,broken\n"
                    + "2,Sara Omar,sara@x.com,SE,3,n\n");
            assertTrue(sys.loadStudentsFromCSV(path), "load should succeed");
            assertTrue(sys.searchStudentById(2) != null, "rows after a bad row must still load");
        });

        t.test("loadStudentsFromCSV returns false for a missing file", () -> {
            assertFalse(new AdvisingSystemPhase1().loadStudentsFromCSV("does/not/exist.csv"), "missing file");
        });

        t.test("loaded events are added to the student's schedule (conflicts are detected)", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            String path = tempCsv(EVENTS_HEADER + "1,Advising,Meeting,1,5/15/2026 10:00,5/15/2026 11:00,Room A\n");
            assertTrue(sys.loadEventsFromCSV(path), "load events");
            assertFalse(sys.scheduleMeeting("Clash", dt(2026, 5, 15, 10, 30), dt(2026, 5, 15, 11, 30), "Room B", 1),
                    "meeting overlapping a loaded event must be rejected");
        });

        t.test("loadEventsFromCSV skips rows that conflict with the student's schedule", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            String path = tempCsv(EVENTS_HEADER
                    + "1,A Meeting,Meeting,1,5/15/2026 10:00,5/15/2026 11:00,Room A\n"
                    + "2,B Meeting,Meeting,1,5/15/2026 10:30,5/15/2026 11:30,Room B\n");
            assertTrue(sys.loadEventsFromCSV(path), "load events");
            assertEquals(1, count(sys.searchStudentById(1).getSchedule()), "conflicting row must be skipped");
        });

        t.test("new events get IDs that do not collide with loaded event IDs", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            sys.addStudent(student(2, "Sara Omar"));
            String path = tempCsv(EVENTS_HEADER + "1,Loaded Workshop,Workshop,1,5/15/2026 10:00,5/15/2026 11:00,Hall\n");
            sys.loadEventsFromCSV(path);
            assertTrue(sys.scheduleMeeting("New Meeting", dt(2026, 6, 1, 9, 0), dt(2026, 6, 1, 10, 0), "Office", 2),
                    "schedule meeting");
            // Deleting student 2 removes their meeting by ID; with colliding IDs it would remove the workshop too.
            sys.deleteStudent(2);
            assertTrue(capture(() -> sys.printEventDetailsByTitle("Loaded Workshop")).contains("Loaded Workshop"),
                    "loaded workshop must survive deleting an unrelated student");
        });

        t.test("scheduleMeeting rejects end <= start", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            assertFalse(sys.scheduleMeeting("Bad", dt(2026, 5, 1, 11, 0), dt(2026, 5, 1, 10, 0), "Office", 1),
                    "inverted interval must be rejected");
        });

        t.test("scheduleWorkshop with a missing student leaves other students untouched", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            assertFalse(sys.scheduleWorkshop("W", dt(2026, 5, 1, 9, 0), dt(2026, 5, 1, 10, 0), "Hall", new int[]{1, 999}),
                    "must fail");
            assertEquals(0, count(sys.searchStudentById(1).getSchedule()), "no partial schedule entries");
        });

        t.test("scheduleWorkshop with a conflicting student leaves other students untouched", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            sys.addStudent(student(2, "Sara Omar"));
            sys.scheduleMeeting("Busy", dt(2026, 5, 1, 9, 0), dt(2026, 5, 1, 10, 0), "Office", 2);
            assertFalse(sys.scheduleWorkshop("W", dt(2026, 5, 1, 9, 30), dt(2026, 5, 1, 11, 0), "Hall", new int[]{1, 2}),
                    "must fail");
            assertEquals(0, count(sys.searchStudentById(1).getSchedule()), "no partial schedule entries");
        });

        t.test("scheduleWorkshop rejects an empty participant list", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            assertFalse(sys.scheduleWorkshop("W", dt(2026, 5, 1, 9, 0), dt(2026, 5, 1, 10, 0), "Hall", new int[0]),
                    "workshop without students");
        });

        t.test("Workshop.addParticipant rejects duplicates", () -> {
            Workshop w = new Workshop(1, "W", dt(2026, 5, 1, 9, 0), dt(2026, 5, 1, 10, 0), "Hall");
            Student s = student(1, "Ali Saleh");
            assertTrue(w.addParticipant(s), "first add");
            assertFalse(w.addParticipant(s), "duplicate add");
        });

        t.test("deleteStudent removes the student from workshops and deletes empty ones", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            sys.addStudent(student(2, "Sara Omar"));
            sys.scheduleWorkshop("Shared", dt(2026, 5, 1, 9, 0), dt(2026, 5, 1, 10, 0), "Hall", new int[]{1, 2});
            sys.scheduleWorkshop("Solo", dt(2026, 5, 2, 9, 0), dt(2026, 5, 2, 10, 0), "Hall", new int[]{1});
            assertTrue(sys.deleteStudent(1), "delete");
            assertTrue(capture(() -> sys.printEventDetailsByTitle("Shared")).contains("Shared"), "shared workshop stays");
            assertEquals("", capture(() -> sys.printEventDetailsByTitle("Solo")), "empty workshop deleted");
            assertEquals("", capture(() -> sys.printWorkshopStudents("Shared")).contains("Ali Saleh") ? "still listed" : "",
                    "deleted student removed from workshop");
        });

        t.test("event toString shows its date/time", () -> {
            Meeting m = new Meeting(1, "Advising", dt(2026, 5, 1, 9, 0), dt(2026, 5, 1, 10, 0), "Office", student(1, "Ali Saleh"));
            assertTrue(m.toString().contains("05/01/2026 09:00"), "toString was: " + m);
        });

        t.test("main survives non-numeric menu input", () -> {
            String out = runMain("abc\n6\n");
            assertTrue(out.contains("Goodbye"), "should keep running and exit normally");
        });

        t.test("main exits cleanly when input ends", () -> {
            String out = runMain("");
            assertTrue(out.contains("Goodbye"), "should exit normally on end of input");
        });

        t.test("DateTime rejects impossible dates and times", () -> {
            assertThrows(IllegalArgumentException.class, () -> dt(2026, 13, 1, 9, 0), "month 13");
            assertThrows(IllegalArgumentException.class, () -> dt(2026, 2, 30, 9, 0), "Feb 30");
            assertThrows(IllegalArgumentException.class, () -> dt(2026, 5, 1, 24, 0), "hour 24");
            assertThrows(IllegalArgumentException.class, () -> dt(2026, 5, 1, 9, 60), "minute 60");
            dt(2028, 2, 29, 23, 59); // leap day is valid
        });

        t.test("loadEventsFromCSV skips a row with an impossible date", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            String path = tempCsv(EVENTS_HEADER + "1,Bad Date,Meeting,1,13/40/2026 10:00,13/40/2026 11:00,Room A\n");
            assertTrue(sys.loadEventsFromCSV(path), "load events");
            assertEquals(0, count(sys.searchStudentById(1).getSchedule()), "invalid row must be skipped");
        });

        t.test("getFirstName ignores leading spaces", () -> {
            assertEquals("Ali", new Student("  Ali Saleh", 1, "e", "CS", 1, "").getFirstName(), "first name");
        });

        t.test("searchStudentByEmail is case-insensitive and null-safe", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(new Student("No Email", 1, null, "CS", 1, ""));
            sys.addStudent(new Student("Ali Saleh", 2, "Ali@Example.com", "CS", 1, ""));
            assertEquals(2, sys.searchStudentByEmail("ali@example.com").getStudentId(), "case-insensitive match");
            assertTrue(sys.searchStudentByEmail(null) == null, "null email finds nobody");
        });

        t.test("partial-name search with null does not crash", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            StudentList list = new StudentList();
            list.add(student(1, "Ali Saleh"));
            assertEquals(0, count(list.findByNameContains(null)), "null matches nobody");
            sys.printStudentsByPartialName(null);
        });

        t.test("back-to-back meetings do not conflict", () -> {
            AdvisingSystemPhase1 sys = new AdvisingSystemPhase1();
            sys.addStudent(student(1, "Ali Saleh"));
            assertTrue(sys.scheduleMeeting("A", dt(2026, 5, 1, 9, 0), dt(2026, 5, 1, 10, 0), "Office", 1), "first");
            assertTrue(sys.scheduleMeeting("B", dt(2026, 5, 1, 10, 0), dt(2026, 5, 1, 11, 0), "Office", 1), "second");
        });

        return t.runAll("Phase 1");
    }
}
