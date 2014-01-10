import java.io.*;
import java.util.*;

public class TasksHandler extends EmailHandler<Task> {

    /**
     * @param taskList
     */
    public TasksHandler(EmailArrayList<Task> taskList) {
        super(taskList);
    }

    /**
     * @param email
     */
    @Override
    public void handelEmail(Email email) {
        Task task = (Task) email;
        if (!task.isComplete() && !task.wasSent()) {
            try {

                SMTPSession smtpSession = new SMTPSession(ServerRun.SMTP_SEVER, ServerRun.SMTP_PORT, ServerRun.AUTHENTICATE);
                StringBuilder data = new StringBuilder();

                data.append( "The task you gave " + task.getRecipient());
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

                data.append( "The task you gave " + task.getRecipient());
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
}
