package remote;

import infraCommunication.IClientRequestHandler;
import replica.ClientRequest;
import replica.ReplicaResponse;
import replica.enums.ParameterType;
import replica.interfaces.IClient;
import service.interfaces.StoreInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;

public class ClientRequestHandler implements IClientRequestHandler, IClient {
    private static StoreInterface quebecStore;
    private static StoreInterface britishColumbiaStore;
    private static StoreInterface ontarioStore;

    public ClientRequestHandler() {
        instantiateStoreServers();
    }

    @Override
    public void instantiateStoreServers() {
        try {
            URL quebecURL = new URL("http://localhost:8080/quebecStore?wsdl");
            QName quebecQName = new QName("http://service/", "StoreImplService");
            Service quebecService = Service.create(quebecURL, quebecQName);
            quebecStore = quebecService.getPort(StoreInterface.class);

            URL ontarioURL = new URL("http://localhost:8081/ontarioStore?wsdl");
            QName ontarioQName = new QName("http://service/", "StoreImplService");
            Service ontarioService = Service.create(ontarioURL, ontarioQName);
            ontarioStore = ontarioService.getPort(StoreInterface.class);

            URL britishColumbiaURL = new URL("http://localhost:8082/britishColumbiaStore?wsdl");
            QName britishColumbiaQName = new QName("http://service/", "StoreImplService");
            Service britishColumbiaService = Service.create(britishColumbiaURL, britishColumbiaQName);
            britishColumbiaStore = britishColumbiaService.getPort(StoreInterface.class);
        } catch (Exception e) {
            System.out.println("Hello Client exception: " + e);
            // e.printStackTrace();
        }
    }

    @Override
    public ReplicaResponse handleRequestMessage(ClientRequest clientRequest) throws InterruptedException, FileNotFoundException {
        switch (clientRequest.getLocation()) {
            case QUEBEC:
                handleUserAction(clientRequest, quebecStore);
                break;
            case ONTARIO:
                handleUserAction(clientRequest, ontarioStore);
                break;
            case BRITISHCOLUMBIA:
                handleUserAction(clientRequest, britishColumbiaStore);
                break;

        }
        return null;
    }

    private ReplicaResponse handleUserAction(ClientRequest clientRequest, StoreInterface store) throws InterruptedException, FileNotFoundException {
        ReplicaResponse replicaResponse = new ReplicaResponse();
        switch (clientRequest.getUserType()) {
            case CUSTOMER:
                replicaResponse = handleCumstomerMethodInvocation(clientRequest, store);
                break;
            case MANAGER:
                replicaResponse = handleManagerMethodInvocation(clientRequest, store);
                break;
        }

        return replicaResponse;
    }

    private ReplicaResponse handleManagerMethodInvocation(ClientRequest clientRequest, StoreInterface store) throws InterruptedException {
        HashMap<ParameterType, Object> methodParameters = clientRequest.getMethodParameters();
        ReplicaResponse replicaResponse = new ReplicaResponse();
        switch (clientRequest.getMethod()) {

            case ADD_ITEM:
                replicaResponse = addItem(store,
                        (String) methodParameters.get(ParameterType.MANAGERID),
                        (String) methodParameters.get(ParameterType.ITEMID),
                        (String) methodParameters.get(ParameterType.ITEMNAME),
                        (int) methodParameters.get(ParameterType.QUANTITY),
                        (double) methodParameters.get(ParameterType.PRICE));
                break;
            case REMOVE_ITEM:
                replicaResponse = removeItem(store,
                        (String) methodParameters.get(ParameterType.MANAGERID),
                        (String) methodParameters.get(ParameterType.ITEMID),
                        (int) methodParameters.get(ParameterType.QUANTITY));
                break;
            case LIST_ITEMS:
                replicaResponse = listItemAvailability(store,
                        (String) methodParameters.get(ParameterType.MANAGERID));
                break;
        }

        return replicaResponse;
    }

    private ReplicaResponse handleCumstomerMethodInvocation(ClientRequest clientRequest, StoreInterface store) throws InterruptedException, FileNotFoundException {
        HashMap<ParameterType, Object> methodParameters = clientRequest.getMethodParameters();
        ReplicaResponse replicaResponse = new ReplicaResponse();
        switch (clientRequest.getMethod()) {
            case PURCHASE_ITEM:
                replicaResponse =  purchaseItem(store,
                        (String) methodParameters.get(ParameterType.CLIENTID),
                        (String) methodParameters.get(ParameterType.ITEMID),
                        (String) methodParameters.get(ParameterType.DATEOFPURCHASE));
                break;
            case FIND_ITEM:
                replicaResponse =  findItem(store,
                        (String) methodParameters.get(ParameterType.CLIENTID),
                        (String) methodParameters.get(ParameterType.ITEMNAME));
                break;
            case RETURN_ITEM:
                replicaResponse =  returnItem(store,
                        (String) methodParameters.get(ParameterType.CLIENTID),
                        (String) methodParameters.get(ParameterType.ITEMID),
                        (String) methodParameters.get(ParameterType.DATEOFPURCHASE));
                break;
            case EXCHANGE_ITEM:
                replicaResponse =  exchange(store,
                        (String) methodParameters.get(ParameterType.CLIENTID),
                        (String) methodParameters.get(ParameterType.NEWITEMID),
                        (String) methodParameters.get(ParameterType.OLDITEMID),
                        (String) methodParameters.get(ParameterType.DATEOFEXCHANGE));
                break;
        }

        return replicaResponse;
    }



    @Override
    public ReplicaResponse addItem(StoreInterface store, String managerID, String itemID, String itemName, int quantity, double price) throws InterruptedException {
        return store.addItem(managerID.toLowerCase(), itemID.toLowerCase(), itemName.toLowerCase(), quantity, price);
    }

    @Override
    public ReplicaResponse removeItem(StoreInterface store, String managerID, String itemID, int quantity) {
        return store.removeItem(managerID.toLowerCase(), itemID.toLowerCase(), quantity);
    }

    @Override
    public ReplicaResponse listItemAvailability(StoreInterface store, String managerID) {
        return store.listItemAvailability(managerID.toLowerCase());
    }

    @Override
    public ReplicaResponse purchaseItem(StoreInterface store, String customerID, String itemID, String dateOfPurchase) throws InterruptedException {
        return store.runRemotePurchase(customerID.toLowerCase(), itemID.toLowerCase(), dateOfPurchase);
    }

    @Override
    public ReplicaResponse findItem(StoreInterface store, String customerID, String itemName) throws InterruptedException {
        return store.findItemRequest(customerID.toLowerCase(), itemName.toLowerCase());
    }

    @Override
    public ReplicaResponse returnItem(StoreInterface store, String customerID, String itemID, String dateOfReturn) throws FileNotFoundException, InterruptedException {
       return store.returnItem(customerID.toLowerCase(), itemID.toLowerCase(), dateOfReturn);
    }

    @Override
    public ReplicaResponse exchange(StoreInterface store, String customerID, String newItemID, String oldItemID, String dateOfReturn) throws FileNotFoundException, InterruptedException {
        return store.exchangeItem(customerID.toLowerCase(), newItemID.toLowerCase(), oldItemID.toLowerCase(), dateOfReturn);
    }

}