package http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Date;
import java.util.HashMap;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public class HttpServer {

    HashMap<String,Method> specialFunctions = new HashMap<>();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface RequestMethod {
        String path();
    }

	private int port;
	
	public HttpServer(int port) {
		this.port = port;
        Class<HttpServer> obj = HttpServer.class;
        for (Method method : obj.getDeclaredMethods()) {
            if (method.isAnnotationPresent(RequestMethod.class)) {
                RequestMethod rm = (RequestMethod)(method.getAnnotation(RequestMethod.class));
                specialFunctions.put(rm.path(), method);
            }
        }
	}
	// TODO
	public void run() {
//		ServerSocket server = null;
		try {
			ServerSocket server = new ServerSocket(port);
			while (true) {
				System.out.println("listening for connection");
				// blocks until request received
				Socket connection = server.accept();
				System.out.println("connected");
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				OutputStream out = new BufferedOutputStream(connection.getOutputStream());
				// this is only for character streams
				// TODO add support for other file types
				PrintStream pout = new PrintStream(out);
				
				String s = in.readLine();
				String req = s;
				if (req == null) 
					continue;
                String reqPath = req.substring(5, req.indexOf(' ', 5));
                if (reqPath.length() == 0) { reqPath = "index.html"; }
				// TODO see why this is necessary
				// read the rest of data from the input stream, and throw it out
				while (s != null && s.length() > 0) {
					s = in.readLine();
				}

                if (specialFunctions.containsKey(reqPath)) {
                    try {
                        specialFunctions.get(reqPath).invoke(null, pout);
                    } catch (Exception e) {
                        System.out.println("ERROR\n");
                        System.out.println(e);
                    }
                } else {
                    File page = new File(reqPath);
                    if (!page.isFile()) {
                        System.out.println("page does not exist");
                        sendHeader(pout, 404, "File Not Found");
                        pout.println("File Not Found");
                    }
                    else {
                        System.out.println("page exists");
                        sendHeader(pout, 200, "OK");
                        InputStream fin = new FileInputStream(page);
                        BufferedReader fread = new BufferedReader(new InputStreamReader(fin));
                        String line = fread.readLine();
                        while (line != null) {
                            pout.println(line);
                            line = fread.readLine();
                        }
                        fread.close();
                    }
                }
				out.flush();
				connection.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

    @RequestMethod(path = "hello.html")
	private static void helloworld(PrintStream pout) {
        sendHeader(pout, 200, "OK");
        pout.println("hello world!");
    }


    @RequestMethod(path = "goodbye.html")
	private static void goodbyeworld(PrintStream pout) {
        sendHeader(pout, 200, "OK");
        pout.println("goodbye world!");
    }


	private static void sendHeader(PrintStream pout, int code, String message) {
		pout.printf("HTTP/1.0 %1d %s\n", code, message);
		if (code == 200) {
			pout.println("Content-Type: text/html");
			pout.println("Date: " + new Date());
			pout.println("Server: HttpServer");
		}
		pout.println();
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
