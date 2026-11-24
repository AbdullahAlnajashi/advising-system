package advising;

import java.util.Scanner;

/** Console menu for the Phase 1 advising system. Run from the project root. */
public class Main {

    private static final String STUDENTS_FILE = "src/advising/students_100.csv";
    private static final String EVENTS_FILE = "src/advising/events_40.csv";

    public static void main(String[] args) {
        AdvisingSystemPhase1 system = new AdvisingSystemPhase1();
        Scanner input = new Scanner(System.in);

        if (!system.loadStudentsFromCSV(STUDENTS_FILE))
            System.out.println("Warning: could not read " + STUDENTS_FILE);
        if (!system.loadEventsFromCSV(EVENTS_FILE))
            System.out.println("Warning: could not read " + EVENTS_FILE);

        System.out.println("Welcome to the Student Advising System!");

        while (true) {
            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println("1. Add a student");
            System.out.println("2. Search for a student");
            System.out.println("3. Delete a student");
            System.out.println("4. Print all students");
            System.out.println("5. Print all events");
            System.out.println("6. Exit");

            Integer choice = readInt(input, "Enter your choice: ");
            if (choice == null || choice == 6)
                break;

            switch (choice) {
                case 1 -> addStudent(system, input);
                case 2 -> {
                    Integer id = readInt(input, "Enter student ID: ");
                    if (id == null)
                        break;
                    IStudent student = system.searchStudentById(id);
                    System.out.println(student != null ? student : "Student not found");
                }
                case 3 -> {
                    Integer id = readInt(input, "Enter student ID: ");
                    if (id == null)
                        break;
                    System.out.println(system.deleteStudent(id) ? "Student deleted successfully!" : "Student not found");
                }
                case 4 -> system.printAllStudents();
                case 5 -> system.printAllEventsAlphabetically();
                default -> System.out.println("Invalid choice, please enter a number from 1 to 6.");
            }
        }

        System.out.println("Goodbye!");
    }

    private static void addStudent(AdvisingSystemPhase1 system, Scanner input) {
        String name = readLine(input, "Enter student name: ");
        Integer id = readInt(input, "Enter student ID: ");
        String email = readLine(input, "Enter email: ");
        String major = readLine(input, "Enter major: ");
        Integer year = readInt(input, "Enter year level: ");
        String notes = readLine(input, "Enter notes: ");

        if (name == null || id == null || email == null || major == null || year == null || notes == null)
            return;
        if (name.isEmpty() || email.isEmpty() || id <= 0 || year < 1) {
            System.out.println("Invalid student data, student not added.");
            return;
        }
        if (system.searchStudentByEmail(email) != null) {
            System.out.println("A student with this email already exists!");
            return;
        }

        if (system.addStudent(new Student(name, id, email, major, year, notes)))
            System.out.println("Student added successfully!");
        else
            System.out.println("A student with this ID already exists!");
    }

    /** Reads a trimmed line, or returns null at end of input. */
    private static String readLine(Scanner input, String prompt) {
        System.out.print(prompt);
        return input.hasNextLine() ? input.nextLine().trim() : null;
    }

    /** Reads an integer, re-prompting on invalid text; returns null at end of input. */
    private static Integer readInt(Scanner input, String prompt) {
        while (true) {
            String line = readLine(input, prompt);
            if (line == null)
                return null;
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
