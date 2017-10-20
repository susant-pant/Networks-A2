

/**
 * WebServer Class
 * 
 */

import java.util.*;
import java.io.*;
import java.net.*;


public class WebServer extends Thread {
	public String serverName;
	public boolean shutdown;
	public int serverPort;

    /**
     * Default constructor to initialize the web server
     * 
     * @param port 	The server port at which the web server listens > 1024
     * 
     */
	public WebServer(int port) {
		serverPort = port;
		shutdown = false;
	}

    /**
     * The main loop of the web server
     *   Opens a server socket at the specified server port
	 *   Remains in listening mode until shutdown signal
	 * 
     */
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(serverPort);
			serverSocket.setSoTimeout(1000);
			while (!shutdown) {
				try {
					Socket socket = serverSocket.accept();

					(new Worker(socket)).start();
				} catch (SocketTimeoutException e) {
					//ignore
				}
			}
			serverSocket.close();
		} catch (Exception f) {
			//ignore
		}
	}

    /**
     * Signals the server to shutdown.
	 *
     */
	public void shutdown() {
		shutdown = true;
	}

	/**
	 * A simple driver.
	 */
	public static void main(String[] args) {
		int serverPort = 2225;

		// parse command line args
		if (args.length == 1) {
			serverPort = Integer.parseInt(args[0]);
		}
		
		if (args.length >= 2) {
			System.out.println("wrong number of arguments");
			System.out.println("usage: WebServer <port>");
			System.exit(0);
		}
		
		System.out.println("starting the server on port " + serverPort);
		
		WebServer server = new WebServer(serverPort);
		
		server.start();
		System.out.println("server started. Type \"quit\" to stop");
		System.out.println(".....................................");

		Scanner keyboard = new Scanner(System.in);
		while ( !keyboard.next().equals("quit") );
		
		System.out.println();
		System.out.println("shutting down the server...");
		server.shutdown();
		System.out.println("server stopped");
	}
}
