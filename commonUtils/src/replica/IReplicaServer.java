package replica;

public interface IReplicaServer {
    //Manager Invokable methods
    public ReplicaResponse addItem(String managerID, String itemID, String itemName, int quantity, double price);
    public ReplicaResponse removeItem(String managerID, String itemID, int quantity) ;
    public ReplicaResponse listItemAvailability (String managerID);

    //Client Invokable methods
    public ReplicaResponse purchaseItem (String customerID, String itemID, String dateOfPurchase);
    public ReplicaResponse findItem (String customerID, String itemName);
    public ReplicaResponse returnItem (String customerID, String itemID, String dateOfReturn) ;
    public ReplicaResponse exchange(String customerID, String newItemID, String oldItemID, String dateOfReturn);

    public void requestUpdateOfCustomerBudgetLog(String customerID, double price);
    public boolean waitList(String customerID, String itemID, String dateOfPurchase);
}
