import java.util.*;

public class Task extends Email {

    private boolean sent;

    public Task(String owner, Calendar creation_date, Calendar due_date,
                String recipient, String title, String data, boolean completed, boolean sent) {
        super(owner, creation_date, due_date, recipient, title, data);
        this.completed = completed;
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(this.data);
        dataBuilder.append("\n To mark Completion click here:");
        dataBuilder.append("\n ");
        dataBuilder.append("http://" + ServerRun.SERVER_NAME + ":" + ServerRun.port);
        this.sent = sent;
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
    public void setSendStatus (Boolean sent){
        this.sent = sent;
    }
    public boolean wasSent (){
        return this.sent;
    }

}
