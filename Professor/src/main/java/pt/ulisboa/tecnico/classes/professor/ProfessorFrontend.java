package pt.ulisboa.tecnico.classes.professor;

import pt.ulisboa.tecnico.classes.NamingServerFrontend;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.*;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.admin.exception.ProfessorFrontendException;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc.ProfessorServiceBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.List;
import java.util.logging.Logger;

import static io.grpc.Status.Code.*;

public class ProfessorFrontend {

    private final NamingServerFrontend namingServerFrontend = new NamingServerFrontend();

    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    private ManagedChannel channel;
    
    private ProfessorServiceBlockingStub stub;

    private final boolean debugFlag;

    //Constructor
    public ProfessorFrontend(boolean debugFlag) { this.debugFlag = debugFlag; }

    //Methods
    public void getServerStub(String hostPort) {
        String[] splitHostPort = hostPort.split(":");
        channel = ManagedChannelBuilder.forAddress(splitHostPort[0], Integer.parseInt(splitHostPort[1])).usePlaintext().build();
        stub = ProfessorServiceGrpc.newBlockingStub(channel);
    }
    
    public void closeServerStub() {
        channel.shutdownNow();
    }

    //Commands
    public void exit() {
        namingServerFrontend.exit();
    }

    public String list() throws ProfessorFrontendException {
        if (debugFlag) {
            LOGGER.info("List operation starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup();
        ClassesDefinitions.ResponseCode code = ResponseCode.INACTIVE_SERVER;
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
                        throw new ProfessorFrontendException(e.getMessage());
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

    public String openEnrollments(int capacity) throws ProfessorFrontendException {
        if (debugFlag) {
            LOGGER.info("Open Enrollments operation starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup("P");
        ClassesDefinitions.ResponseCode code = ResponseCode.INACTIVE_SERVER;
        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            for (String hostPort : hostPorts) {
                getServerStub(hostPort);
                try {
                    OpenEnrollmentsResponse response = stub.openEnrollments(OpenEnrollmentsRequest.newBuilder().setCapacity(capacity).build());
                    if (response.getCode() != ClassesDefinitions.ResponseCode.DISABLED_SERVER) {
                        namingServerFrontend.addOperation(hostPort);
                        if (debugFlag) {
                            LOGGER.info("Open Enrollments operation done!");
                        }
                        return Stringify.format(response.getCode());
                    } else {
                        code = ResponseCode.DISABLED_SERVER;
                    }
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new ProfessorFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                }
            }
        }
        if (debugFlag) {
            LOGGER.info("Open Enrollments operation done!");
        }
        return Stringify.format(code);
    }

    public String closeEnrollments() throws ProfessorFrontendException {
        if (debugFlag) {
            LOGGER.info("Close Enrollments operation starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup("P");
        ClassesDefinitions.ResponseCode code = ResponseCode.INACTIVE_SERVER;
        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            for (String hostPort : hostPorts) {
                getServerStub(hostPort);
                try {
                    CloseEnrollmentsResponse response = stub.closeEnrollments(CloseEnrollmentsRequest.getDefaultInstance());
                    if (response.getCode() != ClassesDefinitions.ResponseCode.DISABLED_SERVER) {
                        namingServerFrontend.addOperation(hostPort);
                        if (debugFlag) {
                            LOGGER.info("Close Enrollments operation done!");
                        }
                        return Stringify.format(response.getCode());
                    } else {
                        code = ResponseCode.DISABLED_SERVER;
                    }
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new ProfessorFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                }
            }
        }
        if (debugFlag) {
            LOGGER.info("Close Enrollments operation done!");
        }
        return Stringify.format(code);
    }

    public String cancelEnrollment(String studentId) throws ProfessorFrontendException {
        if (debugFlag) {
            LOGGER.info("Cancel Enrollment operation starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup("P");
        ClassesDefinitions.ResponseCode code = ResponseCode.INACTIVE_SERVER;
        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            for (String hostPort : hostPorts) {
                getServerStub(hostPort);
                try {
                    CancelEnrollmentResponse response = stub.cancelEnrollment(CancelEnrollmentRequest.newBuilder().setStudentId(studentId).build());
                    if (response.getCode() != ClassesDefinitions.ResponseCode.DISABLED_SERVER) {
                        namingServerFrontend.addOperation(hostPort);
                        if (debugFlag) {
                            LOGGER.info("Cancel Enrollment operation done!");
                        }
                        return Stringify.format(response.getCode());
                    } else {
                        code = ResponseCode.DISABLED_SERVER;
                    }
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new ProfessorFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                }
            }
        }
        if (debugFlag) {
            LOGGER.info("Cancel Enrollment operation done!");
        }
        return Stringify.format(code);
    }
}