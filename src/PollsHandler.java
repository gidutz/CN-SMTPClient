import java.io.*;
import java.util.*;

public class PollsHandler extends EmailHandler<Poll> {

    public PollsHandler(EmailArrayList<Poll> emails, SQLiteDBHelper db) {
        super(emails, db);
    }

    @Override
    public void handelEmail(Email email) {
        Poll poll = (Poll) email;
        if (poll.isComplete() && !poll.wasSent()) {
            try {

                SMTPSession smtpSession = new SMTPSession(ServerRun.SMTP_SEVER, ServerRun.SMTP_PORT, ServerRun.AUTHENTICATE);
                StringBuilder data = new StringBuilder();

                data.append("The poll you sent is complete " + poll.getTitle() + "\r\n");
                data.append(" Here are the final results \r\n");
                for (int i = 0; i < poll.results.getSize(); i++) {
                    data.append("Option " + i + ": ");
                    data.append(poll.results.getVoteForOption(i));
                    data.append("\r\n");

                }
                Reminder taskCompleteNotice = new Reminder(poll.owner, Calendar.getInstance(),
                        Calendar.getInstance(), poll.owner, "The poll you sent is complete!", data.toString(), true);
                smtpSession.sendMessage(taskCompleteNotice);
                poll.setSendStatus(true);
                emails.update(email);
            } catch (SMTPException smtpException) {
                smtpException.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

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
