import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.Semaphore;

/**
 * 
 * @author Alon Jackson and Gad Benram
 * 
 */
public class ServerRun {

	/** Maximum number of Threads allowed on this server */
	protected static int maxThreads;

	/** Port to listen on */
	protected static int port;

	/** semaphore to handle connections queuing */
	protected static Semaphore connectionLimiter;

	/** the default page name */
	protected static String defaultPage;

	/** the root directory */
	protected static String root;

	/** 404 page */
	protected static String $404page = "404.html";

	private static ServerSocket serverSocket;

	public static String mainPage = "main.html";

	public static String SMTP_USER_NAME = "tasker@cscidc.ac.il";
	public static String SMTP_PASSWORD = "password";
	public static String SMTP_SEVER = "compnet.idc.ac.il";
	public static int SMTP_PORT = 25;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		loadSettigs();
		System.out.println("Listening on port = " + port);
		connectionLimiter = new Semaphore(maxThreads);
		try {
			serverSocket = new ServerSocket(port);
			
			/*
			 * 
			 * For Debug!
			Email task = new Reminder("blabla", Calendar.getInstance(),
					Calendar.getInstance(), new String[] { "Alon239@gmail.com",
							"gidutz@gmail.com" }, "",
					"if u can read this, lab 2 is almost done",
					"gidutz@gmail.com");
			SMTPClient client = new SMTPClient(SMTP_SEVER, SMTP_PORT, true);
			try {
				client.sendMessage(task);
			} catch (SMTPExeption e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			while (true) {
				connectionLimiter.acquire();
				Socket clientSocket = serverSocket.accept();
				Thread handleConnection = new Thread(new ClientHandler(
						clientSocket));
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
			// TODO Auto-generated catch block
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
