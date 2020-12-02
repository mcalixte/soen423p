package Components.store;

import Components.store.item.Item;
import replica.ReplicaResponse;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface StoreInterface {
    public abstract ReplicaResponse purchaseItem(String userID, String itemID, String dateOfPurchase) throws RemoteException;
    public abstract ReplicaResponse findItem(String userID, String itemName) throws RemoteException;
    public abstract ReplicaResponse returnItem(String userID, String itemID, String dateOfPurchase) throws RemoteException;
    public abstract ReplicaResponse addItem(String userID, String itemID, String itemName, int quantity, double price) throws RemoteException;
    public abstract ReplicaResponse removeItem(String userID, String itemID, int quantity);
    public abstract ReplicaResponse listItemAvailability(String userID) throws RemoteException;
}
