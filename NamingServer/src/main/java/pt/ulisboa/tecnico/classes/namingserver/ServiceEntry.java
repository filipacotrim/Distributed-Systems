package pt.ulisboa.tecnico.classes.namingserver;

import java.util.HashSet;
import java.util.Set;

public class ServiceEntry {

    private String name;

    private Set<ServerEntry> serverEntries = new HashSet<>();

    public ServiceEntry() {}

    public ServiceEntry(String name) {
        this.name = name;
    }

    public String getName () {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ServerEntry> getServerEntries() {
        return serverEntries;
    }

    public void setServerEntries(Set<ServerEntry> serverEntries) {
        this.serverEntries = serverEntries;
    }

    public void addServerEntry(ServerEntry serverEntry) {
        for (ServerEntry existentServerEntry: serverEntries) {
            if (existentServerEntry.getHostPort().equals(serverEntry.getHostPort())) {
                serverEntries.remove(existentServerEntry);
                break;
            }
        }
        this.serverEntries.add(serverEntry);
    }
}
