package pt.ulisboa.tecnico.classes.namingserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NamingServices {

    //Singleton
    private static NamingServices instance = null;

    private Map<String, ServiceEntry> serviceEntries = new ConcurrentHashMap<>();

    public NamingServices() {}

    public static NamingServices getInstance() {
        if (instance == null) {
            instance = new NamingServices();
        }
        return instance;
    }

    public Map<String, ServiceEntry> getServiceEntries() {
        return this.serviceEntries;
    }

    public void setServiceEntries(Map<String, ServiceEntry> serviceEntries) {
        this.serviceEntries = serviceEntries;
    }

    public void addServiceEntry(ServiceEntry serviceEntry) {
        this.serviceEntries.put(serviceEntry.getName(), serviceEntry);
    }

    public boolean serviceExists(String name) {
        return serviceEntries.containsKey(name);
    }

    public ServiceEntry getService(String name) {
        return serviceEntries.get(name);
    }

    public void deleteService(String name) {
        serviceEntries.remove(name);
    }
}
