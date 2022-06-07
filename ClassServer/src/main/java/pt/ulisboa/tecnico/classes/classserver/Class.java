package pt.ulisboa.tecnico.classes.classserver;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

public class Class {

    //Singleton
    private static Class instance = null;

    private int capacity;

    private boolean openEnrollments;

    //concurrent hash map students (student ID + student Name)
    private ConcurrentHashMap<String, String[]> studentsEnrolled = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, String[]> studentsUnrolled = new ConcurrentHashMap<>();

    private boolean modified = false;

    private boolean active = true;

    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String closedEnrollments = LocalDateTime.now().format(dateTimeFormat);

    //Constructor
    //Singleton
    private Class() {}

    //Getters
    //Singleton
    public static Class getInstance() {
        if (instance == null) {
            instance = new Class();
        }
        return instance;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean getOpenEnrollments() {
        return this.openEnrollments;
    }

    public String getClosedEnrollments() {
        return this.closedEnrollments;
    }

    public ConcurrentHashMap<String, String[]> getStudentsEnrolled() {
        return this.studentsEnrolled;
    }

    public ConcurrentHashMap<String, String[]> getStudentsUnrolled() {
        return this.studentsUnrolled;
    }

    public boolean getModified() {
        return this.modified;
    }

    public boolean getActive() {
        return this.active;
    }

    public boolean isEnrolled(String studentId) {
        return this.studentsEnrolled.containsKey(studentId);
    }

    public boolean isUnrolled(String studentId) {
        return this.studentsUnrolled.containsKey(studentId);
    }

    public String getTimeStamp(String studentId) {
        if (this.isEnrolled(studentId)) {
            return this.studentsEnrolled.get(studentId)[1];
        }
        else if (this.isUnrolled(studentId)) {
            return this.studentsUnrolled.get(studentId)[1];
        }
        return "";
    }

    //Setters
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setEnrollmentsOpen(boolean openEnrollments) {
        this.openEnrollments = openEnrollments;
    }

    public void setClosedEnrollments(String closedEnrollments) {
        this.closedEnrollments = closedEnrollments;
    }

    public void setStudentsEnrolled(Map<String, String[]> studentsEnrolled) {
        this.studentsEnrolled = new ConcurrentHashMap<String, String[]>(studentsEnrolled);
    }

    public void setStudentsUnrolled(Map<String, String[]> studentsUnrolled) {
        this.studentsUnrolled = new ConcurrentHashMap<String, String[]>(studentsUnrolled);;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    //Methods
    public void enroll(String studentId, String studentName) {
        studentsUnrolled.remove(studentId);
        String[] value = {studentName, LocalDateTime.now().format(dateTimeFormat)};
        studentsEnrolled.put(studentId, value);
    }

    public boolean cancelEnrollment(String studentId) {
        if (!studentsEnrolled.containsKey(studentId)) {
            return false;
        }
        String studentName = studentsEnrolled.get(studentId)[0];
        String[] value = { studentName, LocalDateTime.now().format(dateTimeFormat)};
        studentsEnrolled.remove(studentId);
        studentsUnrolled.put(studentId, value);
        return true;
    }

    public void addStudentsUnrolled(String studentId, String studentName) {
        String[] value = {studentName, LocalDateTime.now().format(dateTimeFormat)};
        studentsUnrolled.put(studentId, value);
    }

    public boolean openEnrollments(int capacity) {
        if (capacity < this.studentsEnrolled.size()) {
            return false;
        }
        this.openEnrollments = true;
        this.capacity = capacity;
        return true;
    }

    public boolean closeEnrollments() {
        if (!this.openEnrollments) {
            return false;
        }
        this.closedEnrollments = LocalDateTime.now().format(dateTimeFormat);
        this.openEnrollments = false;
        return true;
    }
}
