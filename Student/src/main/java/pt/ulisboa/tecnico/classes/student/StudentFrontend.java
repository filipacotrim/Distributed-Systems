package pt.ulisboa.tecnico.classes.student;

import pt.ulisboa.tecnico.classes.NamingServerFrontend;
import pt.ulisboa.tecnico.classes.Stringify;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.Code.*;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.ListClassRequest;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.ListClassResponse;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.EnrollRequest;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.EnrollResponse;
import pt.ulisboa.tecnico.classes.student.exception.StudentFrontendException;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc.StudentServiceBlockingStub;

import java.util.List;
import java.util.logging.Logger;


public class StudentFrontend {

    private final NamingServerFrontend namingServerFrontend = new NamingServerFrontend();

    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    private ManagedChannel channel;

    private StudentServiceBlockingStub stub;

    private final boolean debugFlag;

    public StudentFrontend(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }

    //Methods
    public void getServerStub(String hostPort) {
        String[] splitHostPort = hostPort.split(":");
        channel = ManagedChannelBuilder.forAddress(splitHostPort[0], Integer.parseInt(splitHostPort[1])).usePlaintext().build();
        stub = StudentServiceGrpc.newBlockingStub(channel);
    }

    public void closeServerStub() {
        channel.shutdownNow();
    }

    //Commands
    public void exit() {
        namingServerFrontend.exit();
    }

    public String list() throws StudentFrontendException {
        if (debugFlag) {
            LOGGER.info("List operation starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup();
        ClassesDefinitions.ResponseCode code = ClassesDefinitions.ResponseCode.INACTIVE_SERVER;
        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            for (String hostPort : hostPorts) {
                getServerStub(hostPort);
                try {
                    ListClassResponse response = stub.listClass(ListClassRequest.getDefaultInstance());
                    if (response.getCode() != ClassesDefinitions.ResponseCode.DISABLED_SERVER) {
                        namingServerFrontend.addOperation(hostPort);
                        if (debugFlag) {
                            LOGGER.info("List operation done!");
                        }
                        return Stringify.format(response.getClassState());
                    } else {
                        code = ResponseCode.DISABLED_SERVER;
                    }
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new StudentFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                }
            }
        }

        if (debugFlag) {
            LOGGER.info("List operation done!");
        }
        return Stringify.format(code);
    }

    public String enroll(Student student) throws StudentFrontendException {
        if (debugFlag) {
            LOGGER.info("Enroll operation starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup();
        ClassesDefinitions.ResponseCode code = ClassesDefinitions.ResponseCode.INACTIVE_SERVER;
        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            for (String hostPort : hostPorts) {
                getServerStub(hostPort);
                try {
                    EnrollRequest request = EnrollRequest.newBuilder().setStudent(ClassesDefinitions.Student.newBuilder()
                            .setStudentId(student.getStudentID())
                            .setStudentName(student.getStudentName()).build()).build();
                    EnrollResponse response = stub.enroll(request);
                    if (response.getCode() != ClassesDefinitions.ResponseCode.DISABLED_SERVER) {
                        namingServerFrontend.addOperation(hostPort);
                        if (debugFlag) {
                            LOGGER.info("Enroll operation done!");
                        }
                        return Stringify.format(response.getCode());
                    } else {
                        code = ResponseCode.DISABLED_SERVER;
                    }
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new StudentFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                }
            }
        }

        if (debugFlag) {
            LOGGER.info("Enroll operation done!");
        }
        return Stringify.format(code);
    }
}
