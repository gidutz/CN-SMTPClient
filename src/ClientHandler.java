import java.io.*;
import java.net.*;

/**
 * A thread to handle a single client Accepts the input, parses and outputs a
 * response *
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;

    /* --! connection time out for the socket -- */
    private static final int CONNECTION_TIMESOUT = 30 * 1000;

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
            clientSocket.setSoTimeout(CONNECTION_TIMESOUT);
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
        // TODO:if task_reply?task= ->
        // TODO: if poll_reply?

        if (request.getHeader("Cookie") == null) {
            if (!path.contains("/images/") && !path.contains("/css/"))
                path = ServerRun.root + ServerRun.defaultPage;
            page = new File(path);

        } else {
            // if list.html?type=reminders ->get the user from the cookie
            // if list.html?type=tasks ->get the user from the cookie
            // if list.html?type=polls->get the user from the cookie
            // if delete?id= delete and reload
            // if

        }

        if (page.getAbsoluteFile().isAbsolute()) {
            // TODO:check if the server root path is the start of

            return path;
        } else if (!page.exists()) {
            path = path + ServerRun.$404page;

            return path;
        }

        return path;
    }
}
