package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.*;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class ClassServer {

  public static String hostPort;

  public static String qualifier;

  public static Timer timer;

  public static TimerTask timerTask;

  public static void main(String[] args) {
    boolean debugFlag = false;

    ClassServerNamingServerFrontend namingServerFrontend = new ClassServerNamingServerFrontend();

    System.out.println(ClassServer.class.getSimpleName());

    Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    if(args.length == 4 && args[3].equals("-debug")){
      debugFlag = true;
      System.out.println("Debug mode activated.");
    }
    if((args.length == 4 && !args[3].equals("-debug")) || (args.length < 3 || args.length > 4 || args[1].length() < 4 ||
            !pattern.matcher(args[1]).matches() || (!args[2].equals("S") && !args[2].equals("P")))) {
      System.out.println("Wrong arguments given.\nUsage: $ turmas address port qualifier -debugFlag");
      System.exit(1);
    }
    String host = args[0];
    int port = Integer.parseInt(args[1]);
    hostPort = host + ":" + port;
    qualifier = args[2];

    final BindableService professorImpl = new ProfessorServiceImpl(debugFlag);
    final BindableService studentServiceImpl = new StudentServiceImpl(debugFlag);
    final BindableService adminImpl = new AdminServiceImpl(debugFlag);
    final BindableService classServerImpl = new ClassServerServiceImpl(debugFlag, qualifier);

    // Create a new server to listen on port
    ServerBuilder serverBuilder = ServerBuilder.forPort(port);

    serverBuilder.addService(professorImpl);
    serverBuilder.addService(studentServiceImpl);
    serverBuilder.addService(adminImpl);
    serverBuilder.addService(classServerImpl);
    Server server = serverBuilder.build();

    namingServerFrontend.register(host, port, qualifier);

    // Start the server
    try {
      server.start();

      timer = new Timer(true);
      timerTask = new Gossip(hostPort, qualifier);
      timer.scheduleAtFixedRate(timerTask, 0, 30 * 1000);

      // Server threads are running in the background.
      System.out.println("Server started");

      // Do not exit the main thread. Wait until server is terminated.
      server.awaitTermination();
    }
    catch(Exception e) {
      e.printStackTrace();
      server.shutdownNow();
      System.exit(1);
    }
    finally {
      // Delete server on naming server
      namingServerFrontend.delete(host, port);
      namingServerFrontend.exit();
    }
  }
}