import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class HttpResponder {
	private int responseCode;
	private PrintWriter out;
	private final String CRLF = "\r\n";
	private OutputStream outputStream;
	private File page;
	private  final int CHUNK_SIZE = 1024;
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
	public HttpResponder(File page, boolean chunked,Hashtable<String, String> params,OutputStream outputStream)
			throws IOException {
		this.page = page;
		this.chunked = chunked;
		this.outputStream = outputStream;
		this.out = new PrintWriter(outputStream);
		this.responseCode = 200;
		this.params = params;

	}

	/**
	 * major method of this class, according to the response code, generates a
	 * request and prints it
	 */
	public void echoResponse() {
		if (chunked){
			printChunkedHeaders();
			printResponse(page, CHUNK_SIZE);
		}else{
			printHeaders(page);
			printResponse(page);
		}
		
		File page;
		

	}

	/*
	 * Prints a chunked response for GET request
	 * 
	 * @param page
	 * 
	 * @param chunkSize
	 */
	private void printResponse(File page, int chunkSize) {
		String contentType = "text/html";//TODO:Changed;
		FileInputStream fis = null;

		// if the type is html page
		if (contentType.equals("text/html")
				|| contentType.equals("application/octet-stream")) {

			byte[] chunk = new byte[chunkSize];

			try {
				outputStream.close();
				fis = new FileInputStream(page);
				int length;
				// read until the end of the stream.
				while ((length = fis.read(chunk)) > 0) {
					out.println(Integer.toHexString(length));
					out.println(new String(chunk, "utf-8"));
					out.flush();
					chunk = new byte[chunkSize];
				}
				out.println(CRLF);
				out.flush();
				fis.close();
			} catch (FileNotFoundException e) {

			} catch (IOException e) {

			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Handle image IO
		} else if (contentType.equals("image/jpeg")
				|| contentType.equals("image/png")
				|| contentType.equals("image/gif")) {
			if (contentType.contains("jpeg") || contentType.contains("jpg")) {
				contentType = "jpg";
			} else {
				contentType = contentType.substring(6);
				System.out.println(contentType);

			}
			try {

				BufferedImage originalImage = ImageIO.read(page);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(originalImage, contentType, baos);
				baos.flush();
				byte[] imageInByte = baos.toByteArray();
				out.print(Integer.toHexString(imageInByte.length));
				outputStream.write(imageInByte);
				outputStream.close();
				out.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				out.close();

			}

			// Handle ICO IO
		} else if (contentType.equals("image/x-icon")) {
			try {
				fis = new FileInputStream(page);

				byte[] bFile = new byte[(int) page.length()];
				// read until the end of the stream.
				while (fis.available() != 0) {
					fis.read(bFile, 0, bFile.length);
					out.print(Integer.toHexString(bFile.length));
					outputStream.write(bFile);
				}
				outputStream.flush();
				outputStream.close();
				out.flush();

			} catch (FileNotFoundException e) {
			} catch (IOException e) {

			} finally {
				try {
					fis.close();
					out.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

	/*
	 * prints the response for a GET requets (non-chunked)
	 */
	private void printResponse(File page) {
		String contentType = "text/html";//TODO:Changed

		FileInputStream fis = null;
		Scanner fileStream = null;
		if (contentType.equals("text/html")
				|| contentType.equals("application/octet-stream")) {
			try {
				fis = new FileInputStream(page);
				fileStream = new Scanner(fis);
				// read until the end of the stream.
				while (fileStream.hasNext()) {
					out.print(fileStream.nextLine());
				}
				out.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					fileStream.close();
					fis.close();
				} catch (IOException e) {
				}

			}
			
			//
		} else if (contentType.equals("image/jpeg")
				|| contentType.equals("image/png")
				|| contentType.equals("image/gif")) {
			if (contentType.contains("jpeg") || contentType.contains("jpg")) {
				contentType = "jpg";
			} else {
				contentType = contentType.substring(6);
				System.out.println(contentType);

			}
			try {

				BufferedImage originalImage = ImageIO.read(page);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(originalImage, contentType, baos);
				baos.flush();
				byte[] imageInByte = baos.toByteArray();
				outputStream.write(imageInByte);
				outputStream.close();
				out.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				out.close();

			}
		} else if (contentType.equals("image/x-icon")) {
			try {

				fis = new FileInputStream(page);
				byte[] bFile = new byte[(int) page.length()];
				// read until the end of the stream.
				while (fis.available() != 0) {
					fis.read(bFile, 0, bFile.length);
					outputStream.write(bFile);
				}
				outputStream.flush();
				outputStream.close();
				out.flush();

			} catch (FileNotFoundException e) {
				// do something
			} catch (IOException e) {
				// do something

			} finally {
				try {
					fis.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
	}

	// Prints GET response
	private void printHeaders(File page) {
		StringBuilder headers = new StringBuilder();
		headers.append("HTTP/1.1 " + this.responseCode + getResponseStatus()
				+ CRLF);
		//headers.append("Type:" + request.getContentType() + CRLF);
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
		//headers.append("Type:" + request.getContentType() + CRLF);
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
		//headers.append("Type:" + request.getContentType() + CRLF);
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
		//headers.append("Type:" + request.getContentType() + CRLF);
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
