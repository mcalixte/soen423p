package replica.interfaces;

import replica.ReplicaResponse;
import service.interfaces.StoreInterface;

public interface IClient {
    //Manager Invokable methods
    public ReplicaResponse addItem(StoreInterface store, String managerID, String itemID, String itemName, int quantity, double price);
    public ReplicaResponse removeItem(StoreInterface store, String managerID, String itemID, int quantity) ;
    public ReplicaResponse listItemAvailability (StoreInterface store, String managerID);

    //Client Invokable methods
    public ReplicaResponse purchaseItem (StoreInterface store, String customerID, String itemID, String dateOfPurchase);
    public ReplicaResponse findItem (StoreInterface store, String customerID, String itemName);
    public ReplicaResponse returnItem (StoreInterface store, String customerID, String itemID, String dateOfReturn) ;
    public ReplicaResponse exchange(StoreInterface store, String customerID, String newItemID, String oldItemID, String dateOfReturn);

}
