import java.util.*;

public class Poll extends Email {
    private boolean sent;
    ArrayList<String> recipients;
    PollArray results;
    String[] options;


    public Poll(String owner, Calendar creation_date, Calendar due_date,
                String[] recipients, String title, String data, boolean completed, PollArray results, String[] options, boolean sent) {
        super(owner, creation_date, due_date, recipients, title, data, completed);
        this.recipients = new ArrayList<String>();
        for (String recipient : recipients) {
            String validated = this.validateEmail(recipient);
            if (validated != null)
                this.recipients.add(validated);
        }
        this.options = options;
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

        for (int i = 0; i < this.options.length; i++) {
            dataBuilder.append("<a href=");

            dataBuilder.append("http://" + ServerRun.SERVER_NAME + ":" + ServerRun.port);
            dataBuilder.append("/poll_reply.html?id=" + id + "&ans=" + i);
            dataBuilder.append(">");
            dataBuilder.append(options[i]);
            dataBuilder.append("</a>");

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

    public void setSendStatus(boolean stat) {
        this.sent = stat;
    }

    public String getOptions() {
        StringBuilder sb = new StringBuilder();
        for (String option : this.options) {
            sb.append(option);
            sb.append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();

    }

    /**
     * returns the option an index = option
     *
     * @param option
     * @return
     */
    public String getOption(int option) {
        return this.options[option];
    }

    /**
     * retutns the total number of options
     *
     * @return
     */
    public int getOptionsCount() {
        return this.options.length;
    }

    /**
     * returns the option an index = option
     *
     * @param option
     * @return
     */
    public int getVotesForOption(int option) {
        return this.results.getVoteForOption(option);
    }

    /**
     * poll is never expired since it is not given a due date
     * instead this method refers to isComplete(), stating if the poll has got the expected number of votes.
     *
     * @return
     */
    @Override
    public boolean isExpired() {
        return this.isComplete();
    }
}
