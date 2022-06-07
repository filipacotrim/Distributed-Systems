package pt.ulisboa.tecnico.classes.admin;

import pt.ulisboa.tecnico.classes.NamingServerFrontend;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc.AdminServiceBlockingStub;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DumpRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DumpResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.ActivateRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.ActivateResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DeactivateRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DeactivateResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.ActivateGossipRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.ActivateGossipResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DeactivateGossipRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DeactivateGossipResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.GossipRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.GossipResponse;
import pt.ulisboa.tecnico.classes.admin.exception.AdminFrontendException;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static io.grpc.Status.Code.*;

public class AdminFrontend {

    private final NamingServerFrontend namingServerFrontend = new NamingServerFrontend();

    private ManagedChannel channel;

    private AdminServiceBlockingStub stub;

    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    private final boolean debugFlag;

    public AdminFrontend(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }

    //Methods
    private void getServerStub(String hostPort) {
        String[] splitHostPort = hostPort.split(":");
        channel = ManagedChannelBuilder.forAddress(splitHostPort[0], Integer.parseInt(splitHostPort[1])).usePlaintext().build();
        stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    private void closeServerStub() {
        channel.shutdownNow();
    }

    private String buildResponse(List<String> hostPorts, String[] responseCode, int numServers) {
        if (debugFlag) {
            LOGGER.info("Building response ...");
        }
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < numServers; i++) {
            response.append(hostPorts.get(i)).append(": ");
            if (responseCode[i].equals("")) {
                response.append(Stringify.format(ClassesDefinitions.ResponseCode.INACTIVE_SERVER));
            }
            else {
                response.append(responseCode[i]);
            }
            response.append("\n");
        }

        if (debugFlag) {
            LOGGER.info("Building response done!");
        }
        return response.toString();
    }

    //Commands
    public void exit() {
        namingServerFrontend.exit();
    }

    public String dump(String qualifier) throws AdminFrontendException {
        if (debugFlag) {
            LOGGER.info("Dump operation Starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup(qualifier);
        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            for (String hostPort : hostPorts) {
                getServerStub(hostPort);
                try {
                    DumpResponse response = stub.dump(DumpRequest.getDefaultInstance());
                    namingServerFrontend.addOperation(hostPort);
                    if (debugFlag) {
                        LOGGER.info("Dump operation done!");
                    }
                    return Stringify.format(response.getClassState());
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new AdminFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                }
            }
        }

        if (debugFlag) {
            LOGGER.info("Dump operation done!");
        }
        return Stringify.format(ClassesDefinitions.ResponseCode.INACTIVE_SERVER);
    }

    public String activate(String qualifier) throws AdminFrontendException {
        if (debugFlag) {
            LOGGER.info("Activate operation Starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup(qualifier);
        int numServers = hostPorts.size();
        String[] responseCode = new String[numServers];
        Arrays.fill(responseCode, "");

        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            int j = 0;
            for (String hostPort : hostPorts) {
                if (!responseCode[j].equals("")) {
                    continue;
                }
                getServerStub(hostPort);
                try {
                    ActivateResponse response = stub.activate(ActivateRequest.getDefaultInstance());
                    namingServerFrontend.addOperation(hostPort);
                    responseCode[j] = Stringify.format(response.getCode());
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new AdminFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                    j++;
                }
            }
        }

        if (debugFlag) {
            LOGGER.info("Activate operation done!");
        }
        return this.buildResponse(hostPorts, responseCode, numServers);
    }

    public String deactivate(String qualifier) throws AdminFrontendException {
        if (debugFlag) {
            LOGGER.info("Deactivate operation Starting...");
        }
        List<String> hostPorts = namingServerFrontend.lookup(qualifier);
        int numServers = hostPorts.size();
        String[] responseCode = new String[numServers];
        Arrays.fill(responseCode, "");

        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            int j = 0;
            for (String hostPort : hostPorts) {
                if (!responseCode[j].equals("")) {
                    continue;
                }
                getServerStub(hostPort);
                try {
                    DeactivateResponse response = stub.deactivate(DeactivateRequest.getDefaultInstance());
                    namingServerFrontend.addOperation(hostPort);
                    responseCode[j] = Stringify.format(response.getCode());
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new AdminFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                    j++;
                }
            }
        }

        if (debugFlag) {
            LOGGER.info("Deactivate operation done!");
        }
        return this.buildResponse(hostPorts, responseCode, numServers);
    }

    public String activateGossip(String qualifier) throws AdminFrontendException {
        if (debugFlag) {
            LOGGER.info("Activate operation starting ...");
        }
        List<String> hostPorts = namingServerFrontend.lookup(qualifier);
        int numServers = hostPorts.size();
        String[] responseCode = new String[numServers];
        Arrays.fill(responseCode, "");

        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            int j = 0;
            for (String hostPort : hostPorts) {
                if (!responseCode[j].equals("")) {
                    continue;
                }
                getServerStub(hostPort);
                try {
                    ActivateGossipResponse response = stub.activateGossip(ActivateGossipRequest.getDefaultInstance());
                    namingServerFrontend.addOperation(hostPort);
                    responseCode[j] = Stringify.format(response.getCode());
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new AdminFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                    j++;
                }
            }
        }

        if (debugFlag) {
            LOGGER.info("Activate Gossip operation done!");
        }
        return this.buildResponse(hostPorts, responseCode, numServers);
    }

