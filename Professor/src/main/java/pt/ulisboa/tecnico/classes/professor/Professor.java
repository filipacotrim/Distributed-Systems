package pt.ulisboa.tecnico.classes.professor;

import pt.ulisboa.tecnico.classes.admin.exception.ProfessorFrontendException;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Professor {

  private static final String EXIT_CMD = "exit";
  private static final String LIST_CMD = "list";
  private static final String OPENENROLLMENTS_CMD = "openEnrollments";
  private static final String CLOSEENROLLMENTS_CMD = "closeEnrollments";
  private static final String CANCELENROLLMENT_CMD = "cancelEnrollment";

  public static void displayArgsMsg(String msg) {
    System.out.println(msg);
    System.out.printf("Usage: java %s \nor \n", Professor.class.getName());
    System.out.printf("Usage: java %s -debug\n", Professor.class.getName());
    System.exit(1);
  }

  public static void main(String[] args) {

    boolean debugFlag = false;
    if (args.length == 1) {
      if(!args[0].equals("-debug")){ displayArgsMsg("Wrong Arguments!"); }
      else{ debugFlag = true; }
    }
    else if (args.length > 1) { displayArgsMsg("Too many arguments!"); }

    ProfessorFrontend frontend = new ProfessorFrontend(debugFlag);

    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("> ");

      String line = scanner.nextLine();
      String[] tokens = line.split(" ");

      //exit
      if (EXIT_CMD.equals(tokens[0])) {
        if (tokens.length > 1) {
          System.out.println("Too many arguments.");
          continue;
        }

        frontend.exit();
        scanner.close();
        break;
      }
      //list
      else if (LIST_CMD.equals(tokens[0])) {
        if (tokens.length > 1) {
          System.out.println("Too many arguments.");
          continue;
        }
        try {
          System.out.println(frontend.list() + "\n");
        } catch (ProfessorFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      //openEnrollments
      else if (OPENENROLLMENTS_CMD.equals(tokens[0])) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (tokens.length == 1 || !pattern.matcher(tokens[1]).matches()) {
          System.out.println("Capacity not valid.");
          continue;
        }
        else if (tokens.length > 2) {
          System.out.println("Too many arguments.");
          continue;
        }

        try {
          System.out.println(frontend.openEnrollments(Integer.parseInt(tokens[1])) + "\n");
        } catch (ProfessorFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      //closeEnrollments
      else if (CLOSEENROLLMENTS_CMD.equals(tokens[0])) {
        if (tokens.length > 1) {
          System.out.println("Too many arguments.");
          continue;
        }

        try {
          System.out.println(frontend.closeEnrollments() + "\n");
        } catch (ProfessorFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      //cancelEnrollment
      else if (CANCELENROLLMENT_CMD.equals(tokens[0])) {
        Pattern pattern = Pattern.compile("aluno-?\\d+(\\.\\d+)?");
        if (tokens.length == 1 || tokens[1].length() != 9 || !pattern.matcher(tokens[1]).matches()) {
          System.out.println("Student ID not valid.");
          continue;
        }
        else if (tokens.length > 2) {
          System.out.println("Too many arguments.");
          continue;
        }

        try {
          System.out.println(frontend.cancelEnrollment(tokens[1]) + "\n");
        } catch (ProfessorFrontendException e) {
          System.err.println(e.getMessage());
        }
      }
      else {
        System.out.println("Operation not defined.\n");
      }
    }

    System.exit(0);
  }
}