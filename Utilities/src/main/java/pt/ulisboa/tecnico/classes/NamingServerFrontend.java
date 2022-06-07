package pt.ulisboa.tecnico.classes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.ServerState;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc.NamingServerServiceBlockingStub;

import java.util.*;

public class NamingServerFrontend {

    private final ManagedChannel channel;

    private final NamingServerServiceBlockingStub stub;

    private Map<String, Integer> operations = new HashMap<>();

    //Constructor
    public NamingServerFrontend() {
        int port = 5000;
        channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
        // Create a blocking stub.
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public NamingServerServiceBlockingStub getStub () {
        return this.stub;
    }

    public void addOperation(String hostPort) {
        operations.put(hostPort, operations.get(hostPort) + 1);
    }

    //Methods
    private List<String> lookup(List<String> qualifiers) {
        ClassServerNamingServer.LookupRequest.Builder request = ClassServerNamingServer.LookupRequest.newBuilder()
                .setService("turmas");
        for (String qualifier : qualifiers) {
            request.addQualifiers(qualifier);
        }
        List<ServerState> serverStates = new ArrayList<>(stub.lookup(request.build()).getServersList());

        List<Map.Entry<String, Integer>> operationEntries = new ArrayList<>();
        for(ServerState serverState: serverStates) {
            String hostPort = serverState.getHost()+":"+serverState.getPort();
            if (!operations.containsKey(hostPort)) {
                operations.put(hostPort, 0);
            }
            operationEntries.add(new AbstractMap.SimpleEntry<String, Integer>(hostPort, operations.get(hostPort)));
        }
        operationEntries.sort(Map.Entry.comparingByValue());

        List<String> hostPortsSorted = new ArrayList<>();
        for(Map.Entry<String, Integer> operationsEntry: operationEntries) {
            hostPortsSorted.add(operationsEntry.getKey());
        }

        return hostPortsSorted;
    }

    public List<String> lookup(String qualifier) {
        return lookup(new ArrayList<String>(Collections.singleton(qualifier)));
    }

    public List<String> lookup() {
        return lookup(new ArrayList<String>());
    }

    public void exit() {
        channel.shutdownNow();
    }
}
