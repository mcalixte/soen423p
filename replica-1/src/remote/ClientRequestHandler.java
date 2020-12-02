package remote;

import infraCommunication.IClientRequestHandler;
import replica.ClientRequest;
import replica.ReplicaResponse;
import service.interfaces.StoreInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

public class ClientRequestHandler implements IClientRequestHandler {
    private static StoreInterface quebecStore;
    private static StoreInterface britishColumbiaStore;
    private static StoreInterface ontarioStore;

    public ClientRequestHandler() {
        instantiateStoreServers();
    }

    @Override
    public ReplicaResponse handleRequestMessage(ClientRequest clientRequest) {
        switch (clientRequest.getLocation().toLowerCase()) {
            case "qc":
                handleUserAction(clientRequest.getMethod() ,clientRequest.getMethodParameters(), quebecStore);
                break;
            case "on":
                handleUserAction(clientRequest.getMethod() ,clientRequest.getMethodParameters(), ontarioStore);
                break;
            case "bc":
                handleUserAction(clientRequest.getMethod() ,clientRequest.getMethodParameters(), britishColumbiaStore);
                break;

        }
        return null;
    }

    public static ReplicaResponse addItem(StoreInterface store, String managerID, String itemID, String itemName, int quantity, double price) {
        return store.addItem(managerID.toLowerCase(), itemID.toLowerCase(), itemName.toLowerCase(), quantity, price);
    }

    public static ReplicaResponse removeItem(StoreInterface store, String managerID, String itemID, int quantity) {
        return store.removeItem(managerID.toLowerCase(), itemID.toLowerCase(), quantity);
    }

    public static ReplicaResponse listItemAvailability(StoreInterface store, String managerID) {
        return store.listItemAvailability(managerID.toLowerCase());
    }

    public static ReplicaResponse purchaseItem(StoreInterface store, String customerID, String itemID, String dateOfPurchase) {
        return store.purchaseItem(customerID.toLowerCase(), itemID.toLowerCase(), dateOfPurchase);
    }

    public static ReplicaResponse findItem(StoreInterface store, String customerID, String itemID) {
        return store.findItem(customerID.toLowerCase(), itemID.toLowerCase());
    }

    public static ReplicaResponse returnItem(StoreInterface store, String customerID, String itemID, String dateOfReturn) {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse = store.returnItem(customerID.toLowerCase(), itemID.toLowerCase(), dateOfReturn);

        String provinceOfItem = itemID.substring(0, 2);
        if(returnResponse.getResponse().get(customerID.toLowerCase()).contains("Alert: Item does not belong to this store...")) {
            switch (provinceOfItem.toLowerCase()) {
                case "qc":
                    returnResponse = quebecStore.returnItem(customerID, itemID, dateOfReturn);
                    break;
                case "on":
                    returnResponse = ontarioStore.returnItem(customerID, itemID, dateOfReturn);
                    break;
                case "bc":
                    returnResponse = britishColumbiaStore.returnItem(customerID, itemID, dateOfReturn);
                    break;
            }
        }
        return returnResponse;
    }

    public static ReplicaResponse exchangeItem(StoreInterface store, String customerID, String newItemID, String oldItemID, String dateOfReturn) {
        return store.exchange(customerID.toLowerCase(), newItemID.toLowerCase(), oldItemID.toLowerCase(), dateOfReturn);
    }

    private static void instantiateStoreServers() {
        try {
            URL quebecURL = new URL("http://localhost:8082/quebecStore?wsdl");
            QName quebecQName = new QName("http://service/", "StoreImplService");
            Service quebecService = Service.create(quebecURL, quebecQName);
            quebecStore = quebecService.getPort(StoreInterface.class);

            URL ontarioURL = new URL("http://localhost:8081/ontarioStore?wsdl");
            QName ontarioQName = new QName("http://service/", "StoreImplService");
            Service ontarioService = Service.create(ontarioURL, ontarioQName);
            ontarioStore = ontarioService.getPort(StoreInterface.class);

            URL britishColumbiaURL = new URL("http://localhost:8080/britishColumbiaStore?wsdl");
            QName britishColumbiaQName = new QName("http://service/", "StoreImplService");
            Service britishColumbiaService = Service.create(britishColumbiaURL, britishColumbiaQName);
            britishColumbiaStore = britishColumbiaService.getPort(StoreInterface.class);
        }
        catch (Exception e) {
            System.out.println("Hello Client exception: " + e);
            // e.printStackTrace();
        }
    }
}
