import java.util.*;

public class Task extends Email {

    private boolean sent;

    public Task(String owner, Calendar creation_date, Calendar due_date,
                String recipient, String title, String data, boolean completed, boolean sent) {
        super(owner, creation_date, due_date, "", title, data);
        this.completed = completed;
        this.sent = sent;
        this.recipients = new ArrayList<String>();
        recipients.add(recipient);
    }

    /**
     * Returns the completion status of the task
     *
     * @return
     */
    public boolean isComplete() {
        return this.completed;
    }

    public void setCompleted(boolean status) {
        this.completed = status;
    }

    /**
     * returns if the task was sent
     *
     * @param sent
     */
    public void setSendStatus(Boolean sent) {

        this.sent = sent;
    }

    public boolean wasSent() {
        return this.sent;
    }

    /**
     * Returns the user-defined email starts with "Title:"
     *
     * @return
     */
    @Override
    public String getTitle() {
        if (!this.title.startsWith("Task:"))
            this.title = "Task:" + this.title;
        return this.title;
    }

    /**
     * Appends poll information if not added yet
     *
     * @return
     */
    @Override
    public String getData() {
        StringBuilder dataBuilder = null;

        if (!data.startsWith("Task from ")) {
            dataBuilder = new StringBuilder();

            dataBuilder.append("Task from " + owner + CRLF);
            dataBuilder.append(this.data);
            dataBuilder.append(CRLF +"To mark Completion click here:");
            dataBuilder.append(CRLF);
            dataBuilder.append("http://" + ServerRun.SERVER_NAME + ":" + ServerRun.port);
        }

        return dataBuilder == null ? data : dataBuilder.toString();
    }
}
