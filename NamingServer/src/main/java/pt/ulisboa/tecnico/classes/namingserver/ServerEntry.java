package pt.ulisboa.tecnico.classes.namingserver;

import java.util.Set;

public class ServerEntry {

    private String host;

    private int port;

    private Set<String> qualifiers;

    public ServerEntry() {}
    public ServerEntry(String host, int port, Set<String> qualifiers) {
        this.host = host;
        this.port = port;
        this.qualifiers = qualifiers;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public String getHostPort() {
        return host + ":" + port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Set<String> getQualifiers() {
        return qualifiers;
    }

    public void addQualifier(String qualifier) {
        this.qualifiers.add(qualifier);
    }
}
