package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.*;

import static pt.ulisboa.tecnico.classes.classserver.ClassServer.timer;
import static pt.ulisboa.tecnico.classes.classserver.ClassServer.timerTask;
import static pt.ulisboa.tecnico.classes.classserver.ClassServer.hostPort;
import static pt.ulisboa.tecnico.classes.classserver.ClassServer.qualifier;
import static pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode.*;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;

import java.util.Timer;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;
import java.util.Map;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {
    private final Class classes = Class.getInstance();

    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    private final boolean debugFlag;

    private boolean activeGossip = true;

    public AdminServiceImpl(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }

    @Override
    public void dump(DumpRequest request, StreamObserver<DumpResponse> responseObserver) {
        if (debugFlag) {
            LOGGER.info("Dump Admin Request Starting");
        }

        ClassState.Builder classState = ClassState.newBuilder();
        synchronized (classes) {
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

        //Create Response
        DumpResponse response = DumpResponse.newBuilder().setCode(OK).setClassState(classState.build()).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        if (debugFlag) {
            LOGGER.info("Dump Admin Request Finished");
        }
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        if (debugFlag) {
            LOGGER.info("Activate Admin Request Starting");
        }

        synchronized (classes) {
            classes.setActive(true);
        }

        //Create Response
        ActivateResponse response = ActivateResponse.newBuilder().setCode(OK).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        if(debugFlag) {
            LOGGER.info("Activate Admin Request Finished");
        }
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        if (debugFlag) {
            LOGGER.info("Deactivate Admin Request Starting");
        }

        synchronized (classes) {
            classes.setActive(false);
        }

        //Create Response
        DeactivateResponse response = DeactivateResponse.newBuilder().setCode(OK).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        if(debugFlag) {
            LOGGER.info("Deactivate Admin Request Finished");
        }
    }

    @Override
    public void activateGossip(ActivateGossipRequest request, StreamObserver<ActivateGossipResponse> responseObserver) {
        if (debugFlag) {
            LOGGER.info("ActivateGossip Admin Request Starting");
        }

        synchronized (classes) {
            if (!activeGossip) {
                timer = new Timer(true);
                timerTask = new Gossip(hostPort, qualifier);
                timer.scheduleAtFixedRate(timerTask, 0, 30 * 1000);
                this.activeGossip = true;
            }
        }

        //Create Response
        ActivateGossipResponse response = ActivateGossipResponse.newBuilder().setCode(OK).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        if(debugFlag) {
            LOGGER.info("ActivateGossip Admin Request Finished");
        }
    }

    @Override
    public void deactivateGossip(DeactivateGossipRequest request, StreamObserver<DeactivateGossipResponse> responseObserver) {
        if (debugFlag) {
            LOGGER.info("DeactivateGossip Admin Request Starting");
        }

        synchronized (classes) {
            if (activeGossip) {
                timer.cancel();
                this.activeGossip = false;
            }
        }

        //Create Response
        DeactivateGossipResponse response = DeactivateGossipResponse.newBuilder().setCode(OK).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        if(debugFlag) {
            LOGGER.info("DeactivateGossip Admin Request Finished");
        }
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        if (debugFlag) {
            LOGGER.info("Gossip Admin Request Starting");
        }

        timerTask.run();

        //Create Response
        GossipResponse response = GossipResponse.newBuilder().setCode(OK).build();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        if(debugFlag) {
            LOGGER.info("Gossip Admin Request Finished");
        }
    }

}