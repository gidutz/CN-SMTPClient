import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

/**
 * A thread to handle a single client Accepts the input, parses and outputs a
 * response *
 */
public class ClientHandler implements Runnable {
	private Socket clientSocket;

	/* --! connection time out for the socket -- */
	private static final int CONNECTION_TIMESOUT = 30*1000;

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
			HttpParser parser = new HttpParser(clientSocket.getInputStream());
			
			logRequest(parser);
			/*
			 *if the user logs in, then generate cookie 
			 */
			
			HttpResponder responder = new HttpResponder(parser,clientSocket.getOutputStream());
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

	
	private void logRequest(HttpParser parser){
		System.out.println("method=" + parser.getMethod());
		System.out.println("url=" + parser.getRequestURL());
		System.out.println("headers = "
				+ parser.getHeaders());
		System.out.println("vars = " + parser.getParams());
		System.out.println(parser.getHeaders().toString());
	}
}
