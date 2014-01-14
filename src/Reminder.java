import java.util.Calendar;

public class Reminder extends Email {

	public Reminder(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data,boolean completed ) {
		super(owner, creation_date, due_date, recipient, title, data,completed);
		this.recipients.add(owner);
	}



    /**
     * Returns the completion status of the reminder
     * @return
     */
	public boolean isComplete() {
		return this.completed;
	}

    /**
     * Sets the complete status of the email
     * @param status
     */
    public void setCompleted(boolean status) {
        this.completed = status;

    }

    /**
     * A reminder is completed only iff it was sent
     * @return
     */
    @Override
    public boolean wasSent() {
        return this.completed;
    }

    /**
     * returns the content of the reminder
     * @return
     */
    public String getData(){
        return this.data;
    }
}
