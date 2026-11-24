package Phase2;

public class Workshop extends Event implements IWorkshop {

    private String title;

    private Set<Integer> advisorIds;
    private Set<Integer> studentIds;

    public Workshop(int id,String title,ITimeSlot timeSlot,ILocation location) {

        super(id,timeSlot,location,new BSTSet<Integer>());

        this.title = title;

        advisorIds = new BSTSet<Integer>();
        studentIds = new BSTSet<Integer>();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Set<Integer> getAdvisorIds() {
        return advisorIds;
    }

    @Override
    public Set<Integer> getStudentIds() {
        return studentIds;
    }
}