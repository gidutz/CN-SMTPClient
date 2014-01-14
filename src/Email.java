import java.text.*;
import java.util.*;
import java.util.regex.*;

public abstract class Email {
    String owner;
    Calendar creation_date;
    Calendar due_date;
    String title;
    String data;
    int id;
    boolean completed;
    ArrayList<String> recipients;
    final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    final String CRLF = "\r\n";

    public Email(String owner, Calendar creation_date, Calendar due_date,
                 String recipient, String title, String data) {
        this(owner, creation_date, due_date, recipient, title, data, false);

    }


    public Email(String owner, Calendar creation_date, Calendar due_date,
                 String recipient, String title, String data, boolean completed) {
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
        this.completed = completed;
    }

    public Email(String owner, Calendar creation_date, Calendar due_date,
                 String[] recipients, String title, String data, boolean completed) {
        this(owner, creation_date, due_date, recipients[0], title, data, completed);

        this.recipients = new ArrayList<String>();
        for (String recipient : recipients) {
            recipient = validateEmail(recipient);
            if (recipient != null) {
                this.recipients.add(recipient);
            }
        }
    }


    /**
     * Called without completed value, defaults completed is false
     *
     * @param owner
     * @param creation_date
     * @param due_date
     * @param recipients
     * @param title
     * @param data
     */
    public Email(String owner, Calendar creation_date, Calendar due_date,
                 String[] recipients, String title, String data) {
        this(owner, creation_date, due_date, recipients[0], title, data, false);

        this.recipients = new ArrayList<String>();
        for (String recipient : recipients) {
            recipient = validateEmail(recipient);
            if (recipient != null) {
                this.recipients.add(recipient);
            }
        }
    }

    /**
     * Creates id using the fields: owner, creation date and title
     *
     * @param owner
     * @param creation_date
     * @return
     */
    int hash2id(String owner, Calendar creation_date) {
        int hash = 7;
        for (int i = 0; i < owner.length(); i++) {
            hash = hash * 5 + owner.charAt(i);
        }
        hash += 3 * creation_date.get(Calendar.DAY_OF_YEAR) + 11
                * creation_date.get(Calendar.HOUR_OF_DAY) + 13
                * creation_date.get(Calendar.MINUTE) + 17;

        hash += Math.random() * 13 * 17;
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

    /**
     * Gets the content of the email, sh
     *
     * @return
     */
    public abstract String getData();

    /**
     * if the due date is past the current date returns true
     *
     * @return
     */
    public boolean isExpired() {
        Calendar currentDate = Calendar.getInstance();
        return currentDate.after(this.due_date);
    }

    /**
     * @return
     */
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
            for (String recipient : this.recipients) {
                recString.append(recipient + ";");
            }
            recString.deleteCharAt(recString.length()-1);
            return recString.toString();

        }

        return null;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract void setCompleted(boolean status);

    public abstract boolean wasSent();

}
