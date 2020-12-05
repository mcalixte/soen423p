package servers.qc;

import service.StoreImpl;

import javax.xml.ws.Endpoint;

public class QuebecServer3 {

    public static StoreImpl quebecStoreImpl;

    public static void main(String[] args) {
        System.out.println("Quebec Server Started...");
        quebecStoreImpl = new StoreImpl("qc");
        Endpoint endpoint = Endpoint.publish("http://localhost:9000/quebecStore", quebecStoreImpl);
    }
}
