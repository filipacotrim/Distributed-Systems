package pt.ulisboa.tecnico.classes.student;

import pt.ulisboa.tecnico.classes.student.exception.StudentFrontendException;

import java.util.Scanner;
import java.lang.String;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class Student {

  private static final String EXIT_CMD = "exit";
  private static final String LIST_CMD = "list";
  private static final String ENROLL_CMD = "enroll";

  private String studentID = "";
  private String studentName = "";

  public Student() {}

  public void setStudentID(String studentID) {
    this.studentID = studentID;
  }

  public void setStudentName(String studentName) {this.studentName = studentName;}

  public String getStudentID() { return this.studentID; }

  public String getStudentName() { return this.studentName; }

  public static void displayArgsMsg(String msg) {
    System.out.println(msg);
    System.out.printf("Usage: java %s StudentID StudentName \nor \n", Student.class.getName());
    System.out.printf("Usage: java %s StudentID StudentName -debug\n", Student.class.getName());
    System.exit(1);
  }

  public static void main(String[] args) {
    boolean debugFlag = false;
    if (args.length < 2) { displayArgsMsg("Incorrect Arguments given."); }
    else if (args.length >= 3) {
      if(!args[args.length - 1].equals("-debug")) { displayArgsMsg("Incorrect Arguments given."); }
      else { debugFlag = true; }
    }

    Student student = new Student();
    StudentFrontend studentFrontend = new StudentFrontend(debugFlag);

    Pattern pattern = Pattern.compile("aluno-?\\d+(\\.\\d+)?");
    if(args[0].length() != 9 || !pattern.matcher(args[0]).matches()) {
      displayArgsMsg("StudentID Usage: alunoXXXX , where XXXX is your student number.");
    }
    student.setStudentID(args[0]);

    /*Supports any number of names given that the total length is less or equal than 30 and more than 3.*/
    StringJoiner joiner = new StringJoiner(" ");
    for(int i = 1; i < args.length; i++) {
      if(args[i].equals("-debug")) { break; }
      joiner.add(args[i]);
      if(joiner.length() > 30) {
        displayArgsMsg("Student Name should not be longer than 30 characters.");
      }
    }
    if(joiner.length() < 3) {
      displayArgsMsg("Student Name should be longer than 3 letters");
    }
    student.setStudentName(joiner.toString());

    Scanner scanner = new Scanner(System.in);
    System.out.println(student.getStudentID() + student.getStudentName());
    while (true) {
      System.out.print("> ");

      String line = scanner.nextLine();
      String[] tokens = line.split(" ");

      //exit cmd
      if (EXIT_CMD.equals(tokens[0])) {
        if (tokens.length > 1) {
          System.out.println("Too many arguments.");
          continue;
        }

        scanner.close();
        studentFrontend.exit();
        break;
      }

      //list cmd
      else if (LIST_CMD.equals(tokens[0])) {
        if (tokens.length > 1) {
          System.out.println("Too many arguments.");
          continue;
        }

        try{
          System.out.println(studentFrontend.list() + "\n");
        }
        catch (StudentFrontendException e) {
          System.err.println(e.getMessage());
        }
      }

      //enroll cmd
      else if (ENROLL_CMD.equals(tokens[0])) {
        if (tokens.length > 1) {
          System.out.println("Too many arguments.");
          continue;
        }

        try{
          System.out.println(studentFrontend.enroll(student) + "\n");
        }
        catch (StudentFrontendException e) {
        System.err.println(e.getMessage());
        }
      }

      //undefined command
      else{
        System.out.println("Command not defined.\n");
      }
    }

    System.exit(0);
  }
}
