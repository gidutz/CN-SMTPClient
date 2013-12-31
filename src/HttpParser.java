import java.io.*;
import java.util.*;
import java.net.URLDecoder;

/**
 * This Class is used to parse HTTP Request The constructor accepst a socket and
 * parses the request
 * 
 * @author gidutz
 * 
 */
public class HttpParser {
	private static final ArrayList<String> supportedMethods = new ArrayList<String>(
			Arrays.asList(new String[] { "GET", "POST", "OPTIONS", "HEAD",
					"TRACE" }));
	private String method, path;
	private Hashtable<String, String> headers, params;
	private int[] versionCode;
	private BufferedReader reader;
	private int statusCode;
	private InputStream socketInputStream;

	/**
	 * Constructs a new HTTP parser based on input from the client Socket
	 * 
	 * @param socketInputStream
	 * @throws IOException
	 */
	public HttpParser(InputStream socketInputStream) throws IOException {

		this.socketInputStream = socketInputStream;
		this.reader = new BufferedReader(new InputStreamReader(
				socketInputStream));
		this.method = null;
		this.path = null;
		this.headers = new Hashtable<String, String>();
		this.params = new Hashtable<String, String>();
		this.versionCode = new int[2];

		statusCode = parseRequest();
	}

	private int parseRequest() throws IOException {
		String firstLine, prms[], cmd[], temp[];
		int responseCode;

		// We're optimistic, assuming everything's gonna be OK
		responseCode = 200;

		/* Start messing with the request first line */
		firstLine = reader.readLine();
		if (firstLine == null || firstLine.length() == 0)
			return 0;
		if (Character.isWhitespace(firstLine.charAt(0))) {
			// starting whitespace, return bad request
			return 400;
		}

		// method, HTTP vesion, path
		cmd = firstLine.split("\\s");
		if (cmd.length != 3) {
			return 400;
		}

		// Makes sure the version code looks like HTTP/1.*
		if (cmd[2].indexOf("HTTP/") == 0
				&& cmd[2].indexOf('.') > "HTTP/.".length() - 1) {
			temp = cmd[2].substring(5).split("\\.");

			// if the http version isn't num
			try {
				versionCode[0] = Integer.parseInt(temp[0]);
				versionCode[1] = Integer.parseInt(temp[1]);
			} catch (NumberFormatException nfe) {
				responseCode = 400;
			}
		} else {
			responseCode = 400;

		}

		/* Handle supported methods */
		if (supportedMethods.contains(cmd[0])) {
			method = cmd[0];

			// if there weren't any parameters
			int queryIndex = cmd[1].indexOf('?');
			if (queryIndex < 0)
				path = cmd[1];
			else {

				// Sorry we had to use URLDecoder to get over all the strange
				// signs like &gt
				path = URLDecoder.decode(cmd[1].substring(0, queryIndex),
						"UTF-8");
				prms = cmd[1].substring(queryIndex + 1).split("&");

				params = new Hashtable<String, String>();
				for (int i = 0; i < prms.length; i++) {
					temp = prms[i].split("=");
					if (temp.length == 2) {
						params.put(URLDecoder.decode(temp[0], "UTF-8"),
								URLDecoder.decode(temp[1], "UTF-8"));
					} else if (temp.length == 1
							&& prms[i].indexOf('=') == prms[i].length() - 1) {
						// handle empty string separatedly
						params.put(URLDecoder.decode(temp[0], "UTF-8"), "");
					}
				}
			}
			parseHeaders();
			if (headers == null)
				responseCode = 400;

			/* builds the url path */
			if (method.equals("GET") || method.equals("POST")) {
			
				if (path.startsWith("/")) {
					path = path.substring(1);

				}
				if (method.equals("POST")) {
					parsePostVars();
				}
				responseCode = redirect();
				// parses the additional parameters from the request body
			
			}
			// Returns the unimplemented methods
		} else if (versionCode[0] == 1 && versionCode[1] >= 1) {
			if (cmd[0].equals("PUT") || cmd[0].equals("DELETE")
					|| cmd[0].equals("CONNECT")) {
				responseCode = 501;
			}
		} else {
			responseCode = 400; // no such method is bad request!
		}

		// http version is 1.1 there is no host header
		if (versionCode[0] == 1 && versionCode[1] >= 1
				&& getHeader("Host") == null) {
			responseCode = 400;
		}

		return responseCode;// finally.....
	}

