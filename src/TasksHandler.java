import java.io.*;
import java.util.*;

/**
 * This class handles tasks, when a task is complete or expired, an appropriate message is sent to the owner
 */
public class TasksHandler extends EmailHandler<Task> {

    /**
     * @param taskList
     */
    public TasksHandler(EmailArrayList<Task> taskList, SQLiteDBHelper db) {
        super(taskList, db);
    }

    /**
     * Handles a Task by sending a message to the owner.
     * only expired or completed tasks should be here.
     * @param email
     */
    @Override
    public void handelEmail(Email email) {
        Task task = (Task) email;
        if (!task.isComplete() && !task.wasSent()) {
            try {

                SMTPSession smtpSession = new SMTPSession(ServerRun.SMTP_SEVER, ServerRun.SMTP_PORT, ServerRun.AUTHENTICATE);
                StringBuilder data = new StringBuilder();

                data.append("The task you gave " + task.getRecipient());
                data.append(" is out dated and incomplete! :( you should fire him. ");
                data.append(CRLF + "original message was:");
                data.append(task.getData());
                Reminder taskCompleteNotice = new Reminder(task.owner, Calendar.getInstance(),
                        Calendar.getInstance(), task.owner, "Task out of fate!", data.toString(), true);
                smtpSession.sendMessage(taskCompleteNotice);
                task.setSendStatus(true);
                emails.update(email);
            } catch (SMTPException smtpException) {
                smtpException.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (task.isComplete() && !task.wasSent()) {
            try {

                SMTPSession smtpSession = new SMTPSession(ServerRun.SMTP_SEVER, ServerRun.SMTP_PORT, ServerRun.AUTHENTICATE);
                StringBuilder data = new StringBuilder();

                data.append("The task you gave " + task.getRecipient());
                data.append(" is complete!");
                data.append(CRLF + "original message was:");
                data.append(task.getData());
                Reminder taskCompleteNotice = new Reminder(task.owner, Calendar.getInstance(),
                        Calendar.getInstance(), task.owner, "Task completed!", data.toString(), true);
                smtpSession.sendMessage(taskCompleteNotice);
                task.setSendStatus(true);
                emails.update(email);
            } catch (SMTPException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * loads an email from the database to the main memory
     */
    @Override
    protected void loadFromDatabase() {
        EmailArrayList<Task> tasksList = new EmailArrayList<Task>(db);
        tasksList = db.getAllTasks(tasksList);
        emails = new EmailArrayList<Email>(db);

        for (Email email : tasksList) {
            emails.loadFromDisk(email);
        }
    }
}
