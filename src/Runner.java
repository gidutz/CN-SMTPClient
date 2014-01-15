import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * This file is used to run the server
 * it constructs a ServerSocket and waits for incoming connections
 */
public class Runner implements Runnable {
    public static volatile EmailArrayList<Task> tasksList;
    public static volatile EmailArrayList<Reminder> reminderList;
    public static volatile EmailArrayList<Poll> pollList;
    public SQLiteDBHelper db;

    public void run() {
        db = new SQLiteDBHelper();
        ServerSocket serverSocket = null;

        /**
         * creates or opens the database
         */
        db.openDatabase(ServerRun.POLL_DB);
        db.openDatabase(ServerRun.REM_DB);
        db.openDatabase(ServerRun.TASK_DB);

        loadDB(); //loads the data from the disk database

        try {
            ServerRun.connectionLimiter = new Semaphore(ServerRun.maxThreads);

            serverSocket = new ServerSocket(ServerRun.port);
            System.out.println("Listening on port = " + ServerRun.port);

            startHandlingEmails();

            /*
            * wait for connection. the connections are limited by a semaphore
             */
            while (true) {
                ServerRun.connectionLimiter.acquire();
                Socket clientSocket = serverSocket.accept();


                loadDB();//reloads the data from the dist

                //Handles the incoming connection
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
                    .println("\r\nOh no, the server is down, please call Shraga "
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


    /**
     * loads the emails from the database
     */
    private void loadDB() {

        tasksList = new EmailArrayList<Task>(db);
        reminderList = new EmailArrayList<Reminder>(db);
        pollList = new EmailArrayList<Poll>(db);

        tasksList = db.getAllTasks(tasksList);
        pollList = db.getAllPolls(pollList);
        reminderList = db.getAllReminders(reminderList);
        db.initializeChatTable();
    }

    /**
     * starts Three threads that handle the emails
     */
    private void startHandlingEmails() {
        Thread tasksHandler = new Thread(new TasksHandler(tasksList, db));
        Thread pollsHandler = new Thread(new PollsHandler(pollList, db));
        Thread remindersHandler = new Thread(new RemindersHandler(reminderList, db));
        tasksHandler.start();
        pollsHandler.start();
        remindersHandler.start();

    }
}
