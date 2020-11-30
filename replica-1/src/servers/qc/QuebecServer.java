package servers.qc;

import service.StoreImpl;

import javax.xml.ws.Endpoint;

public class QuebecServer {

    public static StoreImpl quebecStoreImpl;

    public static void main(String[] args) {
        System.out.println("Quebec Server Started...");
        quebecStoreImpl = new StoreImpl("qc");
        Endpoint endpoint = Endpoint.publish("http://localhost:8082/quebecStore", quebecStoreImpl);
    }
}
