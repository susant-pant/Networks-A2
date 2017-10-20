/**
 * WebServer Class
 * 
 */

import java.util.*;
import java.io.*;
import java.net.*;

public class WebServer extends Thread {
	public boolean shutdown;		//boolean that tells threads if server is shutdown
	public int serverPort;			//global access to the server's port

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
			ServerSocket serverSocket = new ServerSocket(serverPort);	//opens a new server socket that listens at serverPort
			serverSocket.setSoTimeout(1000);			//timeout every second to check whether server has shutdown
			while (!shutdown) {							//if not, wait for a client
				try {
					Socket socket = serverSocket.accept();
					//when the socket accepts a client, send it to a Worker thread so main thread can continue listening
					(new Worker(socket)).start();
				} catch (SocketTimeoutException e) {
					//do nothing, this is naturally caught every second
				}
			}
			serverSocket.close();
		} catch (Exception f) {
			System.out.println("Terminal exception caught.");
			System.exit(1);
		}
	}

    /**
     * Signals the server to shutdown.
	 *
     */
	public void shutdown() {
		shutdown = true;		//flags the server's threads to terminate
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
