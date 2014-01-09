import java.util.*;

public class Poll extends Email {

    ArrayList<String> recipients;
    int[] results;


    public Poll(String owner, Calendar creation_date, Calendar due_date,
                String recipient, String title, String data, boolean completed) {
        super(owner, creation_date, due_date, recipient, title, data);
        this.completed = completed;
        this.recipients = new ArrayList<String>();
        recipients.add(recipient);
        this.results = new int[10];
    }
    public Poll(String owner, Calendar creation_date, Calendar due_date,
                String recipient, String title, String data, boolean completed, int[]results) {
        super(owner, creation_date, due_date, recipient, title, data);
        this.completed = completed;
        this.recipients = new ArrayList<String>();
        recipients.add(recipient);
        this.results = results;
    }
    public Poll(String owner, Calendar creation_date, Calendar due_date,
                String recipients[], String title, String data, boolean completed, int[]results) {
        super(owner, creation_date, due_date, recipients, title, data,completed);
        this.results = results;
    }
    public void updateVote(int result) {
        this.results[result]++;

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

    /**
     * return true only if all the recipients have answered the poll
     *
     * @return
     */
    public boolean checkStatus() {
        int sumReplies = 0;
        for (int i =0; i<this.results.length;i++){
            sumReplies += results[i];
        }
        //sum all the fileds and check if the sum equals the size of the recipients array
        return sumReplies>=recipients.size();
    }

}
