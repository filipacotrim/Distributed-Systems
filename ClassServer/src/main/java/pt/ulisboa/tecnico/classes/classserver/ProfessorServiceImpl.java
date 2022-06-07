package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.*;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;

import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.logging.Logger;

import static pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode.INACTIVE_SERVER;
import static pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode.OK;

public class ProfessorServiceImpl extends ProfessorServiceGrpc.ProfessorServiceImplBase {

    private final Class classes = Class.getInstance();

    private final boolean debugFlag;

    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    //Constructors
    public ProfessorServiceImpl(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }

    //Methods
    @Override
    public void listClass(ListClassRequest request, StreamObserver<ListClassResponse> responseObserver) {
        if (this.debugFlag) {
            LOGGER.info("Listing Class for Professor...");
        }

        ClassState.Builder classState = ClassState.newBuilder();
        ClassesDefinitions.ResponseCode code = ResponseCode.OK;
        synchronized (classes) {
            if (!classes.getActive()) {
                code = ResponseCode.DISABLED_SERVER;
            }
            else {
                //Create ClassState
                classState.setCapacity(classes.getCapacity());
                classState.setOpenEnrollments(classes.getOpenEnrollments());
                for (Map.Entry<String, String[]> student : classes.getStudentsEnrolled().entrySet()) {
                    classState.addEnrolled(Student.newBuilder().setStudentId(student.getKey()).setStudentName(student.getValue()[0]).build());
                }
                for (Map.Entry<String, String[]> student : classes.getStudentsUnrolled().entrySet()) {
                    classState.addDiscarded(Student.newBuilder().setStudentId(student.getKey()).setStudentName(student.getValue()[0]).build());
                }
            }
        }

        //Create Response
        ListClassResponse response = ListClassResponse.newBuilder().setCode(code).setClassState(classState.build()).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if (this.debugFlag) {
            LOGGER.info("Listing Class for Professor finished.");
        }
    }

    @Override
    public void openEnrollments(OpenEnrollmentsRequest request, StreamObserver<OpenEnrollmentsResponse> responseObserver) {
        int capacity = request.getCapacity();

        if (this.debugFlag) {
            LOGGER.info("Opening Enrollments of Class with Capacity " + capacity + "...");
        }

        ClassesDefinitions.ResponseCode code = OK;
        synchronized (classes) {
            if (!classes.getActive()) {
                code = ResponseCode.DISABLED_SERVER;
            }
            else {
                //Enrollemnts already open
                if (classes.getOpenEnrollments()) {
                    code = ResponseCode.ENROLLMENTS_ALREADY_OPENED;

                    if (this.debugFlag) {
                        LOGGER.info("Opening Enrollments Failed: Enrollments of Class already opened.");
                    }
                }
                //Opens enrollments
                else if (classes.openEnrollments(capacity)) {
                    classes.setModified(true);

                    if (this.debugFlag) {
                        LOGGER.info("Opening Enrollments Successfully Completed.");
                    }
                }
                //Capacity < Enrolled students
                else {
                    code = ResponseCode.FULL_CLASS;

                    if (this.debugFlag) {
                        LOGGER.info("Opening Enrollments Failed: Enrollments of Class already opened.");
                    }
                }
            }
        }

        //Create Response
        OpenEnrollmentsResponse response = OpenEnrollmentsResponse.newBuilder().setCode(code).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if (this.debugFlag) {
            LOGGER.info("Opening Enrollments of Class with Capacity " + capacity + " finished.");
        }
    }

    @Override
    public void closeEnrollments(CloseEnrollmentsRequest request, StreamObserver<CloseEnrollmentsResponse> responseObserver) {
        if (this.debugFlag) {
            LOGGER.info("Closing Enrollments of Class...");
        }

        ClassesDefinitions.ResponseCode code = ResponseCode.OK;
        synchronized (classes) {
            if (!classes.getActive()) {
                code = ResponseCode.DISABLED_SERVER;
            }
            else {
                //Closes enrollments
                if (classes.closeEnrollments()) {
                    classes.setModified(true);

                    if (this.debugFlag) {
                        LOGGER.info("Closing Enrollments Successfully Completed.");
                    }
                }
                //Enrollments already closed
                else {
                    code = ResponseCode.ENROLLMENTS_ALREADY_CLOSED;

                    if (this.debugFlag) {
                        LOGGER.info("Closing Enrollments Failed: Enrollments of Class already closed.");
                    }
                }
            }
        }

        //Create Response
        CloseEnrollmentsResponse response = CloseEnrollmentsResponse.newBuilder().setCode(code).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if (this.debugFlag) {
            LOGGER.info("Closing Enrollments of Class finished.");
        }
    }

    @Override
    public void cancelEnrollment(CancelEnrollmentRequest request, StreamObserver<CancelEnrollmentResponse> responseObserver) {
        String studentId = request.getStudentId();

        if (this.debugFlag) {
            LOGGER.info("Cancelling Enrollment of Student with Student Number " + studentId + " in Class...");
        }

        ClassesDefinitions.ResponseCode code = ResponseCode.OK;
        synchronized (classes) {
            if (!classes.getActive()) {
                code = ResponseCode.DISABLED_SERVER;
            }
            else {
                //Cancels student enrollment
                if (classes.cancelEnrollment(studentId)) {
                    classes.setModified(true);

                    if (this.debugFlag) {
                        LOGGER.info("Cancel Enrollment Successfully Completed.");
                    }
                }
                //Student does not exist or is not enrolled in class
                else {
                    code = ResponseCode.NON_EXISTING_STUDENT;

                    if (this.debugFlag) {
                        LOGGER.info("Cancel Enrollment Failed: The Student does not exist or is not enrolled in Class.");
                    }
                }
            }
        }

        //Create Response
        CancelEnrollmentResponse response = CancelEnrollmentResponse.newBuilder().setCode(code).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if (this.debugFlag) {
            LOGGER.info("Cancelling Enrollment of Student with Student Number " + studentId + " in Class finished.");
        }
    }
}