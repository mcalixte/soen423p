package remote;

import infraCommunication.IClientRequestHandler;
import networkEntities.RegisteredReplica;
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
            URL qcURL = new URL("http://localhost:8083/qcStore?wsdl");
            QName qcQName = new QName("http://implementation.service/", "StoreServerImplService");
            Service qcService = Service.create(qcURL, qcQName);
            quebecStore = qcService.getPort(StoreInterface.class);

            URL onURL = new URL("http://localhost:8084/onStore?wsdl");
            QName onQName = new QName("http://implementation.service/", "StoreServerImplService");
            Service onService = Service.create(onURL, onQName);
            ontarioStore = onService.getPort(StoreInterface.class);

            URL bcURL = new URL("http://localhost:8085/bcStore?wsdl");
            QName bcQName = new QName("http://implementation.service/", "StoreServerImplService");
            Service bcService = Service.create(bcURL, bcQName);
            britishColumbiaStore = bcService.getPort(StoreInterface.class);

        } catch (Exception e) {
            System.out.println("Hello Client exception: " + e);
            // e.printStackTrace();
        }
    }

    @Override
    public ReplicaResponse handleRequestMessage(ClientRequest clientRequest){
        ReplicaResponse replicaResponse = new ReplicaResponse();
        switch (clientRequest.getLocation()) {
            case QUEBEC:
                replicaResponse = handleUserAction(clientRequest, quebecStore);
                break;
            case ONTARIO:
                replicaResponse = handleUserAction(clientRequest, ontarioStore);
                break;
            case BRITISHCOLUMBIA:
                replicaResponse = handleUserAction(clientRequest, britishColumbiaStore);
                break;

        }
        return replicaResponse;
    }

    private ReplicaResponse handleUserAction(ClientRequest clientRequest, StoreInterface store) {
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

    private ReplicaResponse handleManagerMethodInvocation(ClientRequest clientRequest, StoreInterface store)  {
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

    private ReplicaResponse handleCumstomerMethodInvocation(ClientRequest clientRequest, StoreInterface store){
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
                        (String) methodParameters.get(ParameterType.DATEOFRETURN));
                break;
            case EXCHANGE_ITEM:
                replicaResponse = exchange(store,
                        (String) methodParameters.get(ParameterType.CLIENTID),
                        (String) methodParameters.get(ParameterType.NEWITEMID),
                        (String) methodParameters.get(ParameterType.OLDITEMID),
                        (String) methodParameters.get(ParameterType.DATEOFEXCHANGE));
                break;
        }

        return replicaResponse;
    }

    @Override
    public ReplicaResponse addItem(StoreInterface store, String managerID, String itemID, String itemName, int quantity, double price) {
        try {
            return store.addItem(managerID.toLowerCase(), itemID.toLowerCase(), itemName.toLowerCase(), quantity, price);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        HashMap<String, String> strResponse = new HashMap<>();
        strResponse.put(managerID,"Replica2: Unable to process addItem through the ClientRequestHandler");
        ReplicaResponse response = new ReplicaResponse(RegisteredReplica.ReplicaS2,false,-1, strResponse);
        return response;
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
    public ReplicaResponse purchaseItem(StoreInterface store, String customerID, String itemID, String dateOfPurchase) {
        try {
            return store.purchaseItem(customerID.toLowerCase(), itemID.toLowerCase(), dateOfPurchase);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        HashMap<String, String> strResponse = new HashMap<>();
        strResponse.put(customerID,"Replica2: Unable to process purchaseItem through the ClientRequestHandler");
        ReplicaResponse response = new ReplicaResponse(RegisteredReplica.ReplicaS2,false,-1, strResponse);
        return response;
    }

    @Override
    public ReplicaResponse findItem(StoreInterface store, String customerID, String itemName) {
        try {
            return store.findItemRequest(customerID.toLowerCase(), itemName.toLowerCase());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        HashMap<String, String> strResponse = new HashMap<>();
        strResponse.put(customerID,"Replica2: Unable to process findItem through the ClientRequestHandler");
        ReplicaResponse response = new ReplicaResponse(RegisteredReplica.ReplicaS2,false,-1, strResponse);
        return response;
    }

    @Override
    public ReplicaResponse returnItem(StoreInterface store, String customerID, String itemID, String dateOfReturn){
        try {
            return store.returnItem(customerID.toLowerCase(), itemID.toLowerCase(), dateOfReturn);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        HashMap<String, String> strResponse = new HashMap<>();
        strResponse.put(customerID,"Replica2: Unable to process returnItem through the ClientRequestHandler");
        ReplicaResponse response = new ReplicaResponse(RegisteredReplica.ReplicaS2,false,-1, strResponse);
        return response;
    }

    @Override
    public ReplicaResponse exchange(StoreInterface store, String customerID, String newItemID, String oldItemID, String dateOfReturn) {
        try {
            return store.exchangeItem(customerID.toLowerCase(), newItemID.toLowerCase(), oldItemID.toLowerCase(), dateOfReturn);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(Exception e) {
            System.out.println("Not able to do the exchange");
        }

        HashMap<String, String> strResponse = new HashMap<>();
        strResponse.put(customerID,"Replica2: Unable to process exchangeItem through the ClientRequestHandler");
        ReplicaResponse response = new ReplicaResponse(RegisteredReplica.ReplicaS2,false,-1, strResponse);
        return response;
    }

}