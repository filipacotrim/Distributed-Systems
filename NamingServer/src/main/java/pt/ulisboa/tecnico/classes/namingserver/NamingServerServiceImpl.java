package pt.ulisboa.tecnico.classes.namingserver;

import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.DeleteRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.DeleteResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.ServerState;

import java.util.HashSet;
import java.util.Set;
import java.util.*;
import java.util.logging.Logger;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    private final NamingServices namingServices = NamingServices.getInstance();

    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    private final boolean debugFlag;

    public NamingServerServiceImpl(boolean debugFlag) { this.debugFlag = debugFlag;}

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        String serviceName = request.getService();
        String[] hostPort = request.getHostPort().split(":");
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);
        Set<String> qualifiers = new HashSet<>(request.getQualifiersList());

        if (debugFlag) {
            LOGGER.info("Server Register Starting...");
        }

        synchronized (namingServices) {
            ServerEntry serverEntry = new ServerEntry(host, port, qualifiers);
            ServiceEntry serviceEntry;
            if (namingServices.serviceExists(serviceName)) {
                serviceEntry = namingServices.getService(serviceName);
            }
            else {
                serviceEntry = new ServiceEntry(serviceName);
                namingServices.addServiceEntry(serviceEntry);
            }
            serviceEntry.addServerEntry(serverEntry);
        }

        //Create Response
        RegisterResponse response = RegisterResponse.getDefaultInstance();

        //Send Response
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        //Send ReObserver.onCompleted();
        if (debugFlag) {
            LOGGER.info("Server Register Finished.");
        }
    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        if (debugFlag) {
            LOGGER.info("Server Lookup Starting...");
        }

        String serviceName = request.getService();
        Set<String> qualifiers = new HashSet<>(request.getQualifiersList());

        LookupResponse.Builder response = LookupResponse.newBuilder();
        synchronized (namingServices) {
            for (Map.Entry<String, ServiceEntry> serviceEntry : namingServices.getServiceEntries().entrySet()) {
                if (serviceEntry.getKey().equals(serviceName)) {
                    Set<ServerEntry> serverEntries = serviceEntry.getValue().getServerEntries();

                    for (ServerEntry serverEntry : serverEntries) {
                        ServerState.Builder serverState = ServerState.newBuilder().setHost(serverEntry.getHost())
                                .setPort(String.valueOf(serverEntry.getPort())).addAllQualifiers(serverEntry.getQualifiers());
                        boolean containsQualifiers = true;

                        for (String qualifier : qualifiers) {
                            if (!serverEntry.getQualifiers().contains((qualifier))) {
                                containsQualifiers = false;
                                break;
                            }
                        }
                        if (containsQualifiers) {
                            response.addServers(serverState);
                        }
                    }
                    break;
                }
            }
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

        if (debugFlag) {
            LOGGER.info("Server Lookup Finished.");
        }
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        if (debugFlag) {
            LOGGER.info("Server Delete Starting...");
        }

        String serviceName = request.getService();
        String hostPort = request.getHostPort();

        synchronized (namingServices) {
            for (Map.Entry<String, ServiceEntry> serviceEntries : namingServices.getServiceEntries().entrySet()) {
                if (serviceEntries.getKey().equals(serviceName)) {
                    Set<ServerEntry> serverEntries = serviceEntries.getValue().getServerEntries();
                    serverEntries.removeIf(serverEntry -> serverEntry.getHostPort().equals(hostPort));
                    break;
                }
            }
        }

        responseObserver.onNext(DeleteResponse.getDefaultInstance());
        responseObserver.onCompleted();

        if (debugFlag) {
            LOGGER.info("Server Delete Finished.");
        }
    }
}
