create a controller with methods annotated with RequestMethod("path/to/resource")
 -- method must have as its first argument a PrintStream and some fixed number of subsequent String arguments
 -- requests to the path will have text after the next slash parsed into subsequent arguments

 start server with desired port number
 create an instance of the controller object and register the controller on the server
 call server.run()

