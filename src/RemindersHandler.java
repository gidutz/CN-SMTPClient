import java.io.*;

public class RemindersHandler extends EmailHandler<Reminder> {

    public RemindersHandler(EmailArrayList<Reminder> emails, SQLiteDBHelper db) {
        super(emails, db);
    }

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
