import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Alon Jackson and Gad Benram
 */
public class ServerRun {

    /**
     * Maximum number of Threads allowed on this server
     */
    protected static int maxThreads;

    /**
     * Port to listen on
     */
    protected static int port;

    /**
     * semaphore to handle connections queuing
     */
    protected static Semaphore connectionLimiter;

    /**
     * the default page name
     */
    protected static String defaultPage;

    /**
     * the root directory
     */
    protected static String root;

    /**
     * 404 page
     */
    protected static String $404page = "404.html";

    private static ServerSocket serverSocket;

    public static String mainPage = "main.html";

    public static String SMTP_USER_NAME = "tasker@cscidc.ac.il";
    public static String SMTP_PASSWORD = "password";
    public static String SMTP_SEVER = "compnet.idc.ac.il";
    public static int SMTP_PORT = 25;
    public static boolean AUTHENTICATE = true;
    public static EmailArrayList<Task> taskList;
    public static EmailArrayList<Reminder> reminderList;
    public static EmailArrayList<Poll> pollList;
    public static SQLiteDBHelper db;

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {

            loadSettigs();
        } catch (NullPointerException e) {
            System.err.println("Cannot find config.ini. Server must shut down");
            System.exit(2);
        }

        try {
            db = new SQLiteDBHelper();

            loadDB();
        } catch (Exception e) {
            // TODO: cannot locate DB!
        }

        try {
            connectionLimiter = new Semaphore(maxThreads);

            serverSocket = new ServerSocket(port);
            System.out.println("Listening on port = " + port);

            startHandlingEmails();

			/*
             *
			 * For Debug! Email task = new Reminder("blabla",
			 * Calendar.getInstance(), Calendar.getInstance(), new String[] {
			 * "Alon239@gmail.com", "gidutz@gmail.com" }, "",
			 * "if u can read this, lab 2 is almost done", "gidutz@gmail.com");
			 * SMTPClient client = new SMTPClient(SMTP_SEVER, SMTP_PORT, true);
			 * try { client.sendMessage(task); } catch (SMTPExeption e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); }
			 */
            while (true) {
                connectionLimiter.acquire();
                Socket clientSocket = serverSocket.accept();
                Thread handleConnection = new Thread(new ClientHandler(
                        clientSocket, taskList, reminderList, pollList));
                System.out
                        .println("Connection established, total connections = "
                                + (maxThreads - connectionLimiter
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

    private static void startHandlingEmails() {
        Thread tasksHendler = new Thread(new TasksHandler(taskList));
        Thread pollsHandler = new Thread(new PollsHandler(pollList));
        Thread remindersHandler = new Thread(new RemindersHandler(reminderList));
        tasksHendler.start();
        pollsHandler.start();
        remindersHandler.start();
    }

    private static void loadDB() {

        db.openDatabase("", "emails");

        taskList = db.getAllTasks();
        pollList = db.getAllPolls();
        reminderList = db.getAllReminders();
    }

    /**
     * loads proprtires from a cobfiguration file
     */
    private static void loadSettigs() {

        Properties prop = new Properties();

        try {

            InputStream inputStream = ServerRun.class.getClassLoader()
                    .getResourceAsStream("config.ini");

            prop.load(inputStream);

            // get the properties
            root = prop.getProperty("root");
            defaultPage = prop.getProperty("defaultPage");
            maxThreads = Integer.parseInt(prop.getProperty("maxThreads"));
            port = Integer.parseInt(prop.getProperty("port"));
            if (!root.endsWith("/")) {
                root = root + "/";
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

    }
}
