package service.utils.helpers.managerUtils;

import service.StoreImpl;
import service.entities.item.Item;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ManagerUtils {

    public static boolean verifyID(String genericID, String provinceID) {
        return genericID.toLowerCase().replace(" ", "").contains(provinceID.toLowerCase().replace(" ",""));
    }

    public static int getItemQuantity(String itemID, HashMap<String, List<Item>> inventory){
        for(Map.Entry<String, List<Item>> entry : inventory.entrySet()){
            if(entry.getKey().equalsIgnoreCase(itemID))
                return entry.getValue().size();
        }
        return -1; //Meaning this item does not exist in the inventory
    }

    public static boolean customerHasRequiredFunds(String customerID, double price, HashMap<String, Double> customerBudgetLog) {
        boolean customerHasFunds = false;
        for(Map.Entry<String, Double> entry : customerBudgetLog.entrySet())
            if(entry.getKey().equalsIgnoreCase(customerID)) {
                System.out.println("Current customer budget: CustomerID: "+entry.getKey()+" Budget: "+entry.getValue());
                if(entry.getValue() != null && (entry.getValue() - price) >= 0.00)
                    return true;
                else
                    return false;
            }

        customerHasFunds = ! (price > 1000);

        return customerHasFunds;
    }

    public static String addToStock(Item item, HashMap<String, List<Item>> inventory)  {
        String response = "";
        String formattedItemID = item.getItemID().toLowerCase();
        List<Item> itemsInInventoryList = inventory.get(formattedItemID);
        if(itemsInInventoryList != null)
            if(itemsInInventoryList.size() > 0)
                if(itemsInInventoryList.get(0).getPrice() == item.getPrice()) {
                    inventory.get(item.getItemID().toLowerCase()).add(item);
                    response = "Alert: Item will be added ...";
                }
                else
                    response = "Alert: Item will not be added, this item does not have the same price as others of its kind ...";
            else{
                response = "Alert: Item will be added, this item is the first of its kind ...";
                itemsInInventoryList.add(item);
                inventory.put(item.getItemID().toLowerCase(), itemsInInventoryList);
            }
        else{
            response = "Alert: Item will be added ...";
            List<Item> itemList = new ArrayList<>();
            itemList.add(item);
            inventory.put(item.getItemID().toLowerCase(), itemList);
        }
        return response;
    }

    public static String removeSingularItem(String itemID, HashMap<String, List<Item>> inventory) {
        String formattedItemID = itemID.toLowerCase();
        List<Item> items = inventory.get(formattedItemID);
        String itemToBeRemoved = "";

        if (items != null )
            if(items.size() > 0) {
                itemToBeRemoved = items.get(0).toString();
                items.remove(0);
                return itemToBeRemoved;
            }
            else {
                System.out.println("\nThere are no more items of that type left with the itemID: "+itemID+ "\n");
                return "\nThere are no moore items of that type left with the itemID: "+itemID+ "\n";
            }
        else {
            System.out.println("\nAn item of that name does not exist in this store or has been removed completely\n");
            return itemToBeRemoved;
        }
    }

    public static void removeItemTypeFromInventory(String itemID, HashMap<String, List<Item>> inventory) {
        String formattedItemID = itemID.toLowerCase().replace(" ", "");
        if(inventory.get(formattedItemID) != null)
            inventory.remove(formattedItemID);
        else
            System.out.println("\nAn item of that ID does not exist in this store\n");
    }

    public static String listItems(HashMap<String, List<Item>> inventory) {
        StringBuilder returnMessage = new StringBuilder("This store contains the following items: \r\n");
        for(Map.Entry<String, List<Item>> entry : inventory.entrySet()){
            for(Item item : entry.getValue()) {
                returnMessage.append(item.toString()+", quantity: "+ entry.getValue().size() +"\n");
                break;
            }
        }
        return returnMessage.toString();
    }

    public static String handleWaitlistedCustomers(String itemID, double price, StoreImpl store) {
        String waitlistResponse = "";
        for(Map.Entry<String, List<String>> entry : store.getItemWaitList().entrySet()){
            if(itemID.equalsIgnoreCase(entry.getKey())) {
                for(int i = 0; i < entry.getValue().size() ; i++) {
                    if(ManagerUtils.customerHasRequiredFunds(entry.getValue().get(i), price, store.getCustomerBudgetLog())) {
                        HashMap<String, Date> map = new HashMap<>();
                        String dateString = new SimpleDateFormat("mm/dd/yyyy HH:mm").format(new Date());
                        try {
                            map.put(itemID, new SimpleDateFormat("mm/dd/yyyy HH:mm").parse(dateString));
                        } catch (ParseException e) {
                            System.out.println("Unable to purchase item due to a malformed date string... Restart the process of purchasing");
                        }
                        waitlistResponse += "Purchased Item from inventory Customer who was on waitlist: CustomerID:"+entry.getValue().get(i)+" itemID:"+itemID+"\n";
                        store.purchaseItem(entry.getValue().get(i), itemID, dateString);

                        List<HashMap<String, Date>> list = new ArrayList<>();
                        list.add(map);

                        store.getCustomerPurchaseLog().put(entry.getKey(), list);
                        entry.getValue().remove(i); //Remove that User from the waitlist

                        return waitlistResponse;
                    }
                }
            }
        }

        return "";
    }
}