	/**
	 * parses the Headers
	 * 
	 * @throws IOException
	 */
	private void parseHeaders() throws IOException {
		String line;
		int index;
		line = reader.readLine();

		while (!line.equals("")) {

			index = line.indexOf(':');
			if (index < 0) {
				headers = null;
				break;
			} else {
				headers.put(line.substring(0, index).toLowerCase(), line
						.substring(index + 1).trim());
			}
			line = reader.readLine();
		}
	}

	private void parsePostVars() throws IOException {
		String line, prms[], temp[];
		int length = Integer.parseInt(getHeader("content-length"));
		char[] data = new char[length];
		reader.read(data);
		line = new String(data);
		prms = line.split("&");
		for (int i = 0; i < prms.length; i++) {
			temp = prms[i].split("=");
			if (temp.length == 2) {
				params.put(URLDecoder.decode(temp[0], "UTF-8"),
						URLDecoder.decode(temp[1], "UTF-8"));
			} else if (temp.length == 1
					&& prms[i].indexOf('=') == prms[i].length() - 1) {
				// handle empty string separatedly
				params.put(URLDecoder.decode(temp[0], "UTF-8"), "");
			}
		}

	}

	public String getMethod() {
		return method;
	}

	/**
	 * Returns the value of the Header
	 * 
	 * @param key
	 *            header name
	 * @return the value of the header
	 */
	public String getHeader(String key) {
		if (headers != null)
			return (String) headers.get(key.toLowerCase());
		else
			return null;
	}

	/**
	 * retuns a hash-table of all the headers
	 * 
	 * @return
	 */
	public Hashtable<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Returns the request url
	 * 
	 * @return
	 */
	public String getRequestURL() {
		return path;
	}

	/**
	 * Returns the value of a parameter
	 * 
	 * @param key
	 *            the parameter name
	 * @return
	 */
	public String getParam(String key) {
		return (String) params.get(key);
	}

	private int redirect() {

		// change absolute path to relative path
		if (path.startsWith("http://" + getHeader("host"))) {
			path = path.substring(("http://" + getHeader("host")).length() + 1);
		}

		path = ServerRun.root + path;
		File page = new File(path);
		if (page.isDirectory()) {
			path = path + ServerRun.defaultPage;
			page = new File(path);
		}
		
		if (getHeader("Cookie")==null){
			if (path.equals(ServerRun.root+"main.html")&&getParam("username")!=null){
				//notify the responder to set cookie;
				path = ServerRun.root + ServerRun.mainPage;
				page = new File(path);
				return 302;
			}else{
				path = ServerRun.root + ServerRun.defaultPage;
				page = new File(path);
				return 302;

			}
		}
		if (page.getAbsoluteFile().isAbsolute()) {
			// TODO:check if the server root path is the start of

			return 401;
		} else if (!page.exists()) {
			path = path + ServerRun.$404page;

			return 404;
		}

		return statusCode;
	}

	/**
	 * getParams returns a HashTable to the request parameters
	 * 
	 * @return
	 */
	public Hashtable<String, String> getParams() {
		return params;
	}

	/**
	 * getVersion returns the version of the HTTP request
	 * 
	 * @return
	 */
	public String getVersion() {
		return versionCode[0] + "." + versionCode[1];
	}

	/**
	 * getContentType returns the proper MIME content type according to the
	 * requested file's extension.
	 * 
	 * @param fileRequested
	 *            File requested by client
	 */
	public String getContentType() {
		if (path == null) {
			return null;
		}
		if (path.endsWith(".htm") || path.endsWith(".html")) {
			return "text/html";
		} else if (path.endsWith(".gif")) {
			return "image/gif";
		} else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (path.endsWith(".png")) {
			return "image/png";
		} else if (path.endsWith(".ico")) {
			return "image/x-icon";
		} else {
			return "application/octet-stream";
		}
	}

	public int getStatusCode() {
		return statusCode;
	}

}
