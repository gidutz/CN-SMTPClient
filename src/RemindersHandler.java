import java.io.*;

/**
 * Extends EmailHandler/
 * Checks when if there exists a reminder that should be sent, sends it and updates its fields
 */
public class RemindersHandler extends EmailHandler<Reminder> {

    public RemindersHandler(EmailArrayList<Reminder> emails, SQLiteDBHelper db) {
        super(emails, db);
    }

    /**
     * Handles an expired Reminder by sending it
     * @param email
     */
    @Override
    public synchronized void handelEmail(Email email) {
        Reminder reminder = (Reminder) email;
        if (!reminder.isComplete()) {
            try {
                SMTPSession smtpSession = new SMTPSession(ServerRun.SMTP_SEVER, ServerRun.SMTP_PORT, ServerRun.AUTHENTICATE);
                smtpSession.sendMessage(reminder);
                reminder.setCompleted(true);
                emails.update(email);
            } catch (SMTPException smtpException) {
                smtpException.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * Loads the reminders from the database
     */
    @Override
    protected void loadFromDatabase() {
        EmailArrayList<Reminder> remindersList = new EmailArrayList<Reminder>(db);
        remindersList = db.getAllReminders(remindersList);
        emails.clear();
        for (Email email : remindersList) {
            emails.loadFromDisk(email);
        }
    }


}
