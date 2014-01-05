import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.commons.codec.binary.Base64;

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

	public SMTPSession(String host, int port, boolean authRequired) {
		this.host = host;
		this.port = port;
		this.AUTH_REQUIRED = authRequired;
	}

	private void connect() {
		try {
			socket = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new OutputStreamWriter(socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (Exception ex) {
			// Ignore the exception. Probably the socket is not open.
		}
	}

	private String getResponse() {
		String response = null;
		try {
			response = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	public void sendMessage(Email email) throws IOException, SMTPExeption {
		if (this.socket == null){
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
		if (AUTH_REQUIRED) {
			doCommand(AUTH_LOGIN, null);
		}

		/*
		 * Send the email, must be synchronized
		 */
		synchronized (email) {
			doCommand(MAIL_FROM, email.getSender());
			for (String recipient : email.getRecipients()) {
				doCommand(MAIL_TO, recipient);
			}
			doCommand(MAIL_CONTENT, email.getData());

		}
		doCommand(QUIT, null);
	}

	/**
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws SMTPExeption
	 */
	private int doCommand(int command, String param) throws IOException,
			SMTPExeption {
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
					throw new SMTPExeption(
							"problem comuniation with the server");
				}
			} while (!response.startsWith("250 "));// with a space after 250

			break;

		case AUTH_LOGIN:
			statement = "AUTH LOGIN";
			sendStatement(statement);
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("334")) {
				throw new SMTPExeption("Authentication Failed");
			}

		case AUTHEN_USER:
			statement = Base64.encodeBase64String(ServerRun.SMTP_USER_NAME.getBytes());
			sendStatement(statement);
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("334")) {
				throw new SMTPExeption("Authentication Failed");
			}
		case AUTHEN_PASS:
			statement = Base64.encodeBase64String(ServerRun.SMTP_PASSWORD.getBytes());
			sendStatement(statement);
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("235")) {
				throw new SMTPExeption("Authentication Failed");
			}
			break;

		case MAIL_TO:
			statement = "RCPT TO: <" + param + ">";
			sendStatement(statement);
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("250")) {
				throw new SMTPExeption("Cannot send mail");
			}
			break;
		case MAIL_FROM:
			statement = "MAIL FROM: <" + param + ">";
			sendStatement(statement);
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("250")) {
				throw new SMTPExeption("Cannot send mail");
			}
			break;
		case MAIL_CONTENT:
			statement = "DATA";
			sendStatement(statement);
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("354")) {
				throw new SMTPExeption("Cannot send mail");
			}
			statement = prepatreMail(param);
			sendStatement(statement);
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("250")) {
				throw new SMTPExeption("Cannot send mail");
			}
			break;
		case QUIT:
			sendStatement("QUIT");
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("221")) {
				throw new SMTPExeption("Cannot disconnect");
			}
			break;
		}
		return -1;
	}

	private String prepatreMail(String data) {
		StringBuilder message = new StringBuilder();
		message.append("FROM:" + "<gidutz@gmail.com>"+CRLF);
		message.append("To: \"An Jackie?\" <Alon239@gmail.com>"+CRLF);
		message.append("Date: Tue, 1 January 2014 16:02:43 -0500"+CRLF);
		message.append("Subject: Test Message From Lab 2"+CRLF);

		message.append(data);
		message.append(CRLF);
		message.append(".");

		return message.toString();
	}

	private void sendStatement(String statement) throws IOException {
		System.out.println(statement);
		out.write(statement);
		out.write(CRLF);
		out.flush();
	}
}
