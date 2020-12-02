package replica.interfaces;

import replica.ReplicaResponse;
import service.interfaces.StoreInterface;

import java.io.FileNotFoundException;

public interface IClient {
    //Manager Invokable methods
    public ReplicaResponse addItem(StoreInterface store, String managerID, String itemID, String itemName, int quantity, double price) throws InterruptedException;
    public ReplicaResponse removeItem(StoreInterface store, String managerID, String itemID, int quantity) ;
    public ReplicaResponse listItemAvailability (StoreInterface store, String managerID);

    //Client Invokable methods
    public ReplicaResponse purchaseItem (StoreInterface store, String customerID, String itemID, String dateOfPurchase) throws InterruptedException;
    public ReplicaResponse findItem (StoreInterface store, String customerID, String itemName) throws InterruptedException;
    public ReplicaResponse returnItem (StoreInterface store, String customerID, String itemID, String dateOfReturn) throws FileNotFoundException, InterruptedException;
    public ReplicaResponse exchange(StoreInterface store, String customerID, String newItemID, String oldItemID, String dateOfReturn) throws FileNotFoundException, InterruptedException;

}
