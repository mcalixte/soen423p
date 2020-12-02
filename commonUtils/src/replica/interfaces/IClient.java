package replica.interfaces;

import replica.ReplicaResponse;

public interface IClient {
    //Manager Invokable methods
    public ReplicaResponse addItem(IStoreInterface store, String managerID, String itemID, String itemName, int quantity, double price);
    public ReplicaResponse removeItem(IStoreInterface store, String managerID, String itemID, int quantity) ;
    public ReplicaResponse listItemAvailability (IStoreInterface store, String managerID);

    //Client Invokable methods
    public ReplicaResponse purchaseItem (IStoreInterface store, String customerID, String itemID, String dateOfPurchase);
    public ReplicaResponse findItem (IStoreInterface store, String customerID, String itemName);
    public ReplicaResponse returnItem (IStoreInterface store, String customerID, String itemID, String dateOfReturn) ;
    public ReplicaResponse exchange(IStoreInterface store, String customerID, String newItemID, String oldItemID, String dateOfReturn);

}
