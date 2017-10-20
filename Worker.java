/**
 * Worker class
 * 
 */

import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.net.*;
import java.text.*;
import java.util.*;

public class Worker extends Thread {
	private Socket connection;			//holds the socket that has a connection to the client
	
	/**
	 * Default constructor to initialize the Worker threads
	 * 
	 * @param socket	The socket which has accepted the client
	 * 
	 */
	public Worker(Socket socket) {
		this.connection = socket;		//connects the Worker thread's socket to the client
	}

	/**
	 * Parses and qualifies the client's request and responds with either '200 OK', '400 Bad Request' or '404 File Not Found'
	 * 
	 */
	public void run() {
		String request;					//holds the original request
		String[] temp;					//holds different elements of the request

		Date now = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");

		String date = "Date: " + fmt.format(now);
		String server = "Server: CPSC 441 Server";
		String connect = "Connection: close";
		String lastMod = "";
		String contentLength = "";
		String contentType = "";
		String header = "";

		try {
			OutputStream outputStream = connection.getOutputStream();				//output stream to the client
			Scanner inputStream = new Scanner(new InputStreamReader(connection.getInputStream()));		//input stream from the client
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();			//packages header and body into a byte array
			
			request = inputStream.nextLine();				//find the client's request
			temp = request.split(" ");						//split using spaces

			byte[] headerBytes;

			//if the format of request is incorrect, send a Bad Request message
			if (!(temp.length == 3) || !(temp[0].equals("GET")) || !(temp[2].equals("HTTP/1.0") || temp[2].equals("HTTP/1.1"))) {
				header = "HTTP/1.1 400 Bad Request\r\n"
							+ date + "\r\n"
							+ server + "\r\n"
							+ connect + "\r\n" + "\r\n";

				//write header to byte array
				headerBytes = header.getBytes("US-ASCII");
				byteStream.write(headerBytes);
			}
			
			else {
				File file = new File(System.getProperty("user.dir") + temp[1]);

				//else if the request is semantically correct but there is no file as described in the request, send a File Not Found message
				if (!file.exists()) {
					header = "HTTP/1.1 404 Not Found\r\n"
								+ date + "\r\n"
								+ server + "\r\n"
								+ connect + "\r\n" + "\r\n";

					//write header to byte array
					headerBytes = header.getBytes("US-ASCII");
					byteStream.write(headerBytes);
				}
				
				//else everything is gucci
				else {
					try {
						lastMod = "Last-Modified: " + fmt.format(file.lastModified());				//find last modified time of file
						Integer contentSize = new Double(file.length()).intValue();					//find size of file in integers,
						contentLength = "Content-Length: " + contentSize.toString();				//	then convert it into a string
						contentType = "Content-Type: " + Files.probeContentType(file.toPath());		//find the type of the content
					} catch (Exception e) {
						System.out.println("Terminal exception caught.");
						System.exit(1);
					}

					header = "HTTP/1.1 200 OK\r\n" + date + "\r\n" + server + "\r\n"
								+ lastMod + "\r\n" + contentLength + "\r\n"
								+ contentType + "\r\n" + connect + "\r\n" + "\r\n";

					//write header to byte array
					headerBytes = header.getBytes("US-ASCII");
					byteStream.write(headerBytes);

					//write file's data (the body of the message) to byte array
					byte[] bodyBytes = new byte[(int) file.length()];
					try {
						FileInputStream fis = new FileInputStream(file);
						fis.read(bodyBytes);
					} catch (Exception e) {
						System.out.println("Error in reading file.");
						System.exit(1);
					}
					byteStream.write(bodyBytes);
				}
			}

			//gather the byte stream of the HTTP response in a byte array
			byte[] object = byteStream.toByteArray();
			//send it to the client
			outputStream.write(object);
			outputStream.flush();

			inputStream.close();
			outputStream.close();
		} catch (Exception e) {
			System.out.println("Terminal exception caught.");
			System.exit(1);
		}
	}
}
