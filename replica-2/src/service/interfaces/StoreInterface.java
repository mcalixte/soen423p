package service.interfaces;

import replica.ReplicaResponse;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.io.FileNotFoundException;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface StoreInterface {

    ReplicaResponse addItem (String managerID, String itemID, String itemName, int quantity, double price) throws InterruptedException;

    ReplicaResponse removeItem (String managerID, String itemID, int quantity);

    ReplicaResponse listItemAvailability(String managerID);

    ReplicaResponse purchaseItem (String customerID, String itemID, String dateOfPurchase) throws InterruptedException;

    ReplicaResponse runRemotePurchase(String customerID, String itemID, String dateOfPurchase) throws InterruptedException;

    String findItem (String customerID, String itemName);

    ReplicaResponse findItemRequest(String customerID, String itemName) throws InterruptedException;

    ReplicaResponse returnItem(String customerID, String itemID, String dateOfReturn) throws FileNotFoundException, InterruptedException;

    ReplicaResponse exchangeItem (String customerID, String newItemID, String oldItemID, String dateOfExchange) throws InterruptedException, FileNotFoundException;

    String showWaitlist();

    String showCustomerLog();

    double getItemPrice(String itemID);

    double getRemotePrice(String itemID) throws InterruptedException;

    String addToWaitlist(String customerID, String itemId, String dateOfPurchase) throws InterruptedException;

    String isManager(String managerID);

    String getStore();
}
