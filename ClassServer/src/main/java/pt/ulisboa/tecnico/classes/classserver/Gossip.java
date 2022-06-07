package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.NamingServerFrontend;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse;


import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import static io.grpc.Status.Code.*;

public class Gossip extends TimerTask {

    private final NamingServerFrontend namingServerFrontend = new NamingServerFrontend();

    private final Class classes = Class.getInstance();

    private final String hostPort;

    private final String qualifier;

    private ManagedChannel channel;

    private ClassServerServiceGrpc.ClassServerServiceBlockingStub stub;

    public Gossip(String hostPort, String qualifier) {
        this.hostPort = hostPort;
        this.qualifier = qualifier;
    }

    @Override
    public void run() {
        propagateState();
    }

    //Methods
    public void getServerStub(String hostPort) {
        String[] splitHostPort = hostPort.split(":");
        channel = ManagedChannelBuilder.forAddress(splitHostPort[0], Integer.parseInt(splitHostPort[1])).usePlaintext().build();
        stub = ClassServerServiceGrpc.newBlockingStub(channel);
    }

    public void closeServerStub() {
        channel.shutdownNow();
    }

    private void propagateState() {
        ClassState.Builder classState = ClassState.newBuilder();
        synchronized (classes) {
            if (!classes.getModified()) {
                return;
            }
            //Create ClassState
            classState.setCapacity(classes.getCapacity());
            classState.setOpenEnrollments(classes.getOpenEnrollments());
            classState.setClosedEnrollments(classes.getClosedEnrollments());
            for (Map.Entry<String, String[]> student : classes.getStudentsEnrolled().entrySet()) {
                classState.addEnrolled(ClassesDefinitions.Student.newBuilder().setStudentId(student.getKey())
                        .setStudentName(student.getValue()[0]).setTimeStamp(student.getValue()[1]).build());
            }
            for (Map.Entry<String, String[]> student : classes.getStudentsUnrolled().entrySet()) {
                classState.addDiscarded(ClassesDefinitions.Student.newBuilder().setStudentId(student.getKey())
                        .setStudentName(student.getValue()[0]).setTimeStamp(student.getValue()[1]).build());
            }
            classes.setModified(false);
        }

        List<String> hostPorts = namingServerFrontend.lookup();
        for (String hostPort : hostPorts) {
            if (hostPort.equals(this.hostPort)) {
                continue;
            }
            getServerStub(hostPort);
            try {
                PropagateStateRequest request = PropagateStateRequest.newBuilder().setClassState(classState.build()).setQualifier(this.qualifier).build();
                PropagateStateResponse response = stub.propagateState(request);
                System.out.println(Stringify.format(response.getCode()));
            }
            catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND || e.getStatus().getCode() == UNAVAILABLE) {
                    System.out.println(Stringify.format(ClassesDefinitions.ResponseCode.INACTIVE_SERVER));
                }
            }
            finally{
                closeServerStub();
            }
        }
    }

}
