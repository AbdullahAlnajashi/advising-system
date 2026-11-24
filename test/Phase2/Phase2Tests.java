package Phase2;

import static testutil.TestRunner.*;

import testutil.TestRunner;

public class Phase2Tests {

    private static ITimeSlot slot(int day, int startHour, int endHour) {
        return new TimeSlot(new DateTime(2026, 5, day, startHour, 0), new DateTime(2026, 5, day, endHour, 0));
    }

    /** A small world: room 1 (capacity given), advisor 100, students 200..200+n-1. */
    private static AdvisingSystemPhase2 world(int roomCapacity, int studentCount) {
        AdvisingSystemPhase2 sys = new AdvisingSystemPhase2();
        ILocation office = new Location(50, "Office 50", false, false, 1, null);
        ILocation room = new Location(1, "Room 1", true, false, roomCapacity, new Schedule());
        sys.addLocation(office);
        sys.addLocation(room);
        sys.addAdvisor(new Advisor(100, "Dr. Ahmed", "a@x.com", new Schedule(), office));
        for (int i = 0; i < studentCount; i++)
            sys.addStudent(new Student(200 + i, "S" + i, "s" + i + "@x.com", new Schedule(), "CS"));
        return sys;
    }

    private static ScheduleFailureReason reasonOf(TestBody body) {
        return assertThrows(SchedulingException.class, body, "expected SchedulingException").getReason();
    }

    public static int run() {
        TestRunner t = new TestRunner();

        t.test("DateTime.format pads to MM/DD/YYYY HH:MM", () -> {
            assertEquals("05/07/2026 09:05", new DateTime(2026, 5, 7, 9, 5).format(), "format");
        });

        t.test("TimeSlot rejects end <= start", () -> {
            assertThrows(IllegalArgumentException.class, () -> slot(1, 11, 10), "inverted slot");
            assertThrows(IllegalArgumentException.class, () -> slot(1, 10, 10), "empty slot");
        });

        t.test("compareTo does not overflow for extreme IDs", () -> {
            Student low = new Student(Integer.MIN_VALUE, "a", "a", new Schedule(), "CS");
            Student high = new Student(1, "b", "b", new Schedule(), "CS");
            assertTrue(low.compareTo(high) < 0, "Person.compareTo overflow");
            Location l1 = new Location(Integer.MIN_VALUE, "a", false, false, 1, null);
            Location l2 = new Location(1, "b", false, false, 1, null);
            assertTrue(l1.compareTo(l2) < 0, "Location.compareTo overflow");
            Meeting m1 = new Meeting(Integer.MIN_VALUE, slot(1, 9, 10), l1, 1, 2);
            Meeting m2 = new Meeting(1, slot(1, 9, 10), l1, 1, 2);
            assertTrue(m1.compareTo(m2) < 0, "Event.compareTo overflow");
        });

        t.test("Schedule.clear fully resets (remove after clear returns false)", () -> {
            Schedule s = new Schedule();
            s.add(1, slot(1, 9, 10));
            s.clear();
            assertFalse(s.remove(1), "event 1 must be gone after clear");
            assertTrue(s.add(1, slot(2, 9, 10)), "re-adding after clear");
            assertTrue(s.remove(1), "remove re-added event");
            assertFalse(s.conflicts(slot(2, 9, 10)), "slot must be free again");
        });

        t.test("scheduleMeeting books both participants and detects conflicts", () -> {
            AdvisingSystemPhase2 sys = world(10, 2);
            int id = sys.scheduleMeeting(slot(1, 9, 10), 100, 200);
            assertTrue(sys.searchStudentById(200).getSchedule().contains(id), "student booked");
            assertTrue(sys.searchAdvisorById(100).getSchedule().contains(id), "advisor booked");
            assertEquals(ScheduleFailureReason.CONFLICT_ADVISOR,
                    reasonOf(() -> sys.scheduleMeeting(slot(1, 9, 10), 100, 201)), "advisor conflict");
        });

        t.test("scheduleMeeting rejects a null time slot", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            assertThrows(IllegalArgumentException.class, () -> sys.scheduleMeeting(null, 100, 200), "null slot");
        });

