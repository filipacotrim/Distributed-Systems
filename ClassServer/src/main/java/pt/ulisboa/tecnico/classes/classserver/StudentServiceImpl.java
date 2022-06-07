package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.*;
import static pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode.*;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;

import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.logging.Logger;

public class StudentServiceImpl extends StudentServiceGrpc.StudentServiceImplBase {

    private final Class classes = Class.getInstance();

    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    private final boolean debugFlag;

    public StudentServiceImpl(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }

    @Override
    public void listClass(ListClassRequest request, StreamObserver<ListClassResponse> responseObserver) {
        if(debugFlag) {
            LOGGER.info("Listing Class for Student.");
        }

        ClassState.Builder classState = ClassState.newBuilder();
        ClassesDefinitions.ResponseCode code = OK;
        synchronized (classes) {
            if (!classes.getActive()) {
                code = DISABLED_SERVER;
            }
            else {
                classState.setCapacity(classes.getCapacity());
                classState.setOpenEnrollments(classes.getOpenEnrollments());

                for (Map.Entry<String, String[]> studentEntry : classes.getStudentsEnrolled().entrySet()) {
                    Student.Builder student = Student.newBuilder();
                    student.setStudentId(studentEntry.getKey()).setStudentName(studentEntry.getValue()[0]).build();
                    classState.addEnrolled(student);
                }
                for (Map.Entry<String, String[]> studentEntry : classes.getStudentsUnrolled().entrySet()) {
                    Student.Builder student = Student.newBuilder();
                    student.setStudentId(studentEntry.getKey()).setStudentName(studentEntry.getValue()[0]).build();
                    classState.addDiscarded(student);
                }
            }
        }

        ListClassResponse response = ListClassResponse.newBuilder().setCode(code).setClassState(classState.build()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        if(debugFlag) {
            LOGGER.info("List Class to Student Finished.");
        }
    }

    @Override
    public void enroll(EnrollRequest request, StreamObserver<EnrollResponse> responseObserver) {
        String studentID = request.getStudent().getStudentId();
        String studentName = request.getStudent().getStudentName();
        EnrollResponse response;
        if(debugFlag) {
            LOGGER.info("Enrolling Student "+ studentName +" with Student number "+ studentID+" in Class.");
        }

        synchronized (classes) {
            if (!classes.getActive()) {
                response = EnrollResponse.newBuilder().setCode(DISABLED_SERVER).build();
            }
            else {
                if (!classes.getOpenEnrollments()) {
                    response = EnrollResponse.newBuilder().setCode(ENROLLMENTS_ALREADY_CLOSED).build();
                    if (debugFlag) {
                        LOGGER.info("Enrollment failed: The class has reached its maximum capacity.");
                    }
                } else if (classes.isEnrolled(studentID)) {
                    response = EnrollResponse.newBuilder().setCode(STUDENT_ALREADY_ENROLLED).build();
                    if (debugFlag) {
                        LOGGER.info("Enrollment failed: The student is already enrolled.\n");
                    }
                } else if (classes.getCapacity() == classes.getStudentsEnrolled().size()) {
                    response = EnrollResponse.newBuilder().setCode(FULL_CLASS).build();
                    if (debugFlag) {
                        LOGGER.info("Enrollment failed: The class has reached its maximum capacity.");
                    }
                } else {
                    classes.enroll(studentID, studentName);
                    classes.setModified(true);
                    response = EnrollResponse.newBuilder().setCode(OK).build();
                    if (debugFlag) {
                        LOGGER.info("Enrolling successfully completed.");
                    }
                }
            }
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        if(debugFlag) {
            LOGGER.info("Enrolling Student "+ studentName +" with Student number "+ studentID+" in Class is Finished.");
        }
    }
}
