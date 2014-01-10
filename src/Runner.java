import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Created by gidutz on 1/10/14.
 */
public class Runner implements Runnable {
    public static volatile EmailArrayList<Task> tasksList;
    public static volatile EmailArrayList<Reminder> reminderList;
    public static volatile EmailArrayList<Poll> pollList;
    public SQLiteDBHelper db;

    public void run() {
        db = new SQLiteDBHelper();
        ServerSocket serverSocket = null;

        loadDB();
        try {
            ServerRun.connectionLimiter = new Semaphore(ServerRun.maxThreads);

            serverSocket = new ServerSocket(ServerRun.port);
            System.out.println("Listening on port = " + ServerRun.port);

            startHandlingEmails();

			/*
             *
			 * For Debug! Email task = new Reminder("blabla",
			 * Calendar.getInstance(), Calendar.getInstance(), new String[] {
			 * "Alon239@gmail.com", "gidutz@gmail.com" }, "",
			 * "if u can read this, lab 2 is almost done", "gidutz@gmail.com");
			 * SMTPClient client = new SMTPClient(SMTP_SEVER, SMTP_PORT, true);
			 * try { client.sendMessage(task); } catch (SMTPException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); }
			 */
            while (true) {
                ServerRun.connectionLimiter.acquire();
                Socket clientSocket = serverSocket.accept();


                loadDB();

                Thread handleConnection = new Thread(new ClientHandler(clientSocket, tasksList, reminderList, pollList));
                System.out
                        .println("Connection established, total connections = "
                                + (ServerRun.maxThreads - ServerRun.connectionLimiter
                                .availablePermits()));
                handleConnection.start();

            }

        } catch (IOException e) {
            System.err.println(e);
            System.out
                    .println("\r\nOh no, the server is down, please to call Shraga "
                            + "\nhttp://www.youtube.com/watch?v=BaMTbN6Iz3g");

            System.exit(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);

        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    private void loadDB() {

        db.openDatabase("", "emails");
        tasksList = new EmailArrayList<Task>(db);
        reminderList = new EmailArrayList<Reminder>(db);
        pollList = new EmailArrayList<Poll>(db);

        tasksList = db.getAllTasks(tasksList);
        pollList = db.getAllPolls(pollList);
        reminderList = db.getAllReminders(reminderList);
    }

    private void startHandlingEmails() {
        Thread tasksHandler = new Thread(new TasksHandler(tasksList,db));
        Thread pollsHandler = new Thread(new PollsHandler(pollList,db));
        Thread remindersHandler = new Thread(new RemindersHandler(reminderList,db));
        tasksHandler.start();
        pollsHandler.start();
        remindersHandler.start();

    }
}