        t.test("cancelWorkshop frees the location for that time", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            int w = sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[]{100}, new int[]{200});
            assertTrue(sys.cancelWorkshop(w), "cancel");
            assertFalse(sys.searchLocationById(1).getSchedule().conflicts(slot(1, 9, 10)), "room must be free");
            sys.scheduleWorkshop("W2", slot(1, 9, 10), 1, new int[]{100}, new int[]{200});
        });

        t.test("removing the last participant deletes the workshop and frees the room", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            int w = sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[0], new int[]{200});
            assertTrue(sys.deleteStudent(200), "delete student");
            assertEquals(0, sys.getWorkshops().size(), "workshop deleted");
            assertFalse(sys.searchLocationById(1).getSchedule().contains(w), "room released");
        });

        t.test("scheduleWorkshop enforces location capacity without side effects", () -> {
            AdvisingSystemPhase2 sys = world(2, 3);
            assertEquals(ScheduleFailureReason.CAPACITY_EXCEEDED,
                    reasonOf(() -> sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[0], new int[]{200, 201, 202})),
                    "too many students");
            assertEquals(0, sys.getWorkshops().size(), "nothing created");
            assertTrue(sys.searchStudentById(200).getSchedule().empty(), "no partial booking");
        });

        t.test("scheduleWorkshop failure on a later student leaves nobody booked", () -> {
            AdvisingSystemPhase2 sys = world(10, 2);
            sys.scheduleMeeting(slot(1, 9, 10), 100, 201);
            sys.addAdvisor(new Advisor(101, "Dr. B", "b@x.com", new Schedule(), null));
            assertEquals(ScheduleFailureReason.CONFLICT_STUDENT,
                    reasonOf(() -> sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[]{101}, new int[]{200, 201})),
                    "student 201 is busy");
            assertTrue(sys.searchStudentById(200).getSchedule().empty(), "student 200 not booked");
            assertTrue(sys.searchAdvisorById(101).getSchedule().empty(), "advisor 101 not booked");
            assertTrue(sys.searchLocationById(1).getSchedule().empty(), "room not booked");
        });

        t.test("scheduleWorkshop rejects a workshop with no participants", () -> {
            AdvisingSystemPhase2 sys = world(10, 0);
            assertThrows(IllegalArgumentException.class,
                    () -> sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[0], new int[0]), "empty workshop");
        });

        t.test("addStudentToWorkshop fails if the student is already in it", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            int w = sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[0], new int[]{200});
            assertEquals(ScheduleFailureReason.CONFLICT_STUDENT,
                    reasonOf(() -> sys.addStudentToWorkshop(w, 200)), "duplicate student");
        });

        t.test("addAdvisorToWorkshop fails if the advisor is already in it", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            int w = sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[]{100}, new int[]{200});
            assertEquals(ScheduleFailureReason.CONFLICT_ADVISOR,
                    reasonOf(() -> sys.addAdvisorToWorkshop(w, 100)), "duplicate advisor");
        });

        t.test("addStudentToWorkshop works in an unlimited-capacity location", () -> {
            AdvisingSystemPhase2 sys = world(10, 2);
            sys.addLocation(new Location(2, "Big Hall", true, false, -1, new Schedule()));
            int w = sys.scheduleWorkshop("W", slot(1, 9, 10), 2, new int[0], new int[]{200});
            sys.addStudentToWorkshop(w, 201);
            assertTrue(sys.searchStudentById(201).getSchedule().contains(w), "student added");
        });

        t.test("addStudentToWorkshop enforces capacity", () -> {
            AdvisingSystemPhase2 sys = world(1, 2);
            int w = sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[0], new int[]{200});
            assertEquals(ScheduleFailureReason.CAPACITY_EXCEEDED,
                    reasonOf(() -> sys.addStudentToWorkshop(w, 201)), "room full");
        });

        t.test("removeAdvisorFromWorkshop on an unknown advisor reports ADVISOR_NOT_FOUND", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            int w = sys.scheduleWorkshop("W", slot(1, 9, 10), 1, new int[]{100}, new int[]{200});
            assertEquals(ScheduleFailureReason.ADVISOR_NOT_FOUND,
                    reasonOf(() -> sys.removeAdvisorFromWorkshop(w, 999)), "unknown advisor");
        });

        t.test("deleteStudent cancels the student's meetings on both schedules", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            int m = sys.scheduleMeeting(slot(1, 9, 10), 100, 200);
            assertTrue(sys.deleteStudent(200), "delete");
            assertFalse(sys.searchAdvisorById(100).getSchedule().contains(m), "advisor freed");
            assertEquals(0, sys.getMeetings().size(), "meeting removed");
            assertFalse(sys.deleteStudent(200), "second delete returns false");
        });

        t.test("BSTSet remove keeps remaining keys in sorted order", () -> {
            BSTSet<Integer> set = new BSTSet<>();
            int[] keys = {50, 30, 70, 20, 40, 60, 80, 35, 45};
            for (int k : keys)
                set.insert(k);
            assertTrue(set.remove(30), "remove node with two children");
            assertTrue(set.remove(50), "remove root");
            List<Integer> l = set.getKeys();
            StringBuilder sb = new StringBuilder();
            l.findFirst();
            while (true) {
                sb.append(l.retrieve()).append(' ');
                if (l.last())
                    break;
                l.findNext();
            }
            assertEquals("20 35 40 45 60 70 80 ", sb.toString(), "inorder after removals");
            assertEquals(7, set.size(), "size");
        });

        t.test("DateTime rejects impossible dates and times", () -> {
            assertThrows(IllegalArgumentException.class, () -> new DateTime(2026, 0, 1, 9, 0), "month 0");
            assertThrows(IllegalArgumentException.class, () -> new DateTime(2027, 2, 29, 9, 0), "Feb 29 non-leap");
            assertThrows(IllegalArgumentException.class, () -> new DateTime(2026, 4, 31, 9, 0), "Apr 31");
            assertThrows(IllegalArgumentException.class, () -> new DateTime(2026, 5, 1, -1, 0), "hour -1");
        });

        t.test("addLocation rejects a reservable location without a schedule", () -> {
            AdvisingSystemPhase2 sys = new AdvisingSystemPhase2();
            assertFalse(sys.addLocation(new Location(9, "Broken", true, false, 10, null)), "no schedule");
        });

        t.test("cancelMeeting of an unknown ID returns false", () -> {
            assertFalse(world(10, 1).cancelMeeting(42), "unknown meeting");
            assertFalse(world(10, 1).cancelWorkshop(42), "unknown workshop");
        });

        t.test("addStudentToWorkshop on an unknown workshop reports EVENT_NOT_FOUND", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            assertEquals(ScheduleFailureReason.EVENT_NOT_FOUND,
                    reasonOf(() -> sys.addStudentToWorkshop(42, 200)), "unknown workshop");
        });

        t.test("schedule entries survive removing a BST node with two children", () -> {
            Schedule s = new Schedule();
            int[] days = {15, 8, 22, 4, 12, 18, 26};
            for (int i = 0; i < days.length; i++)
                assertTrue(s.add(i + 1, slot(days[i], 9, 10)), "add " + days[i]);
            assertTrue(s.remove(1), "remove root (two children)");
            assertFalse(s.conflicts(slot(15, 9, 10)), "removed slot is free");
            for (int i = 1; i < days.length; i++)
                assertTrue(s.conflicts(slot(days[i], 9, 10)), "slot on day " + days[i] + " still booked");
            assertEquals(6, s.size(), "size");
        });

        t.test("back-to-back meetings do not conflict", () -> {
            AdvisingSystemPhase2 sys = world(10, 1);
            sys.scheduleMeeting(slot(1, 9, 10), 100, 200);
            sys.scheduleMeeting(slot(1, 10, 11), 100, 200);
            assertEquals(2, sys.getMeetings().size(), "both meetings booked");
        });

        return t.runAll("Phase 2");
    }
}
