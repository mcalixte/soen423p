package service.utilities.helpers;

import networkEntities.RegisteredReplica;
import replica.ReplicaResponse;
import service.StoreImpl;
import service.utilities.entities.item.Item;
import service.utilities.helpers.managerUtils.ManagerUtils;

import java.util.HashMap;
import java.util.List;

public class ManagerHelper {
    private String provinceID;

    public ManagerHelper(String provinceID) {
        this.provinceID = provinceID;
    }

    public synchronized ReplicaResponse addItem(String managerID, String itemID, String itemName, int quantity, double price, StoreImpl store) {
        ReplicaResponse replicaResponse = new ReplicaResponse();
        Item item = null;
        String addToStockResponse = "";
        if(ManagerUtils.verifyID(managerID, this.provinceID)) {
            for(int i = 0; i < quantity; i++) {
                item = new Item(itemID, itemName, price);
                addToStockResponse = ManagerUtils.addToStock(item, store.getInventory());
            }
            store.getItemLog().add(item);

            String waitListResponse = ManagerUtils.handleWaitlistedCustomers(itemID, price, store);

            replicaResponse.getResponse().put(managerID, addToStockResponse+ " " + item.toString()+ ", quantity: "+ quantity +"\n"+waitListResponse);
            replicaResponse.setSuccessResult(true);
            replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
            return replicaResponse;
        }
        else {
            replicaResponse.getResponse().put(managerID, "\tThis is a foreign manager, try a proper manager ...");
            replicaResponse.setSuccessResult(false);
            replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
            return replicaResponse;
        }
    }


    public synchronized ReplicaResponse removeItem(String managerID, String itemID, int quantity, HashMap<String, List<Item>> inventory) {
        ReplicaResponse replicaResponse = new ReplicaResponse();

        String item = "";
        if(ManagerUtils.verifyID(managerID, this.provinceID))
            if(quantity != -1)
                if(!(quantity > ManagerUtils.getItemQuantity(itemID, inventory))){
                    for(int i = 0; i < quantity ; i++)
                        item = ManagerUtils.removeSingularItem(itemID, inventory);

                    replicaResponse.getResponse().put(managerID,  "Successful: Completely Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity);
                    replicaResponse.setSuccessResult(true);
                    replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                    return replicaResponse;
                }
                else {
                    System.out.println("\nAlert: Can not remove items greater then its availability\n");

                    replicaResponse.getResponse().put(managerID, "\tTask UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity+"\n");
                    replicaResponse.setSuccessResult(false);
                    replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                    return replicaResponse;
                }

            else {
                ManagerUtils.removeItemTypeFromInventory(itemID, inventory);
                replicaResponse.getResponse().put(managerID, "Successful: Completely Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity);
                replicaResponse.setSuccessResult(true);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                return replicaResponse;
            }
        else {
            System.out.println("\nALERT: You are not permitted to do this action on this store\n");
            replicaResponse.getResponse().put(managerID, "Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity+"\nALERT: You are not permitted to do this action on this store\n");
            replicaResponse.setSuccessResult(false);
            replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
            return replicaResponse;
        }
    }

    public synchronized ReplicaResponse listItemAvailability(String managerID, HashMap<String, List<Item>> inventory) {
        ReplicaResponse replicaResponse = new ReplicaResponse();
        String returnMessage;

        if(ManagerUtils.verifyID(managerID, this.provinceID)) {
            replicaResponse.getResponse().put(managerID,  ManagerUtils.listItems(inventory));
            replicaResponse.setSuccessResult(true);
            replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
        }
        else {
            returnMessage = "\nALERT: You are not permitted to do this action on this store\n";
            replicaResponse.getResponse().put(managerID, returnMessage);
            replicaResponse.setSuccessResult(true);
            replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
        }
        return replicaResponse;
    }

}