import java.util.*;

public class Poll extends Email {
    private boolean sent;
    ArrayList<String> recipients;
    PollArray results;


    public Poll(String owner, Calendar creation_date, Calendar due_date,
                String[] recipients, String title, String data, boolean completed, boolean sent) {
        super(owner, creation_date, due_date, recipients[0], title, data);
        this.completed = completed;
        this.recipients = new ArrayList<String>();
        for (String recipient : recipients) {
            String validated = this.validateEmail(recipient);
            if (validated != null)
                this.recipients.add(validated);
        }
        this.results = new PollArray(10);

        this.sent = sent;
    }

    public Poll(String owner, Calendar creation_date, Calendar due_date,
                String[] recipients, String title, String data, boolean completed, PollArray results, boolean sent) {
        super(owner, creation_date, due_date, recipients, title, data, completed);
        this.recipients = new ArrayList<String>();
        for (String recipient : recipients) {
            String validated = this.validateEmail(recipient);
            if (validated != null)
                this.recipients.add(validated);
        }
        this.results = results;
        this.sent = sent;

    }


    public void addVote(int result) {
        this.results.addVote(result);

    }

    /**
     * Checks the complete status of the poll
     *
     * @return
     */
    public boolean isComplete() {
        return this.checkStatus();
    }

    @Override
    public void setCompleted(boolean status) {
        this.completed = status;
    }

    @Override
    public boolean wasSent() {
        return this.sent;
    }

    /**
     * return true only if all the recipients have answered the poll
     *
     * @return
     */
    public boolean checkStatus() {

        //sum all the fileds and check if the sum equals the size of the recipients array
        return this.results.getVotes() >= recipients.size();
    }

    /**
     * @return
     */
    @Override
    public String getData() {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(this.data);
        dataBuilder.append(CRLF);
        dataBuilder.append("to answer please click on the following link (ONLY VOTE ONCE!!!)");

        for (int i = 1; i <= 10; i++) {

            dataBuilder.append("http://" + ServerRun.SERVER_NAME + "/polly_reply?id=" + id + "&ans=" + i);
            dataBuilder.append(CRLF);

        }
        return dataBuilder.toString();
    }

    /**
     * returns the poll results as string
     *
     * @return
     */
    public String getPollResults() {
        return this.results.toString();
    }

    public void setSendStatus (boolean stat){
        this.sent = stat;
    }
}
