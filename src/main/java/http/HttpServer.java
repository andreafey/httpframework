package http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.ServerSocket;

public class HttpServer {
	
	private int port;
	
	public HttpServer(int port) {
		this.port = port;
	}
	// TODO
	public void run() {
//		ServerSocket server = null;
		try {
			ServerSocket server = new ServerSocket(port);
			while (true) {
				// blocks until request received
				Socket connection = server.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				OutputStream out = new BufferedOutputStream(connection.getOutputStream());
				PrintStream pout = new PrintStream(out);
				
				String s = in.readLine();
				String req = s;
				if (req == null) 
					continue;
				// TODO see why this is necessary
				// read the rest of data from the input stream, and throw it out
				while (s != null && s.length() > 0) {
					s = in.readLine();
				}
				// TODO print http headers
				pout.println("hello");
				out.flush();
				connection.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	// TODO
	public void stop() {}
	
//	public static HttpServer createServer(int port) {
//		
//	}

	public static void main(String[] args) {
		HttpServer server = new HttpServer(1234);
		server.run();
	}

}
