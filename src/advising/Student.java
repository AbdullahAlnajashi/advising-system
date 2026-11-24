package advising;


public class Student implements IStudent {

    private String name;
    private int studentId;
    private String email;
    private String major;
    private int yearLevel;
    private String notes;
    private LinkedList<IEvent> schedule;
    
    
 // Creates a new student with all required fields
    public Student(String name, int studentId, String email, String major, int yearLevel, String notes) {
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.major = major;
        this.yearLevel = yearLevel;
        this.notes = notes;
        this.schedule = new LinkedList<>();
    }
 // Returns the full name of the student.
    @Override
    public String getName() {
        return name;
    }
 // Sets the full name of the student.
    @Override
    public void setName(String name) {
        this.name = name;
    }
 // Returns the unique student ID.
    @Override
    public int getStudentId() {
        return studentId;
    }
 // Returns the student's email address.
    @Override
    public String getEmail() {
        return email;
    }
 // Sets the student's email address.
    @Override
    public void setEmail(String email) {
        this.email = email;
    }
 // Returns the student's major.
    @Override
    public String getMajor() {
        return major;
    }
 // Sets the student's major.
    @Override
    public void setMajor(String major) {
        this.major = major;
    }
 // Returns the student's academic year level.
    @Override
    public int getYearLevel() {
        return yearLevel;
    }
 // Sets the student's academic year level.
    @Override
    public void setYearLevel(int yearLevel) {
        this.yearLevel = yearLevel;
    }
 // Returns notes related to the student.
    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getFirstName() {
        return name == null ? "" : name.trim().split("\\s+")[0];
    }
 // Returns the student's event schedule.
    @Override
    public LinkedList<IEvent> getSchedule() {
        return schedule;
    }
 // Returns a formatted string with all student details.
    @Override
    public String toString() {
        return "Name: " + name +
               ", ID: " + studentId +
               ", Email: " + email +
               ", Major: " + major +
               ", Year: " + yearLevel +
               ", Notes: " + notes;
    }

    @Override
    public int compareTo(IStudent other) {
        return Integer.compare(this.studentId, other.getStudentId());
    }
}