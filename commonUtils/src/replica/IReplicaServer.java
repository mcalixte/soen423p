package replica;

public interface IReplicaServer {
    //Manager Invokable methods
    public String addItem(String managerID, String itemID, String itemName, int quantity, double price);
    public String removeItem(String managerID, String itemID, int quantity) ;
    public String listItemAvailability (String managerID);

    //Client Invokable methods
    public String purchaseItem (String customerID, String itemID, String dateOfPurchase);
    public String findItem (String customerID, String itemName);
    public String returnItem (String customerID, String itemID, String dateOfReturn) ;
    public String exchange(String customerID, String newItemID, String oldItemID, String dateOfReturn);

    public void requestUpdateOfCustomerBudgetLog(String customerID, double price);
    public boolean waitList(String customerID, String itemID, String dateOfPurchase);
}
