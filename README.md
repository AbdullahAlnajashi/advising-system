# Student Advising System

A university **Data Structures** course project, written in Java and delivered in two phases. The
system manages students, advisors, rooms, one-to-one advising meetings, and group workshops. It
checks for scheduling conflicts and room capacity.

All data structures are **written from scratch**, without `java.util` collections. The goal of
the project is to implement the same system twice and compare the results:

| | Phase 1 | Phase 2 |
|---|---|---|
| Core structure | Singly linked list | Binary search tree (`BSTSet`, `BSTMap`) |
| Find a student by ID | O(n): linear scan | O(log n) on average |
| Check a time conflict | O(n): scan the student's events | O(log n): tree lookup |

## Search speed: linked list vs. BST

Measured with [`test/SearchBenchmark.java`](test/SearchBenchmark.java). The benchmark looks up every
student once, with IDs inserted in random order (fixed seed). Comparisons are exact; times are
the best of 5 runs on one laptop.

| Students | Linked list: avg comparisons | BST: avg comparisons | Linked list: time | BST: time |
|---:|---:|---:|---:|---:|
| 100 | 50.5 | 7.8 | 0.03 ms | 0.04 ms |
| 1,000 | 500.5 | 13.4 | ~2 ms | 0.13 ms |
| 10,000 | 5,000.5 | 16.1 | ~300 ms | 1.5 ms |

At 10,000 students, the BST needs **about 310× fewer comparisons** and runs **about 200× faster**.
The linked list's cost grows in step with the number of students. The tree's cost grows by only
a few comparisons each time the data size grows tenfold.

## Phase 1: linked lists (`src/advising`)

- Loads 100 students and 40 events from CSV files. Rows that are malformed, have impossible
  dates, refer to unknown students, or clash with a student's existing schedule are skipped.
- Students are kept sorted by ID, and events are kept in alphabetical order by title.
- Students can be searched by ID, email, exact name, partial name, major, or year level.
- Meetings and multi-student workshops are scheduled with conflict detection. A workshop is
  booked for every listed student or for none of them, never partially.
- Deleting a student also deletes their meetings and removes them from workshops. A workshop
  left with no students is deleted.
- An interactive console menu (`Main`) can add, search, delete, and list students and events.
  Invalid input never crashes it.

## Phase 2: binary search trees (`src/Phase2`)

- Advisors, students, rooms, meetings, and workshops are stored in BST-backed sets. Every lookup
  by ID is a tree search.
- Every person and every reservable room has a `Schedule`. A `Schedule` refuses overlapping
  bookings in O(log n).
- **Interval-tree trick:** `TimeSlot.compareTo` returns `0` when two slots overlap. Searching the
  schedule's BST for a new slot therefore finds any clashing booking in one tree descent.
- Workshops check that the room exists, that it can be reserved, that it is free at that time,
  and that it has enough capacity. Each failure is reported with a typed `SchedulingException`
  (`CONFLICT_STUDENT`, `CAPACITY_EXCEEDED`, `LOCATION_NOT_FOUND`, …).
- Cancelling a workshop, or removing its last participant, frees the room and every
  participant's schedule.

## Testing

[`test/`](test) holds **47 automated tests**: 23 for Phase 1 and 24 for Phase 2. They run with a
small test runner written for this project, so nothing beyond the JDK is needed. GitHub Actions
runs them on every push.

The tests cover:
- Conflict detection, including back-to-back events, which are allowed.
- All-or-nothing workshop booking.
- Room capacity, including rooms with unlimited capacity.
- Cascading deletes, and freeing rooms when workshops are cancelled.
- CSV rows that are malformed, conflicting, or have impossible dates.
- BST removal of a node with two children.
- Integer overflow in `compareTo`.
- Date validation, including leap years.
- Console input that is not a number, and input that ends early.

## Running

Requires **JDK 17 or newer**. Run these commands from the project root.

```sh
# macOS / Linux / Git Bash
javac -d out $(find src test -name '*.java')
```

```powershell
# Windows PowerShell
javac -d out (Get-ChildItem -Recurse src, test -Filter *.java).FullName
```

```sh
java -cp out advising.Main        # Phase 1: interactive menu
java -cp out Phase2.MainPhase2    # Phase 2: scheduling demo
java -cp out SearchBenchmark      # linked list vs. BST benchmark
```

Tests:

```sh
./run-tests.sh      # macOS / Linux / Git Bash
.\run-tests.ps1     # Windows PowerShell
```

## Project structure

```
src/advising/      Phase 1: linked list, StudentList, EventList, CSV loading, console menu
src/Phase2/        Phase 2: BSTSet, BSTMap, Schedule, TimeSlot, scheduling system, demo
test/              Test suites, test runner, and search benchmark
```

## Limitations

- The BST is not self-balancing. If IDs are inserted in sorted order, it becomes a linked list.
  The benchmark measures this case: 1,000 sorted IDs need 500.5 comparisons on average.
  A red-black or AVL tree would guarantee O(log n).
- Both phases are console applications. There is no graphical interface and no login screen.
