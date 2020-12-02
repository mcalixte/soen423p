package StoreServer;

import Components.store.StoreImplementation;

import javax.xml.ws.Endpoint;

public class QCStoreServer {

    public static StoreImplementation quebecStoreImpl;

    public static void main(String[] args) {
        System.out.println("Quebec Server Started...");
        quebecStoreImpl = new StoreImplementation("qc");
        Endpoint endpoint = Endpoint.publish("http://localhost:8002/quebecStore", quebecStoreImpl);
    }
}