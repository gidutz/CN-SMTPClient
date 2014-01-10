
public class PollsHandler extends EmailHandler<Poll> {

	public PollsHandler(EmailArrayList<Poll> emails,SQLiteDBHelper db) {
		super(emails, db);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handelEmail(Email email) {
		// TODO Auto-generated method stub

	}

    @Override
    protected void loadFromDatabase() {
        EmailArrayList<Poll> pollsList = new EmailArrayList<Poll>(db);
        pollsList = db.getAllPolls(pollsList);
        emails.clear();
        for (Email email : pollsList) {
            emails.loadFromDisk(email);
        }
    }

}
