import java.io.*;
import java.net.*;

/**
 * A thread to handle a single client Accepts the input, parses and outputs a
 * response *
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;

    /* --! connection time out for the socket -- */
    private static final int CONNECTION_TIMEOUT = 30 * 1000;

    // why 857? because i can!

    public ClientHandler(Socket clientSocket) {

        this.clientSocket = clientSocket;
    }

    /**
     * Starts handling the client
     */
    @Override
    public void run() {
        System.out.println(clientSocket);
        try {
            clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
            HttpParser request = new HttpParser(clientSocket.getInputStream());

            logRequest(request);
            /*
             * if the user logs in, then generate cookie
			 */
            String path = redirect(request);
            int responseCode = 200;
            HttpResponder responder = new HttpResponder(path, responseCode,
                    clientSocket.getOutputStream());
            // make HTTP response
            responder.echoResponse();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ServerRun.connectionLimiter.release();
        System.out.println("Connection eliminated with " + clientSocket);
    }

    private void logRequest(HttpParser parser) {
        System.out.println("method=" + parser.getMethod());
        System.out.println("url=" + parser.getRequestURL());
        System.out.println("headers = " + parser.getHeaders());
        System.out.println("vars = " + parser.getParams());
        System.out.println(parser.getHeaders().toString());
    }

    private String redirect(HttpParser request) {
        String path = request.getRequestURL();
        if (path.startsWith("/")) {
            path = path.substring(1);

        }
        // change absolute path to relative path
        if (path.startsWith("http://" + request.getHeader("host"))) {
            path = path.substring(("http://" + request.getHeader("host"))
                    .length() + 1);
        }

        path = ServerRun.root + path;
        File page = new File(path);
        if (page.isDirectory()) {
            path = path + ServerRun.defaultPage;
            page = new File(path);
        }


        if (request.getHeader("Cookie") == null) {
            if (!path.contains("/images/") && !path.contains("/css/")) {
                path = ServerRun.root + ServerRun.defaultPage;

            } else if (path.endsWith("task_reply.html")) {
                //TODO: handle task reply

            } else if (path.endsWith("poll_reply.html")) {
                //TODO:handle poll reply
            }
            page = new File(path);

        } else { //There is a cookie with a user name
                String[] cookie = request.getHeader("Cookie").split("=");
                String user = cookie[1];
                String id = null;
            if (path.endsWith("index.html")) {
                path = ServerRun.root + ServerRun.mainPage;
            }else  if (path.endsWith("tasks.html")) {
                if ((id = request.getParam("id")) != null) {
                    deleteEmail(id);

                } else {
                    path = generateTasks(user);
                }
            } else if (path.endsWith("reminders.html")) {
                if ((id = request.getParam("id")) != null) {
                    deleteEmail(id);

                } else {
                    path = generateReminders(user);
                }
            } else if (path.endsWith("polls.html")) {
                if ((id = request.getParam("id")) != null) {
                    deleteEmail(id);

                } else {
                    path = generatePolls(user);
                }
            } else if (path.endsWith("submit_reminder.html")) {
                if (request.getParam("id").equalsIgnoreCase("new")) {
                    //TODO: create new reminder and redirect to the list;
                } else {
                    //TODO: update the reminder whose id = getParam("id")
                }

            } else if (path.endsWith("submit_task.html")) {
                if (request.getParam("id").equalsIgnoreCase("new")) {
                    //TODO: create new reminder and redirect to the list;
                } else {
                    //TODO: update the reminder whose id = getParam("id")
                }
            } else if (path.endsWith("submit_poll.html")) {
                if (request.getParam("id").equalsIgnoreCase("new")) {
                    //TODO: create new reminder and redirect to the list;
                } else {
                    //TODO: update the reminder whose id = getParam("id")
                }
            }


        }

        if (page.getAbsoluteFile().isAbsolute()) {
            // TODO:check if the server root path is the start of

            return path;
        } else if (!(new File(path)).exists()) {
            path = path + ServerRun.$404page;
        }

        return path;
    }

    private String generateReminders(String user) {

        //TODO: Create file reminders and return it as path
        return "reminders.html";

    }

    private String generatePolls(String user) {
        //TODO: Create file reminders and return it as path

        return "reminders.html";

    }


    private String generateTasks(String user) {
        //TODO: Create file reminders and return it as path
        return "reminders.html";

    }


    private void deleteEmail(String id) {

        //Delete the poll and
    }

}