# http.Server

This library defines a very simple http server.  The simplest way to use it is:

```
import http.Server;

int port = 1234;

Server s = new Server(port);
s.run()
```

This will cause the server to listen for http GET requests on port 1234; it will look for
any requested files relative to its current directory.  So, for example, a request for the
URL `http://localhost:1234/foo/bar/bat.html` would return contents of the file `./foo/bar/bat.html`,
if it exists, or a 404 error if it does not.  Note that this is a HUGE security vulnerability,
so don't run this program on a public server!

You can also define your own custom controller class with methods to be invoked for certain
specified URLs.  Do do that, create a class with public static methods annotated with the `@RequestMethod`
annotation.  Each of these methods should take a PrintStream as its first argument, and
may take any number of addtional String arguments.

When the server receives a request for a URL which matches a String given in one of the controller's
annotated @RequestMethods, it calls that method with a PrintStream and any String arguments
resulting from any '/'-separated components in the part of the URL after the matching path.

For example:

```
import http.Server;
import http.RequestMethod;
import java.io.PrintStream;

public class MyController {

  @RequestMethod("one")
  public static void one(PrintStream pout) {
    Server.sendHeader(pout, 200, "OK");
    pout.println("Hi there, this is the one.");
  }
  
  @RequestMethod("two")
  public static void two(PrintStream pout, String thing) {
    Server.sendHeader(pout, 200, "OK");
    pout.printf("Hi there, this is two, and thing = \"%s\".", thing);
  }
}
```

Then register an instance of your controller with the server before invoking `run()`:

```
import http.Server;

int port = 1234;

Server s = new Server(port);
s.registerController(new MyController());
s.run()
```

Note that request paramters (e.g. '?name=value&...') are not handled.
