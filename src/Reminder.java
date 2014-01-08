import java.util.Calendar;

public class Reminder extends Email {

	public Reminder(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data ) {
		super(owner, creation_date, due_date, recipient, title, data);
		this.recipients.add(owner);
	}

	public Reminder(String owner, Calendar creation_date, Calendar due_date,
			String[] recipients, String title, String data) {
		super(owner, creation_date, due_date, recipients, title, data);
		this.recipients.add(owner);
	}

	@Override
	public boolean isComplete() {
		// TODO Auto-generated method stub
		return this.isExpired();
	}

}
