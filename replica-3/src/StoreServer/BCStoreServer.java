package StoreServer;

import Components.store.StoreImplementation;

import javax.xml.ws.Endpoint;

public class BCStoreServer {
    public static StoreImplementation britishColumbiaStoreImpl;

    public static void main(String[] args) {
        System.out.println("British Columbia Server Started...");
        britishColumbiaStoreImpl = new StoreImplementation("bc");
        Endpoint endpoint = Endpoint.publish("http://localhost:8000/britishColumbiaStore", britishColumbiaStoreImpl);
    }
}
