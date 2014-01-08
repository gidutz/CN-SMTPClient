import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Email {
	String owner;
	Calendar creation_date;
	Calendar due_date;
	String title;
	String data;
	int id;
	ArrayList<String> recipients;
	final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	public Email(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data) {
		super();
		this.owner = owner;
		this.creation_date = creation_date;
		this.due_date = due_date;
		this.title = title;
		this.data = data;
		this.id = Math.abs(hash2id(owner, creation_date));
		this.recipients = new ArrayList<String>();
		recipient = validateEmail(recipient);
		if (recipient != null)
			this.recipients.add(recipient);

	}

	public Email(String owner, Calendar creation_date, Calendar due_date,
			String[] recipients, String title, String data) {
		this(owner, creation_date, due_date, recipients[0], title, data);

		this.recipients = new ArrayList<String>();
		for (String recipient : recipients) {
			recipient = validateEmail(recipient);
			if (recipient != null) {
				this.recipients.add(recipient);
			}
		}
	}

	int hash2id(String owner, Calendar creation_date) {
		int hash = 7;
		for (int i = 0; i < owner.length(); i++) {
			hash = hash * 5 + owner.charAt(i);
		}
		hash += 3 * creation_date.get(Calendar.DAY_OF_YEAR) + 11
				* creation_date.get(Calendar.HOUR_OF_DAY) + 13
				* creation_date.get(Calendar.MINUTE) + 17
				* creation_date.get(Calendar.SECOND);

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

	String validateEmail(String string) {
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(string);
		if (matcher.matches()) {
			return string;
		} else {
			return null;
		}
	}

	public abstract boolean isComplete();

	/**
	 * reurns a string of recipients in the following format
	 * username@servername.extension;username2@servername2.extension2;...
	 * 
	 * @return
	 */
	public String getRecipientsString() {
		if (this.recipients != null) {
			StringBuilder recString = new StringBuilder();
			for (String recipent : this.recipients) {
				recString.append(recipent + ";");
			}
		}

		return null;
	}
}
