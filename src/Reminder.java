import java.util.Calendar;


public class Reminder extends Email {

	public Reminder(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data, String sender) {
		super(owner, creation_date, due_date, recipient, title, data, sender);
		// TODO Auto-generated constructor stub
	}

	public Reminder(String owner, Calendar creation_date, Calendar due_date,
			String[] recipients, String title, String data, String sender) {
		super(owner, creation_date, due_date, recipients, title, data, sender);
		// TODO Auto-generated constructor stub
	}



	@Override
	public boolean isComplete() {
		// TODO Auto-generated method stub
		return this.isExpired();
	}

}
