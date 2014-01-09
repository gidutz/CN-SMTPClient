import java.util.Calendar;

public class Reminder extends Email {

	public Reminder(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data,boolean completed ) {
		super(owner, creation_date, due_date, recipient, title, data,completed);
		this.recipients.add(owner);
	}

    /**
     * Constructs a Reminder
     * @param owner
     * @param creation_date
     * @param due_date
     * @param recipients
     * @param title
     * @param data
     */
	public Reminder(String owner, Calendar creation_date, Calendar due_date,
			String[] recipients, String title, String data, boolean completed) {
		super(owner, creation_date, due_date, recipients, title, data, completed);
		this.recipients.add(owner);
	}

    /**
     * Returns the completion status of the reminder
     * @return
     */
	public boolean isComplete() {
		return this.completed;
	}

    public void setCompleted(boolean status) {
        this.completed = status;
    }

}
