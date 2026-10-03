import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Deadline {

    public int id;
    public String taskName;
    public String subject;
    public Date deadlineDate;
    public Time deadlineTime;
    public String priority;
    public String status;
    public Timestamp createdAt;

    public Deadline(
            int id,
            String taskName,
            String subject,
            Date deadlineDate,
            Time deadlineTime,
            String priority,
            String status,
            Timestamp createdAt) {

        this.id = id;
        this.taskName = taskName;
        this.subject = subject;
        this.deadlineDate = deadlineDate;
        this.deadlineTime = deadlineTime;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
    }
}