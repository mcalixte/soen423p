package Components.store;

import Components.store.item.Item;
import networkEntities.RegisteredReplica;
import replica.ReplicaResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ManagerHelper {

    private String branchID;

    private int quebecPurchaseItemUDPPort = 50000;
    private static int quebecListItemUDPPort = 50001;
    private static int quebecBudgetPort = 50009;

    private int britishColumbiaPurchaseItemUDPPort = 50002;
    private static int britishColumbiaListItemUDPPort = 50003;
    private static int britishColumbiaBudgetPort = 50010;

    private int ontarioPurchaseItemUDPPort = 50004;
    private static int ontarioListItemUDPPort = 50005;
    private static int ontarioBudgetPort = 50011;

    public ManagerHelper(String branchID) {
        this.branchID = branchID;
    }

    public synchronized ReplicaResponse addItem(String userID, String itemID, String itemName, int quantity, double price, StoreImplementation store) {
        ReplicaResponse replicaResponse = new ReplicaResponse();
        Item item = new Item(itemName, itemID, price);
        int index = quantity;
        String actionMessage = "";
        String waitListResponse = "";
        while (index != 0) {
            if (store.getInventory().get(itemID) == null || store.getInventory().get(itemID).size() == 0) {

                actionMessage = "Alert: Item will be added ...";
                ArrayList<Item> itemList = new ArrayList<Item>();
                store.getInventory().put(itemID, itemList);
                store.getInventory().get(itemID).add(item);
                store.getItemLog().put(itemID,price);
                index--;
                System.out.println("Checking wait list");
                Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Add Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID + " ItemName: " + itemName + " Quantity: " + quantity + " Price: " + price);
                Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Add Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID + " ItemName: " + itemName + " Quantity: " + quantity + " Price: " + price);
                if(index==0){
                    waitListResponse = store.checkWaitList(itemID, quantity);
                }
                }
            else if ((store.getInventory().get(itemID).size() > 0 && price == store.getInventory().get(itemID).get(0).getPrice()) || store.getInventory().get(itemID) == null) {
                store.getInventory().get(itemID).add(item);
                index--;
                if (index == 0) {
                    actionMessage = "Alert: Item will be added ...";
                    Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Add Item to Inventory UserID: "
                            + userID + " ItemID: " + itemID + " ItemName: " + itemName + " Quantity: " + quantity + " Price: " + price);
                    Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Add Item to Inventory UserID: "
                            + userID + " ItemID: " + itemID + " ItemName: " + itemName + " Quantity: " + quantity + " Price: " + price);
                    System.out.println("Checking wait list");
                    if(index == 0) {
                        waitListResponse = store.checkWaitList(itemID, quantity);
                    }
                }
            }
            else {
                if(price == store.getInventory().get(itemID).get(0).getPrice())
                {
                    actionMessage = "Alert: Item will not be added, this item does not have the same price as others of its kind ...";
                }
                Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Add Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID + " ItemName: " + itemName + " Quantity: " + quantity + " Price: " + price);
                Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Add Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID + " ItemName: " + itemName + " Quantity: " + quantity + " Price: " + price);

                replicaResponse.getResponse().put(userID, actionMessage+ " " + item.toString()+ ", quantity: "+ quantity +"\n"+waitListResponse);
                replicaResponse.setSuccessResult(false);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                return replicaResponse;
            }
        }
        replicaResponse.getResponse().put(userID, actionMessage+ " " + item.toString()+ ", quantity: "+ quantity +"\n"+waitListResponse);
        replicaResponse.setSuccessResult(true);
        replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
        return replicaResponse;
    }

    public synchronized ReplicaResponse removeItem(String userID, String itemID, int quantity, StoreImplementation store) {
        ReplicaResponse replicaResponse = new ReplicaResponse();
        String actionMessage = "";
        int index = quantity;
        if (quantity == -1 && store.getInventory().containsKey(itemID) == true) {
            store.getInventory().get(itemID).clear();
            System.out.print("All " + itemID + " was removed from inventory");
            Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Remove Item from Inventory ManagerID: "
                    + userID + " ItemID: " + itemID + " Quantity: ALL");
            Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Remove Item from Inventory ManagerID: "
                    + userID + " ItemID: " + itemID + " Quantity: ALL");

            replicaResponse.getResponse().put(userID, "Successful: Completely Remove Item from Inventory ManagerID: "+userID+" ItemID: "+itemID + " Quantity: "+quantity);
            replicaResponse.setSuccessResult(true);
            replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
            return replicaResponse;
        }

        while (index > 0) {
            if (store.getInventory().containsKey(itemID) == true && store.getInventory().get(itemID).size() != 0) {
                store.getInventory().get(itemID).remove(0);
                index--;
                if (index == 0) { ;
                    replicaResponse.getResponse().put(userID, "Successful: Completely Remove Item from Inventory ManagerID: "+userID+" ItemID: "+itemID + " Quantity: "+quantity);
                    replicaResponse.setSuccessResult(true);
                    replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                    Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Remove Item from Inventory ManagerID: "
                            + userID + " ItemID: " + itemID + " Quantity: " + quantity);
                    Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Remove Item from Inventory ManagerID: "
                            + userID + " ItemID: " + itemID + " Quantity: " + quantity);
                }
            }
            else if (store.getInventory().get(itemID).size() == 0) {
                replicaResponse.getResponse().put(userID, "\tTask UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+userID+" ItemID: "+itemID + " Quantity: "+quantity+"\n");
                replicaResponse.setSuccessResult(false);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "
                        + userID + " ItemID: " + itemID + " Quantity: " + quantity);
                Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "
                        + userID + " ItemID: " + itemID + " Quantity: " + quantity);
            }
            else if(store.getInventory().containsKey(itemID) == false){
                replicaResponse.getResponse().put(userID, "\tTask UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+userID+" ItemID: "+itemID + " Quantity: "+quantity+"\n");
                replicaResponse.setSuccessResult(false);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS1);
            }
        }
        return replicaResponse;
    }

    public synchronized ReplicaResponse listItemAvailability(String userID, StoreImplementation store) {
        ReplicaResponse replicaResponse = new ReplicaResponse();
        StringBuilder returnMessage = new StringBuilder("This store contains the following items: \r\n");
        if (store.getInventory() != null) {
            for (Map.Entry<String, ArrayList<Item>> entry : store.getInventory().entrySet()) {
                for (Item item : entry.getValue()) {
                    returnMessage.append(item.toString()+ ", quantity: "+ entry.getValue().size() +"\n");
                    break;
                }
            }
            Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Listing Inventory ManagerID: "
                    + userID);
            Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Listing Inventory ManagerID: "
                    + userID);
        }
        else {
            Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Listing Inventory ManagerID: "
                    + userID);
            Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Listing Inventory ManagerID: "
                    + userID);
        }
        replicaResponse.getResponse().put(userID,  returnMessage.toString());
        replicaResponse.setSuccessResult(true);
        replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
        return replicaResponse;
    }
}
