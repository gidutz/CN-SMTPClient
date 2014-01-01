import java.util.Calendar;
import java.util.Date;

public abstract class Email {
	private String owner;
	private Calendar creation_date;
	private Calendar due_date;
	private String recipient;
	private String title;
	private String data;
	private int id;

	public Email(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data) {
		super();
		this.owner = owner;
		this.creation_date = creation_date;
		this.due_date = due_date;
		this.recipient = recipient;
		this.title = title;
		this.data = data;
		this.id = Math.abs(hash2id(owner, creation_date));
	}

	private int hash2id(String owner, Calendar creation_date) {
		int hash = 7;
		for (int i = 0; i < owner.length(); i++) {
			hash = hash * 5 + owner.charAt(i);
		}
		hash += creation_date.get(Calendar.DAY_OF_YEAR)
				+ creation_date.get(Calendar.HOUR_OF_DAY)
				+ creation_date.get(Calendar.MINUTE)
				+ creation_date.get(Calendar.SECOND);

		return hash;
	}

	public int getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}

	public Calendar getCreation_date() {
		return creation_date;
	}

	public Calendar getDue_date() {
		return due_date;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getTitle() {
		return title;
	}

	public String getData() {
		return data;
	}

	public boolean isExpired() {
		Calendar currentDate = Calendar.getInstance();
		return currentDate.after(this.due_date);
	}
	public abstract boolean isComplete();

}
