import java.util.ArrayList;
import java.util.Calendar;

public abstract class Email {
	private String owner;
	private Calendar creation_date;
	private Calendar due_date;
	private String title;
	private String data;
	private int id;
	private ArrayList<String> recipients;
	private String sender;

	public Email(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data, String sender) {
		super();
		this.owner = owner;
		this.creation_date = creation_date;
		this.due_date = due_date;
		this.title = title;
		this.data = data;
		this.id = Math.abs(hash2id(owner, creation_date));
		this.recipients = new ArrayList<String>();
		this.recipients.add(recipient);
		this.sender = sender;
	}

	public Email(String owner, Calendar creation_date, Calendar due_date,
			String[] recipients, String title, String data, String sender) {
		this( owner,  creation_date,  due_date,
				recipients[0] ,  title,  data,  sender);
		
		this.recipients = new ArrayList<String>();
		for (String recipient : recipients) {
			this.recipients.add(recipient);
		}
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
		return recipients.get(0);
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

	public ArrayList<String> getRecipients() {
		return recipients;
	}

	public String getSender() {
		return sender;
	}

	public abstract boolean isComplete();

}
