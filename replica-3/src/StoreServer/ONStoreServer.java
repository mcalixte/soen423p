package StoreServer;

import Components.store.StoreImplementation;

import javax.xml.ws.Endpoint;

public class ONStoreServer {
    public static StoreImplementation ontarioStoreImpl;

    public static void main(String[] args) {
        System.out.println("Ontario Server Started...");
        ontarioStoreImpl = new StoreImplementation("on");
        Endpoint endpoint = Endpoint.publish("http://localhost:8001/ontarioStore", ontarioStoreImpl);
    }
}