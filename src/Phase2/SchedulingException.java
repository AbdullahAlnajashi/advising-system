package Phase2;

/**
 * Exception thrown when scheduling fails.
 */
public class SchedulingException extends Exception {

    private static final long serialVersionUID = 1L;

    private final ScheduleFailureReason reason;

    public SchedulingException(ScheduleFailureReason reason) {
        this.reason = reason;
    }

    public ScheduleFailureReason getReason() {
        return reason;
    }
}