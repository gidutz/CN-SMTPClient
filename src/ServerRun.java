import java.io.*;
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


    public static String mainPage = "main.html";

    public static String SMTP_USER_NAME = "tasker@cscidc.ac.il";
    public static String SMTP_PASSWORD = "password";
    public static String SMTP_SEVER = "compnet.idc.ac.il";
    public static int SMTP_PORT = 25;
    public static boolean AUTHENTICATE = true;

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
        connectionLimiter = new Semaphore(maxThreads);
        Thread serverRun = new Thread(new Runner());
        serverRun.start();



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
