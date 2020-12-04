package Components.store;

import Components.store.item.Item;
import javafx.util.Pair;
import networkEntities.RegisteredReplica;
import replica.ReplicaResponse;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientHelper {

    private String branchID;

    private int quebecPurchaseItemUDPPort = 50000;
    private static int quebecListItemUDPPort = 50001;
    private static int quebecBudgetPort = 50009;
    private static int quebecReturnPort = 50012;
    private static int quebecPurchasePort = 50015;

    private int britishColumbiaPurchaseItemUDPPort = 50002;
    private static int britishColumbiaListItemUDPPort = 50003;
    private static int britishColumbiaBudgetPort = 50010;
    private static int britishColumbiaReturnPort = 50013;
    private static int britishColombiaPurchasePort = 50016;

    private int ontarioPurchaseItemUDPPort = 50004;
    private static int ontarioListItemUDPPort = 50005;
    private static int ontarioBudgetPort = 50011;
    private static int ontarioReturnPort = 50014;
    private static int ontarioPurchasePort = 50017;


    public ClientHelper(String branchID) {
        this.branchID = branchID;
    }

    public synchronized ReplicaResponse purchaseItem(String userID, String itemID, String dateOfPurchaseString, StoreImplementation store) throws ParseException {
        ReplicaResponse replicaResponse = new ReplicaResponse();
        String actionMessage = "";
        double price = 0.00;
        Date dateOfPurchase = new SimpleDateFormat("dd/MM/yyyy").parse(dateOfPurchaseString);
        if (store.getUserPurchaseLog().containsKey(userID) && store.getUserPurchaseLog().get(userID) != null && !store.getUserPurchaseLog().get(userID).values().isEmpty() &&!userID.substring(0, 2).equalsIgnoreCase(branchID)) {
            if (checkBudget(store, userID, price)) {
                System.out.print(store.getUserPurchaseLog().get(userID).values());
                replicaResponse.getResponse().put(userID,"Task UNSUCCESSFUL: Foreign Customer has a foreign item in their possession, can not purchase another. " + userID + ", " + itemID + ", " + dateOfPurchase);
                replicaResponse.setSuccessResult(false);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID + " has already purchased from a different store");
                Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID + " user already purchased from a different store");
            } else {
                replicaResponse.getResponse().put(userID,"Task UNSUCCESSFUL: Customer does not have the funds for this item,"+userID + "," + itemID + "," + dateOfPurchase);
                replicaResponse.setSuccessResult(false);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID);
                Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID);
            }
            return replicaResponse;
        }

        if (store.getInventory().containsKey(itemID) && store.getInventory().get(itemID) != null && store.getInventory().get(itemID).isEmpty()==false) {
            for (Map.Entry<String, ArrayList<Item>> entry : store.getInventory().entrySet())
                if (entry.getKey().equalsIgnoreCase(itemID)&&entry.getKey() != null) {
                    price = entry.getValue().get(0).getPrice();
                    if (checkBudget(store, userID, price)){
                        System.out.println(store.getUserBudgetLog());
                        store.getInventory().get(itemID).remove(0);
                        HashMap<String, Date> purchaseData = new HashMap<String, Date>();
                        purchaseData.put(itemID, dateOfPurchase);
                        store.getUserPurchaseLog().put(userID, purchaseData);
                        replicaResponse.getResponse().put(userID,"Task SUCCESSFUL: Customer purchased Item "+userID + "," + itemID + "," + dateOfPurchase);
                        replicaResponse.setSuccessResult(true);
                        replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                        Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task SUCCESSFUL: Purchase Item to Inventory UserID: "
                                + userID + " ItemID: " + itemID);
                        Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Purchase Item to Inventory UserID: "
                                + userID + " ItemID: " + itemID);
                    } else {
                        replicaResponse.getResponse().put(userID,"Task UNSUCCESSFUL: Customer does not have the funds for this item,"+userID + "," + itemID + "," + dateOfPurchase);
                        replicaResponse.setSuccessResult(false);
                        replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                        Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                                + userID + " ItemID: " + itemID);
                        Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                                + userID + " ItemID: " + itemID);
                    }
                    return replicaResponse;
                }
        } else {
            ReplicaResponse purchaseSuccesful = null;
            if (itemID.toLowerCase().contains("qc") && !branchID.equalsIgnoreCase("QC")) {
                purchaseSuccesful = requestItemOverUDP(quebecPurchaseItemUDPPort, userID, itemID, dateOfPurchaseString);
            } else if (itemID.toLowerCase().contains("on") && !branchID.equalsIgnoreCase("ON")) {
                purchaseSuccesful = requestItemOverUDP(ontarioPurchaseItemUDPPort, userID, itemID, dateOfPurchaseString);
            } else if (itemID.toLowerCase().contains("bc") && !branchID.equalsIgnoreCase("bc")) {
                purchaseSuccesful = requestItemOverUDP(britishColumbiaPurchaseItemUDPPort, userID, itemID, dateOfPurchaseString);
            }
            if (purchaseSuccesful== null) {
                replicaResponse.getResponse().put(userID,"Task UNSUCCESSFUL: However customer added to the waitlist for this item. "+userID + "," + itemID + "," + dateOfPurchase);
                replicaResponse.setSuccessResult(false);
                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
                store.waitList(userID, itemID, dateOfPurchase);
                Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "  >> Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID + "user added to waitlist");
                Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Purchase Item to Inventory UserID: "
                        + userID + " ItemID: " + itemID + "user added to waitlist");
                return replicaResponse;
            }
            for (Map.Entry<String, String> response : purchaseSuccesful.getResponse().entrySet()) {
                actionMessage = response.getValue();
            }
            if(actionMessage.contains("Item was successfully purchased")) {
                HashMap<String, Date> purchaseData = new HashMap<>();
                System.out.println(store.getUserBudgetLog());
                purchaseData.put(itemID, dateOfPurchase);
                store.getUserPurchaseLog().put(userID, purchaseData);

            }
            return purchaseSuccesful;
        }
        return null;
    }

    public synchronized ReplicaResponse findItem(String userID, String itemName,StoreImplementation store) {
        ArrayList<Item> itemList2 = new ArrayList<>();
        StringBuilder foundItems = new StringBuilder();
        foundItems.append(">>>>>>>>>>>> All Items Found <<<<<<<<<<<< \n");


        for (Map.Entry<String, ArrayList<Item>> itemList : store.getInventory().entrySet()) {
            for (Item item : itemList.getValue()) {
                if (item.getItemName().equalsIgnoreCase(itemName)) {
                    itemList2.add(item);
                }
            }
        }
        itemList2.addAll(getRemoteItemsByName(itemName, userID));

        for (Item item : itemList2) {
             foundItems.append("\t" + item.toString() + "\n");;
        }

        ReplicaResponse replicaResponse = new ReplicaResponse();
        replicaResponse.setSuccessResult(true);
        replicaResponse.getResponse().put(userID, foundItems.toString());
        Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + " Task SUCCESSFUL: Find Items Inventory ");
        Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + " Task SUCCESSFUL: Find Items Inventory ");
        return replicaResponse;
    }

    public synchronized ReplicaResponse returnItem(String userID, String itemID, String dateOfReturnString, StoreImplementation store) {
        String actionMessage = "";
        ReplicaResponse replicaResponse = new ReplicaResponse();
        try {
            Date dateOfReturn = new SimpleDateFormat("dd/MM/yyyy").parse(dateOfReturnString);
            if(branchID.equalsIgnoreCase(itemID.substring(0,2))) {
                if (store.getUserPurchaseLog().containsKey(userID) && store.getUserPurchaseLog().get(userID).containsKey(itemID)) {
                    if (isReturnable((store.getUserPurchaseLog().get(userID).get(itemID)), dateOfReturn)) {
                        for (Map.Entry<String, Double> entry : store.getItemLog().entrySet()) {
                            if (entry.getKey().equalsIgnoreCase(itemID)) {
                                double price = entry.getValue();
                                store.getUserReturnLog().put(userID, store.getUserPurchaseLog().get(userID));
                                store.addItem(this.branchID + "1000", itemID, itemID, 1, price);
                                checkBudget(store, userID, -price);
                                replicaResponse.getResponse().put(userID,"Task SUCCESSFUL: Customer "+ userID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n");
                                replicaResponse.setSuccessResult(true);
                                replicaResponse.setReplicaID(RegisteredReplica.ReplicaS1);
                                Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Return Item to Inventory USERID: "
                                        + userID + " ItemID: " + itemID);
                                Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task SUCCESSFUL: Return Item to Inventory USERID: "
                                        + userID + " ItemID: " + itemID);
                                removeUserFromPurchaseLog(userID, itemID, store);
                                updateRemoteCustomerPurchaseLog(userID, itemID, store);
                            }
                        }
                    } else {
                        replicaResponse.getResponse().put(userID,"Task UNSUCCESSFUL: Customer "+ userID+ " returned Item" + itemID+" on "+ dateOfReturn+"\nAlert: Customer has purchased this item in the past, but item purchase date exceeds 30days");
                        replicaResponse.setSuccessResult(false);
                        replicaResponse.setReplicaID(RegisteredReplica.ReplicaS1);
                        Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Return Item to Inventory userID: "
                                + userID + " ItemID: " + itemID);
                        Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Return Item to Inventory userID: "
                                + userID + " ItemID: " + itemID);
                    }
                } else {
                    replicaResponse.getResponse().put(userID,"Task UNSUCCESSFUL: Customer "+ userID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n"+"Alert: Customer has past purchases, but NOT of this item");
                    replicaResponse.setSuccessResult(false);
                    replicaResponse.setReplicaID(RegisteredReplica.ReplicaS1);
                    Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Returned Item" + itemID + "to Inventory");
                    Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNSUCCESSFUL: Returned Item" + itemID + "to Inventory");
                }
                return replicaResponse;
            }
            else {
                if(itemID.toLowerCase().contains("qc")){
                    replicaResponse = requestItemOverUDP(quebecReturnPort,userID, itemID, dateOfReturnString);
                }
                else if(itemID.toLowerCase().contains("on")){
                    replicaResponse = requestItemOverUDP(ontarioReturnPort,userID, itemID, dateOfReturnString);
                }
                else if(itemID.toLowerCase().contains("bc")){
                    replicaResponse = requestItemOverUDP(britishColumbiaReturnPort,userID, itemID, dateOfReturnString);
                }
                return replicaResponse;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return replicaResponse;
    }

    public ReplicaResponse exchangeItem(String userID, String newItemID, String oldItemID, String dateOfExchangeString, StoreImplementation store) {
        ReplicaResponse replicaResponse = new ReplicaResponse();
        String returnItemMessage = "";
        String purchaseItemMessage = "";

        ReplicaResponse response = returnItem(userID, oldItemID, dateOfExchangeString, store);
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnItemMessage = entry.getValue();
        }

        if (returnItemMessage.contains("has been returned")) {
            try {
                ReplicaResponse purchaseResult = purchaseItem(userID, newItemID, dateOfExchangeString, store);
                for (Map.Entry<String, String> entry : purchaseResult.getResponse().entrySet()) {
                    purchaseItemMessage = entry.getValue();
                }
                if (purchaseItemMessage.contains("Item was successfully purchased")) {}
                else {
                    purchaseItem(userID, oldItemID, new SimpleDateFormat("dd/MM/yyyy").format(new Date()), store);
                }
                return purchaseResult;
            } catch (ParseException e) {
            }
        } else {
            return response;
        }
        return replicaResponse;
    }

    private boolean isReturnable(Date dateOfPurchase, Date dateOfReturn) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateOfPurchase);
        calendar.add(Calendar.HOUR, 720);

        Date acceptableLastDayForReturn = calendar.getTime();
        return !dateOfReturn.after(acceptableLastDayForReturn);
    }

    private ReplicaResponse requestItemOverUDP(int storePort, String userID, String itemID, String dateOfPurchase) {
        DatagramSocket socket = null;
        ReplicaResponse purchaseSuccesful = null;
        try {
            socket = new DatagramSocket();
            InetAddress host = InetAddress.getLocalHost();
            byte[] incomingData = new byte[1024];

            StringBuilder requestMessage = new StringBuilder();
            requestMessage.append(userID + "\n");
            requestMessage.append(itemID + "\n");
            requestMessage.append(dateOfPurchase.toString() + "\n");

            byte[] b = requestMessage.toString().getBytes();
            DatagramPacket dp = new DatagramPacket(b, b.length, host, storePort);
            socket.send(dp);

            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length, host, storePort);
            socket.receive(incomingPacket);
            byte[] data = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);

            purchaseSuccesful = (ReplicaResponse) is.readObject();
            for (Map.Entry<String, String> response : purchaseSuccesful.getResponse().entrySet()) {
                if (response.getValue().contains("Item was successfully purchased")) {
                    System.out.println("Item object received and purchased.");
                    return purchaseSuccesful;
                }
            }
            for (Map.Entry<String, String> response : purchaseSuccesful.getResponse().entrySet()) {
                if (response.getValue().contains("has been returned")) {
                    System.out.println(itemID + " : has been returned to its original store.");
                    return purchaseSuccesful;
                }
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return purchaseSuccesful;
    }

    public List<Item> getRemoteItemsByName(String itemName, String userID) {
        String currentProvinceID = this.branchID.toLowerCase();
        List<Item> remotelyReceivedItems = new ArrayList<Item>();

        switch (currentProvinceID) {
            case "on":
                remotelyReceivedItems.addAll(requestRemoteItemList(quebecListItemUDPPort, itemName, userID));
                remotelyReceivedItems.addAll(requestRemoteItemList(britishColumbiaListItemUDPPort, itemName, userID));
                break;
            case "qc":
                remotelyReceivedItems.addAll(requestRemoteItemList(ontarioListItemUDPPort, itemName, userID));
                remotelyReceivedItems.addAll(requestRemoteItemList(britishColumbiaListItemUDPPort, itemName, userID));
                break;
            case "bc":
                remotelyReceivedItems.addAll(requestRemoteItemList(ontarioListItemUDPPort, itemName, userID));
                remotelyReceivedItems.addAll(requestRemoteItemList(quebecListItemUDPPort, itemName, userID));
                break;
        }
        return remotelyReceivedItems;
    }

    private List<Item> requestRemoteItemList(int storePort, String itemName, String userID) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress host = InetAddress.getLocalHost();

            byte[] b = itemName.getBytes();
            DatagramPacket dp = new DatagramPacket(b, b.length, host, storePort);
            socket.send(dp);

            Components.Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNCOMPLETE: Find Item looking at another store");
            Components.Logger.writeStoreLog(branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " Task UNCOMPLETE: Find Item looking at another store:");

            int bufferSize = 1024 * 4;
            byte[] buffer = new byte[bufferSize];
            DatagramPacket reply = new DatagramPacket(buffer, bufferSize, host, storePort);
            socket.receive(reply);


            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            ObjectInputStream is = new ObjectInputStream(in);


            ArrayList<Item> items = null;


            items = (ArrayList<Item>) is.readObject();
            System.out.println("Item List from storeport " + storePort + " has been received");
            return items;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public boolean checkBudget(StoreImplementation store, String userID, Double price) {
        if (store.getUserBudgetLog().containsKey(userID.toLowerCase()) && store.getUserBudgetLog().get(userID.toLowerCase()) - price >= 0) {
            updateRemoteCustomerBudget(userID,price,store);
            return true;
        } else if (1000 - price >= 0 && !store.getUserBudgetLog().containsKey(userID.toLowerCase())) {
            updateRemoteCustomerBudget(userID,price,store);
            return true;
        } else {
            return false;
        }
    }

    public void updateRemoteCustomerBudget(String userID, double price, StoreImplementation store) {
        requestCustomerBudgetUpdate(quebecBudgetPort, userID, price, store);
        requestCustomerBudgetUpdate(ontarioBudgetPort, userID, price, store);
        requestCustomerBudgetUpdate(britishColumbiaBudgetPort, userID, price, store);
    }

    private void requestCustomerBudgetUpdate(int customerBudgetPort, String userID, double price, StoreImplementation store) {
        DatagramSocket serverSocket = null;
        Pair<String,Double> requestMap= null;
        Double userBudget = null;

        if (store.getUserBudgetLog().containsKey(userID.toLowerCase())) {
            userBudget = store.getUserBudgetLog().get(userID) - price;
        }
        else {
            userBudget = 1000.00 - price;
        }
        try {
            serverSocket = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();

            requestMap= new Pair<String,Double>(userID, userBudget);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(requestMap);

            byte[] data = outputStream.toByteArray();
            DatagramPacket dp = new DatagramPacket(data, data.length, ip, customerBudgetPort);
            serverSocket.send(dp);

        } catch (Exception e) {
            System.err.println("Exception " + e);
            System.out.println("Error in sending out the updated customer budget, restart process ....");
        }
    }

    private void removeUserFromPurchaseLog(String userID, String itemID, StoreImplementation store) {
        updateRemoteCustomerPurchaseLog(userID,itemID,store);
        for(Map.Entry<String, HashMap<String, Date>> entry : store.getUserPurchaseLog().entrySet())
            if(entry.getKey().equalsIgnoreCase(userID))
                if(entry.getValue() != null )
                    if(entry.getValue().size() != 0)
                        for(Map.Entry<String, Date> purchaseRecords : entry.getValue().entrySet()) {
                            if(purchaseRecords.getKey().equalsIgnoreCase(itemID))
                                entry.getValue().remove(purchaseRecords);
                            System.out.println(entry.getValue().remove(purchaseRecords.getKey()));
                            System.out.println("User has removed item from purchase logs");
                        }
    }

    public void updateRemoteCustomerPurchaseLog(String userID, String itemID, StoreImplementation store) {
        if(store.branchID.substring(0,2).toLowerCase().contains("qc")) {
            requestCustomerPurchaseUpdate(britishColombiaPurchasePort, userID, itemID);
            requestCustomerPurchaseUpdate(ontarioPurchasePort, userID, itemID);
        }
        if(store.branchID.substring(0,2).toLowerCase().contains("on")) {
            requestCustomerPurchaseUpdate(quebecPurchasePort, userID, itemID);
            requestCustomerPurchaseUpdate(britishColombiaPurchasePort, userID, itemID);
        }
        if(store.branchID.substring(0,2).toLowerCase().contains("bc")){
            requestCustomerPurchaseUpdate(quebecPurchasePort, userID, itemID);
            requestCustomerPurchaseUpdate(ontarioPurchasePort, userID, itemID);
        }
    }

    private void requestCustomerPurchaseUpdate(int customerPurchasePort, String userID, String itemID) {
        DatagramSocket serverSocket = null;
        Pair<String,String> requestMap= null;

        try {
            serverSocket = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();

            requestMap= new Pair<String,String>(userID, itemID);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(requestMap);

            byte[] data = outputStream.toByteArray();
            DatagramPacket dp = new DatagramPacket(data, data.length, ip, customerPurchasePort);
            serverSocket.send(dp);

        } catch (Exception e) {
            System.err.println("Exception " + e);
            System.out.println("Error in sending out the updated customer budget, restart process ....");
        }
    }
}

