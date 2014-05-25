package example;

import http.RequestMethod;
import http.Server;

import java.io.PrintStream;

public class ExampleController {
	
    @RequestMethod("hello.html")
	public static void helloworld(PrintStream pout) {
        Server.sendHeader(pout, 200, "OK");
        pout.println("hello world!");
    }


    @RequestMethod("goodbye.html")
	public static void goodbyeworld(PrintStream pout) {
        Server.sendHeader(pout, 200, "OK");
        pout.println("goodbye world!");
    }

    @RequestMethod("sayhi")
	public static void goodbyeworld(PrintStream pout, String who, String what) {
        Server.sendHeader(pout, 200, "OK");
        pout.printf("hi %s, you are %s\n", who, what);
    }


}
