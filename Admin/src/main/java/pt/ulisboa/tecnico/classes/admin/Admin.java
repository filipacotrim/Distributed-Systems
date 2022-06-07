package pt.ulisboa.tecnico.classes.admin;

import java.util.Scanner;

import pt.ulisboa.tecnico.classes.admin.exception.AdminFrontendException;

public class Admin {

  private static final String EXIT_CMD = "exit";
  private static final String DUMP_CMD = "dump";
  private static final String ACTIVATE_CMD = "activate";
  private static final String DEACTIVATE_CMD = "deactivate";
  private static final String ACTIVATEGOSSIP_CMD = "activateGossip";
  private static final String DEACTIVATEGOSSIP_CMD = "deactivateGossip";
  private static final String GOSSIP_CMD = "gossip";

  public static void displayArgsMsg(String msg) {
    System.out.println(msg);
    System.out.printf("Usage: java %s%n or \n", Admin.class.getName());
    System.out.printf("Usage: java %s -debug\n", Admin.class.getName());
    System.exit(1);
  }

  public static void main(String[] args) {
    boolean debugFlag = false;
    if (args.length == 1) {
      if(!args[0].equals("-debug")){ displayArgsMsg("Wrong Arguments!"); }
      else{ debugFlag = true; }
    }
    else if (args.length > 1) { displayArgsMsg("Too many arguments!"); }

    AdminFrontend frontend = new AdminFrontend(debugFlag);

    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("> ");

      String qualifier = "P";
      String line = scanner.nextLine();
      String[] tokens = line.split(" ");

      // exit
      if (EXIT_CMD.equals(line)) {
        if (tokens.length > 1) {
          System.out.println("Too many arguments.");
          continue;
        }

        scanner.close();
        frontend.exit();
        break;
      }
      // dump
      else if (DUMP_CMD.equals(tokens[0])) {
        if (tokens.length > 1 && (!tokens[1].equals("P") && !tokens[1].equals("S"))) {
          System.out.println("Qualifier not valid.");
          continue;
        }
        else if (tokens.length > 2) {
          System.out.println("Too many arguments.");
          continue;
        }
        else if (tokens.length > 1) {
          qualifier = tokens[1];
        }

        try {
          System.out.println(frontend.dump(qualifier) + "\n");
        }
        catch (AdminFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      // activate
      else if (ACTIVATE_CMD.equals(tokens[0])) {
        if (tokens.length > 1 && (!tokens[1].equals("P") && !tokens[1].equals("S"))) {
          System.out.println("Qualifier not valid.");
          continue;
        }
        else if (tokens.length > 2) {
          System.out.println("Too many arguments.");
          continue;
        }
        else if (tokens.length > 1) {
          qualifier = tokens[1];
        }

        try {
          System.out.println(frontend.activate(qualifier) + "\n");
        }
        catch (AdminFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      // deactivate
      else if (DEACTIVATE_CMD.equals(tokens[0])) {
        if (tokens.length > 1 && (!tokens[1].equals("P") && !tokens[1].equals("S"))) {
          System.out.println("Qualifier not valid.");
          continue;
        }
        else if (tokens.length > 2) {
          System.out.println("Too many arguments.");
          continue;
        }
        else if (tokens.length > 1) {
          qualifier = tokens[1];
        }

        try {
          System.out.println(frontend.deactivate(qualifier) + "\n");
        }
        catch (AdminFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      else if (ACTIVATEGOSSIP_CMD.equals(tokens[0])) {
        if (tokens.length > 1 && (!tokens[1].equals("P") && !tokens[1].equals("S"))) {
          System.out.println("Qualifier not valid.");
          continue;
        }
        else if (tokens.length > 2) {
          System.out.println("Too many arguments.");
          continue;
        }
        else if (tokens.length > 1) {
          qualifier = tokens[1];
        }

        try {
          System.out.println(frontend.activateGossip(qualifier) + "\n");
        }
        catch (AdminFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      else if (DEACTIVATEGOSSIP_CMD.equals(tokens[0])) {
        if (tokens.length > 1 && (!tokens[1].equals("P") && !tokens[1].equals("S"))) {
          System.out.println("Qualifier not valid.");
          continue;
        }
        else if (tokens.length > 2) {
          System.out.println("Too many arguments.");
          continue;
        }
        else if (tokens.length > 1) {
          qualifier = tokens[1];
        }

        try {
          System.out.println(frontend.deactivateGossip(qualifier) + "\n");
        }
        catch (AdminFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      else if (GOSSIP_CMD.equals(tokens[0])) {
        if (tokens.length > 1 && (!tokens[1].equals("P") && !tokens[1].equals("S"))) {
          System.out.println("Qualifier not valid.");
          continue;
        }
        else if (tokens.length > 2) {
          System.out.println("Too many arguments.");
          continue;
        }
        else if (tokens.length > 1) {
          qualifier = tokens[1];
        }

        try {
          System.out.println(frontend.gossip(qualifier) + "\n");
        }
        catch (AdminFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      //invalid command
      else {
        System.out.println("Command not defined.\n");
      }
    }

    System.exit(0);
  }
}