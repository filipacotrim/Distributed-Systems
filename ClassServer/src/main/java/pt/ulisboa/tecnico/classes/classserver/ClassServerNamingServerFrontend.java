package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.NamingServerFrontend;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;

public class ClassServerNamingServerFrontend extends NamingServerFrontend {

    //Constructor
    public ClassServerNamingServerFrontend () {
        super();
    }

    //Methods
    public void register(String host, int port, String qualifier) {
        super.getStub().register(ClassServerNamingServer.RegisterRequest.newBuilder().setService("turmas").setHostPort(host + ":" + port).addQualifiers(qualifier).build());
    }

    public void delete(String host, int port) {
        super.getStub().delete(ClassServerNamingServer.DeleteRequest.newBuilder().setService("turmas").setHostPort(host + ":" + port).build());
    }


}
