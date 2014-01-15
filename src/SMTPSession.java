import org.apache.commons.codec.binary.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class SMTPSession {

    private final String CRLF = "\r\n";
    private final String USER_NAMES = "AlonJackson GadBenram";
    private String host;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private OutputStreamWriter out;
    private boolean useAuthentication;
    private final int SAY_HELLO = 1;
    private final int AUTHEN_USER = 2;
    private final int AUTHEN_PASS = 5;
    private final int AUTH_LOGIN = 7;
    private final int MAIL_TO = 3;
    private final int MAIL_FROM = 8;
    private final int MAIL_CONTENT = 6;
    private final int QUIT = 4;
    private final boolean AUTH_REQUIRED;

    /**
     * Constructs a new SMTPSession object
     *
     * @param host         The address of the SMTP server
     * @param port         the port to connect on the SMTP server
     * @param authRequired is Authentication Required?
     */
    public SMTPSession(String host, int port, boolean authRequired) {
        this.host = host;
        this.port = port;
        this.AUTH_REQUIRED = authRequired;
    }

    /**
     * Connects to the SMTP server
     */
    private synchronized void connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new OutputStreamWriter(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * closes the connection with the socket
     */
    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception ex) {
            // Ignore the exception. Probably the socket is not open.
        }
    }

    /**
     * waits for a response from the server
     * @return
     */
    private String getResponse() {
        String response = null;
        try {
            response = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Sends the email by implementing SMTP protocol
     * @param email the email to be sent
     * @throws IOException
     * @throws SMTPException
     */
    public synchronized void sendMessage(Email email) throws IOException, SMTPException {
        if (this.socket == null) {
            connect();
        }
        if (!this.socket.isConnected()) {
            throw new IOException("Connection to the server is closed");
        }

        String response = getResponse();
        System.out.println(response);
        if (!response.startsWith("220")) {
            throw new IOException("problem comuniation with the server");
        }

        doCommand(SAY_HELLO, this.USER_NAMES);

        //only if the authentication is required according to config.ini
        if (AUTH_REQUIRED) {
            doCommand(AUTH_LOGIN, null);
        }

		/*
         * Send the email, must be synchronized
		 */
        synchronized (email) {
            doCommand(MAIL_FROM, email.getOwner());
            for (String recipient : email.getRecipients()) {
                doCommand(MAIL_TO, recipient);
            }
            doCommand(MAIL_CONTENT, email.getData(), email);

        }
        doCommand(QUIT, null);
    }

    private int doCommand(int command, String param) throws IOException,
            SMTPException {
        return doCommand(command, param, null);
    }

    /**
     * Sends command to the server and waits for a response
     * Echoes the response to the Standard output (System.out)
     * @param command
     * @return
     * @throws IOException
     * @throws SMTPException
     */
    private int doCommand(int command, String param, Email email) throws IOException,
            SMTPException {
        String statement = null;

        param = (param == null) ? "" : param;

        switch (command) {

            case SAY_HELLO:
                statement = "EHLO " + param;
                sendStatement(statement);
                String response;

                do {
                    response = getResponse();
                    System.out.println(response);
                    if (!response.startsWith("250")) {
                        throw new SMTPException(
                                "problem communication with the server");
                    }
                } while (!response.startsWith("250 "));// with a space after 250

                break;

            case AUTH_LOGIN:
                statement = "AUTH LOGIN";
                sendStatement(statement);
                response = getResponse();
                System.out.println(response);
                if (!response.startsWith("334")) {
                    throw new SMTPException("Authentication Failed");
                }

            case AUTHEN_USER:
                statement = Base64.encodeBase64String(ServerRun.SMTP_USER_NAME.getBytes());
                sendStatement(statement);
                response = getResponse();
                System.out.println(response);
                if (!response.startsWith("334")) {
                    throw new SMTPException("Authentication Failed");
                }
            case AUTHEN_PASS:
                statement = Base64.encodeBase64String(ServerRun.SMTP_PASSWORD.getBytes());
                sendStatement(statement);
                response = getResponse();
                System.out.println(response);
                if (!response.startsWith("235")) {
                    throw new SMTPException("Authentication Failed");
                }
                break;

            case MAIL_TO:
                statement = "RCPT TO: <" + param + ">";
                sendStatement(statement);
                response = getResponse();
                System.out.println(response);
                if (!response.startsWith("250")) {
                    throw new SMTPException("Cannot send mail");
                }
                break;
            case MAIL_FROM:
                statement = "MAIL FROM: <" + param + ">";
                sendStatement(statement);
                response = getResponse();
                System.out.println(response);
                if (!response.startsWith("250")) {
                    throw new SMTPException("Cannot send mail");
                }
                break;
            case MAIL_CONTENT:
                statement = "DATA";
                sendStatement(statement);
                response = getResponse();
                System.out.println(response);
                if (!response.startsWith("354")) {
                    throw new SMTPException("Cannot send mail");
                }
                statement = prepareMail(email);
                sendStatement(statement);
                response = getResponse();
                System.out.println(response);
                if (!response.startsWith("250")) {
                    throw new SMTPException("Cannot send mail");
                }
                break;
            case QUIT:
                sendStatement("QUIT");
                response = getResponse();
                System.out.println(response);
                if (!response.startsWith("221")) {
                    throw new SMTPException("Cannot disconnect");
                }
                break;
        }
        return -1;
    }

    /**
     * Prepares the mail body
     * @param email
     * @return a string that represents the mail body
     */
    private String prepareMail(Email email) {
        StringBuilder message = new StringBuilder();
        message.append("FROM:" + "Mr. Tasker" + CRLF);
        message.append("To:");

        for (String recipient : email.getRecipients()) {
            message.append("<" + recipient + ">; ");
        }
        message.append(CRLF);
        message.append("Date: " + Email.DATE_FORMAT.format(email.getCreation_date().getTime()) + CRLF);
        message.append("Subject: " + email.getTitle() + CRLF);
        Scanner scanner = new Scanner(email.getData());
        while (scanner.hasNextLine()) {
            message.append(scanner.nextLine());
            message.append(CRLF);

        }
        message.append(".");

        return message.toString();
    }

    /**
     * Used to send the SMTP statement to the server and echo is to the screen
     * @param statement
     * @throws IOException
     */
    private void sendStatement(String statement) throws IOException {
        System.out.println(statement);
        out.write(statement);
        out.write(CRLF);
        out.flush();
    }
}
