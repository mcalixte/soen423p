package servers.on;

import service.StoreImpl;

import javax.xml.ws.Endpoint;

public class OntarioServer {
    public static StoreImpl ontarioStoreImpl;

    public static void main(String[] args) {
        System.out.println("Ontario Server Started...");
        ontarioStoreImpl = new StoreImpl("on");
        Endpoint endpoint = Endpoint.publish("http://localhost:8081/ontarioStore", ontarioStoreImpl);
    }
}
