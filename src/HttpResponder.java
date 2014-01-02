import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpResponder {
	private int responseCode;
	private PrintStream out;
	private final String CRLF = "\r\n";
	private File page;
	private final int CHUNK_SIZE = 1024;
	private boolean chunked;
	private Hashtable<String, String> params;

	/**
	 * This class is instanced to an object that accepts a parsed HTTP request
	 * and the client output stream and generates a response accordingly
	 * 
	 * @param parsedRequest
	 *            an object representing the parsed HTTP request
	 * @param outputStream
	 *            the client socoket's output stream
	 * @throws IOException
	 */
	public HttpResponder(HttpParser parser, OutputStream outputStream)
			throws IOException {
		this.page = new File (parser.getRequestURL());
		this.chunked = parser.getHeader("chunked")!=null;
		this.out = new PrintStream(outputStream);
		this.responseCode = parser.getStatusCode();
		this.params = parser.getParams();

	}

	/**
	 * major method of this class, according to the response code, generates a
	 * request and prints it
	 * 
	 * @throws FileNotFoundException
	 */
	public void echoResponse() throws FileNotFoundException {
		if (chunked) {
			printChunkedHeaders();
			printResponse(page, CHUNK_SIZE);
		} else {
			printHeaders(page);
			printResponse(page);
		}

	}

	/*
	 * Prints a chunked response for GET request
	 * 
	 * @param page
	 * 
	 * @param chunkSize
	 */
	private void printResponse(File page, int chunkSize) {
		InputStream file = null;
		int length;

		try {
			file = new FileInputStream(page);
			byte[] chunk = new byte[chunkSize];
			while ((length = file.read(chunk)) > 0) {
				out.println(Integer.toHexString(length));
				out.println(new String(chunk, "utf-8"));
				out.flush();
			}

		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				if (file != null)
					file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * prints the response for a GET requets (non-chunked)
	 */
	private void printResponse(File page) throws FileNotFoundException {
		InputStream file = null;
		try {
			file = new FileInputStream(page);
			byte[] buffer = new byte[1000];
			while (file.available() > 0)
				out.write(buffer, 0, file.read(buffer));
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				if (file != null)
					file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// Prints GET response
	private void printHeaders(File page) {
		StringBuilder headers = new StringBuilder();
		headers.append("HTTP/1.1 " + this.responseCode + getResponseStatus()
				+ CRLF);
		// headers.append("Type:" + request.getContentType() + CRLF);
		headers.append("charset:utf-8" + CRLF);
		headers.append("content-length: " + page.length() + CRLF);
		headers.append(CRLF);
		System.out.println(headers.toString());
		out.print(headers.toString());
	}

	// Prints GET Chunked response
	private void printChunkedHeaders() {

		StringBuilder headers = new StringBuilder();
		headers.append("HTTP/1.1 " + this.responseCode + getResponseStatus()
				+ CRLF);
		// headers.append("Type:" + request.getContentType() + CRLF);
		headers.append("charset:utf-8" + CRLF);
		headers.append("Transfer-Encoding: chunked " + CRLF);
		headers.append(CRLF);
		System.out.print(headers.toString());
		out.print(headers.toString());

	}

	/*
	 * prits the POST response, actually generates an HTML page in the memory
	 * and prints it to the socket
	 */
	private void printPostResponse() {
		StringBuilder result = new StringBuilder();
		result.append("<html>");
		result.append("<table border=\"0\">");

		Iterator<Entry<String, String>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
					.next();
			result.append("<tr><td>" + pairs.getKey() + "</td>");
			result.append("<td>" + pairs.getValue() + "</td></tr>");
			it.remove(); // a
		}
		result.append("</table >");
		result.append("</html>");
		StringBuilder headers = new StringBuilder();
		headers.append("HTTP/1.1 " + this.responseCode + getResponseStatus()
				+ CRLF);
		// headers.append("Type:" + request.getContentType() + CRLF);
		headers.append("charset:utf-8" + CRLF);
		headers.append("content-length: " + result.length() + CRLF);
		headers.append(CRLF);
		System.out.println(headers.toString());
		out.print(headers.toString());
		out.print(result.toString());
		out.flush();
	}

	/*
	 * prits the TRACE response
	 */
	private void printTraceResponse() {
		StringBuilder result = new StringBuilder();
		Iterator<Entry<String, String>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
					.next();
			result.append(pairs.getKey() + "=" + pairs.getValue() + CRLF);
			it.remove(); // a
		}
		StringBuilder headers = new StringBuilder();
		headers.append("HTTP/1.1 " + this.responseCode + getResponseStatus()
				+ CRLF);
		// headers.append("Type:" + request.getContentType() + CRLF);
		headers.append("charset:utf-8" + CRLF);
		headers.append("content-length: " + result.length() + CRLF);
		headers.append(CRLF);
		System.out.println(headers.toString());
		out.print(headers.toString());
		out.print(result.toString());
		out.flush();
	}

	private String getResponseStatus() {
		if (this.responseCode == 200)
			return " OK";
		if (this.responseCode == 400)
			return " Bad Request";
		if (this.responseCode == 401)
			return " Unauthorized";
		if (this.responseCode == 404)
			return " Not Found";
		if (this.responseCode == 501)
			return " Not Implemented";
		if (this.responseCode == 500)
			return " Internal Server Error";
		return "";

	}
}
