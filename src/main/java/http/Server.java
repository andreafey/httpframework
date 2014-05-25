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
 

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import example.ExampleController;

public class Server {

    HashMap<String,ControllerMethod> controllerMethods = new HashMap<>();

    private static class ControllerMethod {
    	private Object controller;
    	private Method method;
    	public ControllerMethod(Object controller, Method method) {
    		this.controller = controller;
    		this.method = method;
    	}
    	public void invoke(Object... args) {
    		try {
				method.invoke(controller, args);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		public Method method() {
			return method;
		}
    }
    private static class ControllerMethodWithArgs {
        private ControllerMethod controllerMethod;
        private String[] args;
        public ControllerMethod controllerMethod() { return controllerMethod; }
        public String[] args() { return args; }
        public ControllerMethodWithArgs(ControllerMethod controllerMethod, String[] args) {
            this.controllerMethod = controllerMethod;
            this.args = args;
        }
    }

    private ControllerMethodWithArgs controllerMethodWithArgs(String path) {
        int i = path.length();
        while (i >= 0) {
            String a = path.substring(0,i);
            String b = path.substring(i);
            if (controllerMethods.containsKey(a)) {
                if (b.length() > 0) {
                    return new ControllerMethodWithArgs(controllerMethods.get(a), b.substring(1).split("/"));
                } else {
                    return new ControllerMethodWithArgs(controllerMethods.get(a) ,new String[] {});
                }
            }
            i = path.lastIndexOf('/', i-1);
        }
        return null;
    }

	private int port;
	
	public Server(int port) {
		this.port = port;
	}

	public void registerController(Object controller) {
        for (Method method : controller.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(RequestMethod.class)) {
                RequestMethod rm = (RequestMethod)(method.getAnnotation(RequestMethod.class));
                controllerMethods.put(rm.value(), new ControllerMethod(controller, method));
            }
        }
		
	}
 	public void run() {
 		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
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

                ControllerMethodWithArgs sfwa = controllerMethodWithArgs(reqPath);
                if (sfwa != null) {
                    try {
                        if (sfwa.controllerMethod().method().getParameterTypes().length == sfwa.args().length+1) {
                            Object[] oargs = new Object[sfwa.args().length+1];
                            oargs[0] = pout;
                            for (int i = 0; i < sfwa.args().length; ++i) {
                                oargs[i+1] = sfwa.args()[i];
                            }
                            sfwa.controllerMethod().invoke(oargs);
                        } else {
                            sendHeader(pout, 500, "Internal Server Error");
                            pout.println("Invalid number of arguments");
                        }
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
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public static void sendHeader(PrintStream pout, int code, String message) {
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
	
	public static void main(String[] args) {
		Server server = new Server(1234);
		server.registerController(new ExampleController());
		server.run();
	}

}
