package advising;
//Stores students in a linked list sorted in ascending order by student ID.
public class StudentList implements IStudentList {

	private LinkedList<IStudent> list;

    public StudentList() {
        list = new LinkedList<>();
    }
 // Inserts a student in sorted order by studentId. Returns false if ID already exists.
    @Override
    public boolean add(IStudent student) {
        if (student == null)
            return false;
        if (list.empty()) {
            list.insert(student);
            return true;
        }

        list.findFirst();
        while (true) {
            IStudent current = list.retrieve();
            if (current.getStudentId() == student.getStudentId())
                return false;
            if (current.getStudentId() > student.getStudentId()) {
                IStudent temp =current;
                list.update(student);
                list.insert(temp);
                return true;
            }

            if (list.last())
                break;

            list.findNext();
        }

        list.insert(student);
        return true;
    }
 // Searches for and returns a student by their unique ID. Returns null if not found.
    @Override
    public IStudent findById(int studentId) {
        if (list.empty())
            return null;

        list.findFirst();
        while (true) {
            IStudent current = list.retrieve();

            if (current.getStudentId() == studentId)
                return current;

            if (list.last())
                break;

            list.findNext();
        }

        return null;
    }
 // Returns the total number of students in the list.
    @Override
    public int size() {
        int count = 0;

        if (list.empty())
            return 0;

        list.findFirst();
        while (true) {
            count++;

            if (list.last())
                break;

            list.findNext();
        }

        return count;
    }

    @Override
    public LinkedList<IStudent> findByName(String fullName) {
        LinkedList<IStudent> result = new LinkedList<>();

        if (list.empty())
            return result;

        list.findFirst();
        while (true) {
            IStudent current = list.retrieve();

            if (current.getName().equals(fullName))
                result.insert(current);

            if (list.last())
                break;

            list.findNext();
        }

        return result;
    }


    @Override
    public LinkedList<IStudent> findByNameContains(String partialName) {
        LinkedList<IStudent> result = new LinkedList<>();

        if (list.empty() || partialName == null)
            return result;

        String needle = partialName.toLowerCase();
        list.findFirst();
        while (true) {
            IStudent current = list.retrieve();

            if (current.getName() != null && current.getName().toLowerCase().contains(needle))
                result.insert(current);

            if (list.last())
                break;

            list.findNext();
        }

        return result;
    }
    

 // Searches for and returns a student by their email address. Returns null if not found.
    @Override
    public IStudent findByEmail(String email) {
        if (list.empty())
            return null;

        list.findFirst();

        while (true) {
            IStudent current = list.retrieve();

            if (email != null && email.trim().equalsIgnoreCase(current.getEmail()))
                return current;

            if (list.last())
                break;

            list.findNext();
        }

        return null;
    }
    
 // Returns all students with the given major.
    @Override
    public LinkedList<IStudent> findByMajor(String major) {
        LinkedList<IStudent> result = new LinkedList<>();

        if (list.empty())
            return result;

        list.findFirst();
        while (true) {
            IStudent current = list.retrieve();

            if (current.getMajor().equals(major))
                result.insert(current);

            if (list.last())
                break;

            list.findNext();
        }

        return result;
    
    }

    @Override
    public LinkedList<IStudent> findByYearLevel(int yearLevel) {
        LinkedList<IStudent> result = new LinkedList<>();

        if (list.empty())
            return result;

        list.findFirst();
        while (true) {
            IStudent current = list.retrieve();

            if (current.getYearLevel() == yearLevel)
                result.insert(current);

            if (list.last())
                break;

            list.findNext();
        }

        return result;
    }
 // Returns the full list of students.
    @Override
    public LinkedList<IStudent> getAll() {
        return list;
    }
 // Deletes a student from the list by their ID. Returns false if not found.
    @Override
    public boolean deleteById(int studentId) {
        if (list.empty())
            return false;

        list.findFirst();

        while (true) {
            IStudent current = list.retrieve();

            if (current.getStudentId() == studentId) {
                list.remove();
                return true;
            }

            if (list.last())
                break;

            list.findNext();
        }

        return false;
    }
}