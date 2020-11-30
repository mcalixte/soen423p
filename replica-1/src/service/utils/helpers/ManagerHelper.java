package service.utils.helpers;

import service.StoreImpl;
import service.entities.item.Item;
import service.utils.helpers.managerUtils.ManagerUtils;

import java.util.*;

public class ManagerHelper {
    private String provinceID;

    public ManagerHelper(String provinceID) {
        this.provinceID = provinceID;
    }

    public synchronized String addItem(String managerID, String itemID, String itemName, int quantity, double price, StoreImpl store) {
        Item item = null;
        String addToStockResponse = "";
        if(ManagerUtils.verifyID(managerID, this.provinceID)){
            for(int i = 0; i < quantity; i++){
                item = new Item(itemID, itemName, price);
                addToStockResponse = ManagerUtils.addToStock(item, store.getInventory());
            }
            store.getItemLog().add(item);

            String waitListResponse = ManagerUtils.handleWaitlistedCustomers(itemID, price, store);

            return addToStockResponse+"\n"+item.toString()+"\n"+waitListResponse;
        }
        else {
            return "This is a foreign manager, try a proper manager ...";
        }
    }




    public synchronized String removeItem(String managerID, String itemID, int quantity, HashMap<String, List<Item>> inventory) {
        String item = "";
        if(ManagerUtils.verifyID(managerID, this.provinceID))
            if(quantity != -1)
                if(!(quantity > ManagerUtils.getItemQuantity(itemID, inventory))){
                    for(int i = 0; i < quantity ; i++)
                        item = ManagerUtils.removeSingularItem(itemID, inventory);

                    return item;
                }
                else {
                    System.out.println("\nAlert: Can not remove items greater then its availability\n");

                    return "Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity+"\nAlert: Can not remove items greater then its availability\n";
                }

            else {
                ManagerUtils.removeItemTypeFromInventory(itemID, inventory);

                return "Successful: Completely Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;

            }

        else {
            System.out.println("\nALERT: You are not permitted to do this action on this store\n");

            return "Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity+"\nALERT: You are not permitted to do this action on this store\n";
        }
    }

    public synchronized String listItemAvailability(String managerID, HashMap<String, List<Item>> inventory) {
        String returnMessage;
        if(ManagerUtils.verifyID(managerID, this.provinceID)) {
            returnMessage = ManagerUtils.listItems(inventory);
            System.out.println(returnMessage);

        }
        else {
            returnMessage = "\nALERT: You are not permitted to do this action on this store\n";
            System.out.println(returnMessage);
        }
        return returnMessage;
    }

}