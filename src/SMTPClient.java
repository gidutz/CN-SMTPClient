import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.commons.codec.binary.Base64;

public class SMTPClient {

	private final String CRLF = "\r\n";
	private final String CLIENT_NAME = "NoaKochav AmitGilat";
	private final String AUTH_LOGIN = "AUTH LOGIN";
	private String host;
	private int port;
	private Socket socket;
	private BufferedReader in;
	private OutputStreamWriter out;

	public SMTPClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private void getConnection() {
		try {
			socket = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new OutputStreamWriter(socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void close() {
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
			// TODO: Auto-generated catch block e.printStackTrace();
		}
		return response;
	}

	public void sendMessage() {
		// TODO:
	}

	public void connectWithAuthLogin() {
		getConnection();
		// After connecting, the SMTP server will send a response string.
		String response = getResponse();
		System.out.println(response);
		if (!response.startsWith("220")) {
			System.err.println("Could not connect to the server");
		}
		try {
			out.write("EHLO " + CLIENT_NAME + CRLF);
			out.flush();
			System.out.println("EHLO " + CLIENT_NAME);
			response = getResponse();
			System.out.println(response);
			if (!response.startsWith("250")) {
				System.err.println("Error with the SMTP server");
			}
			do {
				response = getResponse();
				System.out.println(response);
			} while (!response.equalsIgnoreCase("250 AUTH LOGIN"));
			out.write(AUTH_LOGIN + CRLF);
			out.flush();
			System.out.println(AUTH_LOGIN);
			response = getResponse();
			System.out.println(response);

			String userNameEncoded = Base64.encodeBase64String("".getBytes());
			System.out.println(Base64.decodeBase64(userNameEncoded));
			out.write(userNameEncoded);
			out.flush();
			System.out.println(userNameEncoded);
			out.write(CRLF);
			out.flush();
			response = getResponse();
			System.out.println(response);
			String passwordEncoded = Base64.encodeBase64String("".getBytes());
			out.write(passwordEncoded);
			out.flush();
			System.out.println(passwordEncoded);
			out.write(CRLF);
			out.flush();
			response = getResponse();
			System.out.println(response);
			getResponse();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
