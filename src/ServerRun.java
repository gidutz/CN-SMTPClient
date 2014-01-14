import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Alon Jackson and Gad Benram
 */
public class ServerRun {

    /**
     * Maximum number of Threads allowed on this server
     */

    static int maxThreads;

    /**
     * Port to listen on
     */
    public static int port;

    /**
     * semaphore to handle connections queuing
     */
    public static Semaphore connectionLimiter;

    /**
     * the default page name
     */
    public static String defaultPage;

    /**
     * the root directory
     */
    public static String root;

    /**
     * 404 page
     */
    public static String $404page = "404page.html";
    public static String $403page = "403page.html";


    public static String mainPage = "main.html";

    public static String SMTP_USER_NAME = "tasker@cscidc.ac.il";
    public static String SMTP_PASSWORD = "password";
    public static String SMTP_SEVER = "compnet.idc.ac.il";
    public static int SMTP_PORT = 25;
    public static boolean AUTHENTICATE = true;
    public static String DB_PATH = "./test.db";
    public static String SERVER_NAME = "localhost";

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

        Field[] declaredFields = ServerRun.class.getDeclaredFields();
        for (Field field : declaredFields) {
            Object obj = null;
            try {
                obj = field.get(field.getType());
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && obj == null) {
                    System.err.println("Cannot obtain value for required field!");
                    System.err.println(field.getName() + " is missing");
                    System.exit(1);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }
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
            $404page = "404page.html";
            $403page = "403page.html";


            mainPage = "main.html";

            SMTP_USER_NAME = prop.getProperty("SMTPUsername");
            SMTP_PASSWORD = prop.getProperty("SMTPPassword");
            SMTP_SEVER = prop.getProperty("SMTPName");
            SMTP_PORT = Integer.parseInt(prop.getProperty("SMTPPort"));
           

            AUTHENTICATE = prop.getProperty("SMTPIsAuthLogin").equalsIgnoreCase("true");
            DB_PATH = prop.getProperty("DB_PATH");

            String SERVER_NAME = prop.getProperty("ServerName");

        } catch (IOException e) {
            System.err.println("problem loading settings");
            System.err.println(e);

            System.exit(1);
        }


    }


}
