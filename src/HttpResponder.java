import java.io.*;
import java.util.*;
import java.util.Map.*;

public class HttpResponder {
    private int responseCode;
    private PrintStream out;
    private final String CRLF = "\r\n";
    private File page;
    private final int CHUNK_SIZE = 1024;
    private boolean chunked;
    private Hashtable<String, String> params;
    private String path;
    HttpParser request;

    /**
     * This class is instanced to an object that accepts a parsed HTTP request
     * and the client output stream and generates a response accordingly
     *
     * @param outputStream the client socoket's output stream
     * @throws IOException
     */
    public HttpResponder(String path, int responseCode, OutputStream outputStream, HttpParser request)
            throws IOException {
        this.request = request;
        this.path = path;
        this.page = new File(path);
        this.chunked = false;
        this.out = new PrintStream(outputStream);
        this.responseCode = responseCode;

    }

    /**
     * major method of this class, according to the response code, generates a
     * request and prints it
     *
     * @throws FileNotFoundException
     */
    public void echoResponse() throws FileNotFoundException {
        String method = request.getMethod();
        if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("POST")) {
            if (chunked) {
                printChunkedHeaders();
                printResponse(page, CHUNK_SIZE);
            } else {

                printHeaders(page);
                printResponse(page);

            }
        } else if (method.equalsIgnoreCase("TRACE")) {
            printTraceResponse();
        } else if (method.equalsIgnoreCase("HEAD")) {
            printHeaders(page);
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
        headers.append("HTTP/1.1 " + responseCode + getResponseStatus()
                + CRLF);
        // headers.append("Type:" + request.getContentType() + CRLF);
        headers.append("charset:utf-8" + CRLF);
        headers.append("content-length: " + page.length() + CRLF);
        headers.append("Content-Type:" + getContentType() + CRLF);

        headers.append("Host: " +ServerRun.SERVER_NAME+":"+ServerRun.port+ CRLF);
        headers.append(CRLF);
        System.out.println(headers.toString());
        out.print(headers.toString());
    }

    // Prints GET Chunked response
    private void printChunkedHeaders() {

        StringBuilder headers = new StringBuilder();
        headers.append("HTTP/1.1 " + responseCode + getResponseStatus()
                + CRLF);
        headers.append("Type:" + getContentType() + CRLF);
        headers.append("charset:utf-8" + CRLF);
        headers.append("Transfer-Encoding: chunked " + CRLF);
        headers.append("Content-Type:" + getContentType() + CRLF);
        headers.append("Host: " +ServerRun.SERVER_NAME+":"+ServerRun.port+ CRLF);
        headers.append(CRLF);
        System.out.print(headers.toString());
        out.print(headers.toString());

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
        headers.append("Content-Type:" + getContentType() + CRLF);
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
}
