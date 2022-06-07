package pt.ulisboa.tecnico.classes.namingserver;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.regex.Pattern;

public class NamingServer {

  public static void main(String[] args) {
    boolean debugFlag = false;

    System.out.println(NamingServer.class.getSimpleName());

    Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    if(args.length == 3 && args[2].equals("-debug")){
      debugFlag = true;
      System.out.println("Debug mode activated.");
    }
    if((args.length == 3 && !args[2].equals("-debug")) || (args.length < 2 || args.length > 3 || args[1].length() != 4 ||
            !pattern.matcher(args[1]).matches())) {
      System.out.println("Wrong arguments given.\nUsage: $ nameserver address port -debugFlag");
      System.exit(1);
    }

    String host = args[0];
    int port = Integer.parseInt(args[1]);

    final BindableService namingServerImpl = new NamingServerServiceImpl(debugFlag);

    // Create a new server to listen on port
    ServerBuilder serverBuilder = ServerBuilder.forPort(port);
    serverBuilder.addService(namingServerImpl);
    Server server = serverBuilder.build();

    // Start the server
    try {
      server.start();

      // Server threads are running in the background.
      System.out.println("Server started");

      // Do not exit the main thread. Wait until server is terminated.
      server.awaitTermination();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}
