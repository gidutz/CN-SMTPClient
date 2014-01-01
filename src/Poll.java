import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

public class Poll extends Email {

	ArrayList<String> recipients;
	TreeMap<String, Integer> results;

	public Poll(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data) {
		super(owner, creation_date, due_date, recipient, title, data);
		this.recipients = new ArrayList<String>();
		recipients.add(recipient);
		this.results = new TreeMap<String, Integer>();
	}

	public Poll(String owner, Calendar creation_date, Calendar due_date,
			String[] recipients, String title, String data) {
		super(owner, creation_date, due_date, null, title, data);
		this.recipients = new ArrayList<String>();

		for (String recipient : recipients) {
			this.recipients.add(recipient);

		}
	}

	public void updateVote() {

	}

	@Override
	public boolean isComplete() {
		
		for(String recipient : this.recipients){
			if (this.results.get(recipient)==-1)
				return false;
		}
		return true;
	}

}