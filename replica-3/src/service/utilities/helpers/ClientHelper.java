package service.utilities.helpers;

import networkEntities.RegisteredReplica;
import replica.ReplicaResponse;
import service.StoreImpl;
import service.utilities.entities.item.Item;
import service.utilities.date.DateUtils;
import service.utilities.helpers.clientUtils.ClientUtils;
import service.utilities.helpers.managerUtils.ManagerUtils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientHelper {

    private String provinceID;
    public ClientHelper(String provinceID) {
        this.provinceID = provinceID;
    }

    public synchronized ReplicaResponse purchaseItem(String customerID, String itemID, String dateOfPurchase, StoreImpl store) {
        //TODO User can be added to a waitlist whether they have money or not for an item, but if purchase fails, try to give the item to any other person waiting and so on.
        //TODO ... If it doesn't succeed do nothing

        //TODO When the item amount is zero, DO NOT REMOVE COMPLETELY FROM STORE
        ReplicaResponse replicaResponse = new ReplicaResponse();
        Date dateOfPurchaseDateObject =  DateUtils.createDateFromString(dateOfPurchase);
        Boolean isItemSuccessfullyPurchased = false;
        String purchasedItem;
        String response;
        if (!ClientUtils.verifyID(customerID, this.provinceID))
            if (customerHasForeignItem(customerID, itemID, store)) {
                replicaResponse.getResponse().put(customerID,"Task UNSUCCESSFUL: Foreign Customer has a foreign item in their possession, can not purchase another. " + customerID + ", " + itemID + ", " + dateOfPurchase);
                replicaResponse.setSuccessResult(false);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                return replicaResponse;
            }

        double price = getSpecificItemPrice(itemID, store);

        if(price != -1) {
            if (!ClientUtils.customerHasRequiredFunds(customerID, price, store.getCustomerBudgetLog())) {
                replicaResponse.getResponse().put(customerID,"Task UNSUCCESSFUL: Customer does not have the funds for this item,"+customerID + "," + itemID + "," + dateOfPurchase);
                replicaResponse.setSuccessResult(isItemSuccessfullyPurchased);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                return replicaResponse;
            }
        }

        purchasedItem = ClientUtils.purchaseSingularItem(itemID, store.getInventory());
        isItemSuccessfullyPurchased = purchasedItem.equalsIgnoreCase("An item of that name does not exist in this store or has been removed") ? false : true;

        if (isItemSuccessfullyPurchased) {
            if (store.getCustomerBudgetLog().containsKey(customerID.toLowerCase())) {
                store.getCustomerBudgetLog().put(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()) - price);

                Item item = new Item(itemID, getSpecificItemName(itemID, store), getSpecificItemPrice(itemID, store));

                if( store.getCustomerItemDetention().containsKey(customerID.toLowerCase()))
                    store.getCustomerItemDetention().get(customerID).add(item);
                else {
                    List<Item> itemList = new ArrayList<>();
                    itemList.add(item);
                    store.getCustomerItemDetention().put(customerID.toLowerCase(), itemList);
                }


                updateCustomerPurchaseLog(customerID, itemID, store, dateOfPurchaseDateObject);
                store.requestUpdateOfCustomerBudgetLog(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()));
            }
            else {
                Double budget = 1000.00 - price;
                store.getCustomerBudgetLog().put(customerID.toLowerCase(), budget);

                Item item = new Item(itemID, getSpecificItemName(itemID, store), getSpecificItemPrice(itemID, store));

                if( store.getCustomerItemDetention().containsKey(customerID.toLowerCase()))
                    store.getCustomerItemDetention().get(customerID).add(item);
                else {
                    List<Item> itemList = new ArrayList<>();
                    itemList.add(item);
                    store.getCustomerItemDetention().put(customerID.toLowerCase(), itemList);
                }

                updateCustomerPurchaseLog(customerID, itemID, store, dateOfPurchaseDateObject);
                store.requestUpdateOfCustomerBudgetLog(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()));
            }

            replicaResponse.getResponse().put(customerID,"Task SUCCESSFUL: Customer purchased Item "+customerID + "," + itemID + "," + dateOfPurchase );
            replicaResponse.setSuccessResult(isItemSuccessfullyPurchased);
            replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
            return replicaResponse;
        } else {
            if(itemID.contains(this.provinceID.toLowerCase())) {
                store.waitList(customerID, itemID, dateOfPurchase);

                replicaResponse.getResponse().put(customerID,"Task UNSUCCESSFUL: However customer added to the waitlist for this item. "+customerID + "," + itemID + "," + dateOfPurchase);
                replicaResponse.setSuccessResult(isItemSuccessfullyPurchased);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                return replicaResponse;
            }
            else{
                replicaResponse = ClientUtils.requestItemFromCorrectStore(customerID, itemID, dateOfPurchase, this.provinceID);

                return replicaResponse;
            }
        }
    }

    private boolean customerHasForeignItem(String customerID, String itemID, StoreImpl store) {
        String customerProvince = customerID.substring(0, 2);
        boolean customerHasForeignItem = false;
        for(Map.Entry<String, List<Item>> entry : store.getCustomerItemDetention().entrySet())
            if(entry.getKey().equalsIgnoreCase(customerID))
                for(Item item : entry.getValue())
                    if(!item.getItemID().substring(0, 2).equalsIgnoreCase(customerProvince))
                        customerHasForeignItem = true;

        return customerHasForeignItem;
    }

    private void updateCustomerPurchaseLog(String customerID, String itemID, StoreImpl store, Date dateOfPurchaseDateObject) {
        HashMap<String, Date> itemIDandDateOfPurchase = new HashMap<>();
        itemIDandDateOfPurchase.put(itemID, dateOfPurchaseDateObject);
        String formattedCustomerID = customerID.toLowerCase();
        if(store.getCustomerPurchaseLog().containsKey(formattedCustomerID))
            if(store.getCustomerPurchaseLog().get(formattedCustomerID) != null)
                store.getCustomerPurchaseLog().get(formattedCustomerID).add(itemIDandDateOfPurchase);
            else{
                List<HashMap<String, Date>> list = new ArrayList<>();
                list.add(itemIDandDateOfPurchase);
                store.getCustomerPurchaseLog().put(formattedCustomerID, list);
            }
        else {
            List<HashMap<String, Date>> list = new ArrayList<>();
            list.add(itemIDandDateOfPurchase);
            store.getCustomerPurchaseLog().put(formattedCustomerID, list);
        }
    }

    private double getSpecificItemPrice(String itemID, StoreImpl store) {
        double price = 0.00;
        for (Item item : store.getItemLog())
            if (item.getItemID().equalsIgnoreCase(itemID))
                price = item.getPrice();

        return price;
    }

    private String getSpecificItemName(String itemID, StoreImpl store) {
        String itemName = "";
        for (Item item : store.getItemLog())
            if (item.getItemID().equalsIgnoreCase(itemID))
                itemName = item.getItemName();

        return itemName;
    }
    public synchronized ReplicaResponse findItem(String customerID, String itemName, HashMap<String, List<Item>> inventory) {
        List<Item> locallyFoundItems = new ArrayList<>();
        HashMap<String, List<Item>> remotelyFoundItems = new HashMap<>();

        locallyFoundItems = getItemsByName(itemName, inventory);
        remotelyFoundItems = ClientUtils.getRemoteItemsByName(itemName, this.provinceID);
        List<Item> allFoundItems = ClientUtils.mergeAllFoundItems(locallyFoundItems, remotelyFoundItems);

        StringBuilder logString = new StringBuilder();
        logString.append(">>>>>>>>>>>> All Items Found <<<<<<<<<<<< \n");
        StringBuilder foundItems = new StringBuilder();

        List<Item> allFoundItemsWithoutDuplicates = new ArrayList<>(
                new HashSet<>(allFoundItems));

        for (Item item : allFoundItemsWithoutDuplicates)
            foundItems.append("\t" + item.toString()+ " "+ findAmountOfItems(item, allFoundItems) + "\n" );


        //Build out the ReplicaResponse
        ReplicaResponse replicaResponse = new ReplicaResponse();
        replicaResponse.setSuccessResult(true);
        replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
        replicaResponse.getResponse().put(customerID, foundItems.toString());
//        if (allFoundItems != null)
//            if (allFoundItems.size() == 0)
//                logString.append(">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task SUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + ". HOWEVER, No items found.");
//
//            else
//                logString.append(">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task SUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + "." + allFoundItems.size() + " item(s) found.");
//        else
//            logString.append(">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + ". No items found.");

        return replicaResponse;
    }

    private int findAmountOfItems(Item item, List<Item> allFoundItems) {
        int amount = 0;
        for(Item itemType : allFoundItems)
            if(itemType.getItemID().equalsIgnoreCase(item.getItemID()))
                amount++;

        return amount;
    }


    public synchronized ReplicaResponse returnItem(String customerID, String itemID, String dateOfReturn, StoreImpl store) {
        Date dateOfReturnDate = null;
        ReplicaResponse replicaResponse = new ReplicaResponse();

        try {
            dateOfReturnDate = new SimpleDateFormat("dd/mm/yyyy").parse(dateOfReturn);
        } catch (ParseException e) {
           // e.printStackTrace();
        }

        if (ClientUtils.verifyID(itemID, this.provinceID))
            if (store.getCustomerPurchaseLog().containsKey(customerID.toLowerCase()))
                if (store.getCustomerPurchaseLog().get(customerID.toLowerCase()) != null) {
                    Date dateOfPurchase = findDateFromCustomerPurchaseLog(customerID, itemID, store);
                    System.out.println("MKC1: dateOfPurchase: "+dateOfPurchase);
                    if (ClientUtils.isItemReturnWorthy(dateOfPurchase, dateOfReturn, itemID)) {
                        HashMap<String, Date> map = new HashMap<>();
                        map.put(itemID, dateOfReturnDate);
                        store.getCustomerReturnLog().put(customerID, map);
                        ClientUtils.returnItemToInventory(itemID, store.getItemLog(), store.getInventory());
                        removeCustomerFromPurchaseLog(customerID, itemID, store);
                        removeItemFromCustomerItemDetention(customerID, itemID, store);

                        //Handle any waitlisted customers
                        String waitlistReplicaResponse = "";
                        Item itemFromItemLog;
                        for(Item item : store.getItemLog()) {
                            if(item.getItemID().equalsIgnoreCase(itemID))
                                waitlistReplicaResponse = ManagerUtils.handleWaitlistedCustomers(itemID, item.getPrice(), store);
                        }

                        //TODO APPEND PURCHASE ON WAITLIST STRING **
                        //TODO ASK USER IF THEY WANT TO BE PUT ON WAITLIST **
                        //TODO Change the 'C' for 'U' in the customer ID

                        Double price = 0.00;
                        for(Item item : store.getItemLog())
                            if(item.getItemID().equalsIgnoreCase(itemID))
                                price = item.getPrice();

                        store.getCustomerBudgetLog().put(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()) + price);
                        store.requestUpdateOfCustomerBudgetLog(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()));

                        String itemIDToReturn;
                        itemIDToReturn = store.getInventory().get(itemID) != null &&  store.getInventory().get(itemID).size() > 0 ? itemID : "";

                        replicaResponse.getResponse().put(customerID,"Task SUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n");
                        replicaResponse.setSuccessResult(true);
                        replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                        return replicaResponse;
                    } else {
                        System.out.println("Alert: Customer has purchased this item in the past, but item purchase date exceeds 30days");

                        String itemIDToReturn;
                        itemIDToReturn = store.getInventory().get(itemID) != null &&  store.getInventory().get(itemID).size() > 0 ? itemID : "";

                        replicaResponse.getResponse().put(customerID,"Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\nAlert: Customer has purchased this item in the past, but item purchase date exceeds 30days");
                        replicaResponse.setSuccessResult(false);
                        replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                        return replicaResponse;
                    }
                } else {
                    System.out.println("Alert: Customer has past purchases, but NOT of this item");

                    replicaResponse.getResponse().put(customerID,"Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n"+"Alert: Customer has past purchases, but NOT of this item");
                    replicaResponse.setSuccessResult(false);
                    replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                    return replicaResponse;
                }
            else {
                System.out.println("Alert: Customer has no record of past purchases");
                replicaResponse.getResponse().put(customerID,"Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n"+"Alert: Customer has past purchases, but NOT of this item");
                replicaResponse.setSuccessResult(false);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                return replicaResponse;
            }
        else {
            System.out.println("Alert: Item does not belong to this store...");
            replicaResponse = ClientUtils.returnItemToCorrectStore(customerID, itemID, dateOfReturn, provinceID);
            return replicaResponse;
        }
    }

    private void removeItemFromCustomerItemDetention(String customerID, String itemID, StoreImpl store) {
        for(Map.Entry<String, List<Item>> entry : store.getCustomerItemDetention().entrySet())
            if(entry.getKey().equalsIgnoreCase(customerID))
                for(int i = 0 ; i < entry.getValue().size() ; i++)
                    if(entry.getValue().get(i).getItemID().equalsIgnoreCase(itemID))
                        entry.getValue().remove(i);
    }

    private Date findDateFromCustomerPurchaseLog(String customerID, String itemID, StoreImpl store) {
        String dateOfPurchase = new SimpleDateFormat("dd/mm/yyyy").format(new Date());
        Date dateOfPurchaseDate = new Date();
        try {
            dateOfPurchaseDate = new SimpleDateFormat("dd/mm/yyyy").parse(dateOfPurchase);
        } catch (ParseException e) {
           // e.printStackTrace();
        }

        for(Map.Entry<String, List<HashMap<String, Date>>> entry : store.getCustomerPurchaseLog().entrySet())
            if(entry.getKey().equalsIgnoreCase(customerID.toLowerCase()))
                if(entry.getValue() != null)
                    if(entry.getValue().size() > 0)
                        for(int i = 0; i <= entry.getValue().size() - 1; i++)
                            if(entry.getValue().get(i).containsKey(itemID.toLowerCase()))
                                return entry.getValue().get(i).get(itemID.toLowerCase());
        System.out.println("Returning todays date:"+dateOfPurchaseDate);
        return dateOfPurchaseDate;
    }

    private void removeCustomerFromPurchaseLog(String customerID, String itemID, StoreImpl store) {
        for(Map.Entry<String, List<HashMap<String, Date>>> entry : store.getCustomerPurchaseLog().entrySet())
            if(entry.getKey().equalsIgnoreCase(customerID))
                if(entry.getValue() != null )
                    if(entry.getValue().size() != 0)
                        for(int i = 0; i < entry.getValue().size(); i++)
                            if(entry.getValue().get(i).containsKey(itemID))
                                entry.getValue().remove(i);
                    else if(entry.getValue().size() == 0)
                        store.getCustomerPurchaseLog().remove(customerID);
                else if(entry.getValue() != null )
                    store.getCustomerPurchaseLog().remove(customerID);
    }

    public List<Item> getItemsByName(String itemName, HashMap<String, List<Item>> inventory) { List<Item> itemsWithSameName = new ArrayList<>();
        for(Map.Entry<String, List<Item>> entry : inventory.entrySet()){
            for(Item item : entry.getValue()){
                if(item.getItemName().equalsIgnoreCase(itemName))
                    itemsWithSameName.add(item);
            }
        }
        return itemsWithSameName;
    }


    ////////////////////////////////////
    ///     UDP Related Methods      ///
    ////////////////////////////////////
    public void sendCustomerBudgetUpdate(int customerBudgetPort, String customerID, double budget, StoreImpl store) {
        DatagramSocket serverSocket = null;
        HashMap<String, Double> requestMap = new HashMap<>();
        Double updatedCustomerBudget = budget;

        try
        {
            serverSocket = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();

            requestMap.put(customerID, updatedCustomerBudget);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(requestMap);

            byte[] data = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length , ip, customerBudgetPort);
            serverSocket.send(sendPacket); //TODO

        }
        catch(Exception e)
        {
            System.err.println("Exception " + e);
            System.out.println("Error in sending out the updated customer budget, restart process ....");
        }

    }
}