    public String deactivateGossip(String qualifier) throws AdminFrontendException {
        if (debugFlag) {
            LOGGER.info("Deactivate Gossip operation starting ...");
        }
        List<String> hostPorts = namingServerFrontend.lookup(qualifier);
        int numServers = hostPorts.size();
        String[] responseCode = new String[numServers];
        Arrays.fill(responseCode, "");

        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            int j = 0;
            for (String hostPort : hostPorts) {
                if (!responseCode[j].equals("")) {
                    continue;
                }
                getServerStub(hostPort);
                try {
                    DeactivateGossipResponse response = stub.deactivateGossip(DeactivateGossipRequest.getDefaultInstance());
                    namingServerFrontend.addOperation(hostPort);
                    responseCode[j] = Stringify.format(response.getCode());
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new AdminFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                    j++;
                }
            }
        }

        if (debugFlag) {
            LOGGER.info("Deactivate Gossip operation done!");
        }
        return this.buildResponse(hostPorts, responseCode, numServers);
    }

    public String gossip(String qualifier) throws AdminFrontendException {
        if (debugFlag) {
            LOGGER.info("Gossip operation starting ...");
        }
        List<String> hostPorts = namingServerFrontend.lookup(qualifier);
        int numServers = hostPorts.size();
        String[] responseCode = new String[numServers];
        Arrays.fill(responseCode, "");

        //try each server two times before giving up
        for(int i = 0; i < 2; i ++) {
            int j = 0;
            for (String hostPort : hostPorts) {
                if (!responseCode[j].equals("")) {
                    continue;
                }
                getServerStub(hostPort);
                try {
                    GossipResponse response = stub.gossip(GossipRequest.getDefaultInstance());
                    namingServerFrontend.addOperation(hostPort);
                    responseCode[j] = Stringify.format(response.getCode());
                } catch (StatusRuntimeException e) {
                    if (!(e.getStatus().getCode() == DEADLINE_EXCEEDED || e.getStatus().getCode() == NOT_FOUND ||
                            e.getStatus().getCode() == UNAVAILABLE)) {
                        throw new AdminFrontendException(e.getMessage());
                    }
                } finally {
                    closeServerStub();
                    j++;
                }
            }
        }

        if (debugFlag) {
            LOGGER.info("Gossip operation done!");
        }
        return this.buildResponse(hostPorts, responseCode, numServers);
    }

}