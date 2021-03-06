import infraCommunication.OperationCode;
import replica.ClientRequest;
import replica.ReplicaResponse;
import replica.enums.Location;
import replica.enums.ParameterType;
import replica.enums.UserType;

public class FrontEndHelper {

    public ClientRequest createAddItem (String managerID, String itemID, String itemName, int quantity, double price){
        String managerPrefix = managerID.substring(0, 2);
        ClientRequest request = null;
        if (managerPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.ADD_ITEM, Location.QUEBEC, UserType.MANAGER);
        }
        if (managerPrefix.equalsIgnoreCase("bc")) {
            request = new ClientRequest(OperationCode.ADD_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if (managerPrefix.equalsIgnoreCase("on")) {
            request = new ClientRequest(OperationCode.ADD_ITEM, Location.ONTARIO, UserType.MANAGER);
        }


        request.addRequestDataEntry(ParameterType.MANAGERID, managerID);
        request.addRequestDataEntry(ParameterType.ITEMID, itemID);
        request.addRequestDataEntry(ParameterType.ITEMNAME, itemName);
        request.addRequestDataEntry(ParameterType.QUANTITY, quantity);
        request.addRequestDataEntry(ParameterType.PRICE, price);

        return request;

    }

    public ClientRequest createRemoveItem(String managerID, String itemID, int quantity) {
        String managerPrefix = managerID.substring(0, 2);
        ClientRequest request = null;
        if (managerPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.REMOVE_ITEM, Location.QUEBEC, UserType.MANAGER);
        }
        if (managerPrefix.equalsIgnoreCase("bc")) {
            request = new ClientRequest(OperationCode.REMOVE_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if (managerPrefix.equalsIgnoreCase("on")) {
            request = new ClientRequest(OperationCode.REMOVE_ITEM, Location.ONTARIO, UserType.MANAGER);
        }

        request.addRequestDataEntry(ParameterType.MANAGERID, managerID);
        request.addRequestDataEntry(ParameterType.ITEMID, itemID);
        request.addRequestDataEntry(ParameterType.QUANTITY, quantity);

        return request;
    }

    public ClientRequest createListItems(String managerID) {
        String managerPrefix = managerID.substring(0, 2);
        ClientRequest request = null;
        if (managerPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.LIST_ITEMS, Location.QUEBEC, UserType.MANAGER);
        }
        if (managerPrefix.equalsIgnoreCase("bc")) {
            request = new ClientRequest(OperationCode.LIST_ITEMS, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if (managerPrefix.equalsIgnoreCase("on")) {
            request = new ClientRequest(OperationCode.LIST_ITEMS, Location.ONTARIO, UserType.MANAGER);
        }

        request.addRequestDataEntry(ParameterType.MANAGERID, managerID);
        return request;
    }

    public ClientRequest createPurchaseItems(String customerID, String itemID, String dateOfPurchase) {
        String userPrefix = customerID.substring(0, 2);
        ClientRequest request = null;
        if (userPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.PURCHASE_ITEM, Location.QUEBEC, UserType.CUSTOMER);
        }
        if (userPrefix.equalsIgnoreCase("bc")) {
            request = new ClientRequest(OperationCode.PURCHASE_ITEM, Location.BRITISHCOLUMBIA, UserType.CUSTOMER);
        }
        if (userPrefix.equalsIgnoreCase("on")) {
            request = new ClientRequest(OperationCode.PURCHASE_ITEM, Location.ONTARIO,UserType.CUSTOMER);
        }

        request.addRequestDataEntry(ParameterType.CLIENTID, customerID);
        request.addRequestDataEntry(ParameterType.ITEMID, itemID);
        request.addRequestDataEntry(ParameterType.DATEOFPURCHASE, dateOfPurchase);

        return request;
    }

    public ClientRequest createReturnItems(String customerID, String itemID, String dateOfReturn) {
        String userPrefix = customerID.substring(0, 2);
        ClientRequest request = null;
        if (userPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.RETURN_ITEM, Location.QUEBEC, UserType.CUSTOMER);
        }
        if (userPrefix.equalsIgnoreCase("bc")) {
            request = new ClientRequest(OperationCode.RETURN_ITEM, Location.BRITISHCOLUMBIA, UserType.CUSTOMER);
        }
        if (userPrefix.equalsIgnoreCase("on")) {
            request = new ClientRequest(OperationCode.RETURN_ITEM, Location.ONTARIO, UserType.CUSTOMER);
        }

        request.addRequestDataEntry(ParameterType.CLIENTID, customerID);
        request.addRequestDataEntry(ParameterType.ITEMID, itemID);
        request.addRequestDataEntry(ParameterType.DATEOFRETURN, dateOfReturn);

        return request;
    }

    public ClientRequest createFindItem(String customerID, String itemName) {
        String userPrefix = customerID.substring(0, 2);
        ClientRequest request = null;
        if (userPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.FIND_ITEM, Location.QUEBEC, UserType.CUSTOMER);
        }
        if (userPrefix.equalsIgnoreCase("bc")) {
            request = new ClientRequest(OperationCode.FIND_ITEM, Location.BRITISHCOLUMBIA, UserType.CUSTOMER);
        }
        if (userPrefix.equalsIgnoreCase("on")) {
            request = new ClientRequest(OperationCode.FIND_ITEM, Location.ONTARIO,UserType.CUSTOMER);
        }

        request.addRequestDataEntry(ParameterType.CLIENTID, customerID);
        request.addRequestDataEntry(ParameterType.ITEMNAME, itemName);
        return request;
    }

    public ClientRequest createExchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        String userPrefix = customerID.substring(0, 2);
        ClientRequest request = null;
        if (userPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.EXCHANGE_ITEM, Location.QUEBEC, UserType.CUSTOMER);
        }
        if (userPrefix.equalsIgnoreCase("bc")) {
            request = new ClientRequest(OperationCode.EXCHANGE_ITEM, Location.BRITISHCOLUMBIA,UserType.CUSTOMER);
        }
        if (userPrefix.equalsIgnoreCase("on")) {
            request = new ClientRequest(OperationCode.EXCHANGE_ITEM, Location.ONTARIO,UserType.CUSTOMER);
        }

        request.addRequestDataEntry(ParameterType.CLIENTID, customerID);
        request.addRequestDataEntry(ParameterType.NEWITEMID, newItemID);
        request.addRequestDataEntry(ParameterType.OLDITEMID, oldItemID);
        request.addRequestDataEntry(ParameterType.DATEOFEXCHANGE, dateOfExchange);

        return request;
    }
}