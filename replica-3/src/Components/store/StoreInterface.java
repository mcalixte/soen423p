package Components.store;


import replica.ReplicaResponse;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.rmi.RemoteException;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface StoreInterface {
    public abstract ReplicaResponse purchaseItem(String userID, String itemID, String dateOfPurchase) throws RemoteException;
    public abstract ReplicaResponse findItem(String userID, String itemName) throws RemoteException;
    public abstract ReplicaResponse returnItem(String userID, String itemID, String dateOfPurchase) throws RemoteException;
    public abstract ReplicaResponse addItem(String userID, String itemID, String itemName, int quantity, double price) throws RemoteException;
    public abstract ReplicaResponse removeItem(String userID, String itemID, int quantity);
    public abstract ReplicaResponse listItemAvailability(String userID) throws RemoteException;
    public abstract ReplicaResponse exchange(String toLowerCase, String toLowerCase1, String toLowerCase2, String dateOfReturn);
}
