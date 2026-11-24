# Student Advising System

A Java console application for managing students, advisors, meetings, and workshops.
It is built on **hand-written data structures** (no `java.util` collections): a singly linked
list in Phase 1, and binary search trees (`BSTSet`, `BSTMap`) in Phase 2.

## Features

### Phase 1: linked lists
- Load students and events from CSV files. Malformed, unknown-student, and conflicting rows are skipped.
- Add, search (by ID, email, name, partial name, major, year), and delete students.
- Deleting a student removes their meetings and takes them out of workshops. A workshop left with no students is deleted.
- Schedule meetings and multi-student workshops with time-conflict detection. A workshop is booked
  for everyone or for nobody.
- Events are kept in alphabetical order by title.

### Phase 2: binary search trees
- Advisors, students, locations, meetings, and workshops are stored in BST-backed sets, so lookups take O(log n) on average.
- Each person and each reservable room has a `Schedule` that rejects overlapping time slots in
  O(log n). It does this with an interval comparator, where overlapping slots compare as equal.
- One-to-one meetings in the advisor's office, and workshops in shared rooms. Room capacity and room booking conflicts are checked.
- Failures are reported with a typed `SchedulingException` (`CONFLICT_STUDENT`, `CAPACITY_EXCEEDED`, …).
- Cancelling a workshop frees the room and every participant's schedule.

## Project layout

```
src/advising/   Phase 1: linked-list implementation, CSV data, console menu (Main)
src/Phase2/     Phase 2: BST implementation and demo (MainPhase2)
test/           Dependency-free test suite (AllTests)
```

## Running

Requires JDK 17+. Run these commands from the project root:

```sh
javac -d out $(find src -name '*.java')
java -cp out advising.Main        # Phase 1 interactive menu
java -cp out Phase2.MainPhase2    # Phase 2 demo
```

## Tests

```sh
./run-tests.sh          # macOS / Linux / Git Bash
.\run-tests.ps1         # Windows PowerShell
```

The tests also run on every push through GitHub Actions.
