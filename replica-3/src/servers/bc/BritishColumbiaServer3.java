package servers.bc;

import service.StoreImpl;

import javax.xml.ws.Endpoint;

public class BritishColumbiaServer3 {
    public static StoreImpl britishColumbiaStoreImpl;

    public static void main(String[] args) {
        System.out.println("British Columbia Server Started...");
        britishColumbiaStoreImpl = new StoreImpl("bc");
        Endpoint endpoint = Endpoint.publish("http://localhost:9002/britishColumbiaStore", britishColumbiaStoreImpl);
    }
}