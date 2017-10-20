import java.io.*;
import java.nio.file.Files;
import java.net.*;
import java.text.*;
import java.util.*;

public class Worker extends Thread {
	private Socket connection;
	
	public Worker(Socket socket) {
		this.connection = socket;
	}

	public void run() {
		String request;
		String[] temp;

		Date now = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");

		String date = "Date: " + fmt.format(now);
		String server = "Server: CPSC 441 Server";
		String connect = "Connection: close";
		String lastMod = "";
		String contentLength = "";
		String contentType = "";
		String response = "";

		try {
			PrintWriter outputStream = new PrintWriter(new DataOutputStream(connection.getOutputStream()));
			Scanner inputStream = new Scanner(new InputStreamReader(connection.getInputStream()));
			request = inputStream.nextLine();
			temp = request.split(" ");

			if (!(temp.length == 3) || !(temp[0].equals("GET")) || !(temp[2].equals("HTTP/1.0") || temp[2].equals("HTTP/1.1"))) {
				response = "HTTP/1.1 400 Bad Request\r\n"
							+ date + "\r\n"
							+ server + "\r\n"
							+ connect + "\r\n" + "\r\n";
			} else {

				File file = new File(System.getProperty("user.dir") + temp[1]);

				if (!file.exists()) {
					response = "HTTP/1.1 404 Not Found\r\n"
								+ date + "\r\n"
								+ server + "\r\n"
								+ connect + "\r\n" + "\r\n";
				} else {
					try {
						lastMod = "Last-Modified: " + fmt.format(file.lastModified());
						Integer contentSize = new Double(file.length()).intValue();
						contentLength = "Content-Length: " + contentSize.toString();
						contentType = "Content-Type: " + Files.probeContentType(file.toPath());
					} catch (Exception e) {
						//ignore
					}

					response = "HTTP/1.1 200 OK\r\n" + date + "\r\n" + server + "\r\n"
								+ lastMod + "\r\n" + contentLength + "\r\n"
								+ contentType + "\r\n" + connect + "\r\n" + "\r\n";
				}
			}

			System.out.printf(response);
			outputStream.println(response);
			outputStream.flush();

			inputStream.close();
			outputStream.close();
		} catch (Exception e) {
			//ignore
		}
	}
}