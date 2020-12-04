package Components.store;

import Components.*;
import Components.Logger;
import Components.store.item.Item;
import javafx.util.Pair;
import org.omg.CORBA.ORB;
import replica.ReplicaResponse;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(endpointInterface = "Components.store.StoreInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class StoreImplementation implements StoreInterface {
    String branchID;
    HashMap<String, ArrayList<Item>> inventory = new HashMap<>();

    private HashMap<String, HashMap<String, Date>> userPurchaseLog = new HashMap<>();
    private HashMap<String, HashMap<String, Date>> userReturnLog = new HashMap<>();
    private HashMap<String, Double> itemLog = new HashMap<>();
    private HashMap<String, Double> userBudgetLog = new HashMap<>();
    private HashMap<String, List<String>> waitList = new HashMap<>();

    private int quebecPurchaseItemUDPPort = 50000;
    private static int quebecListItemUDPPort = 50001;
    private static int quebecCustomerBudgetPort = 50009;
    private static int quebecReturnPort = 50012;
    private static int quebecPurchasePort = 50015;

    private int britishColumbiaPurchaseItemUDPPort = 50002;
    private static int britishColumbiaListItemUDPPort = 50003;
    private static int britishColumbiaCustomerBudgetPort = 50010;
    private static int britishColumbiaReturnPort = 50013;
    private static int britishColombiaPurchasePort = 50016;

    private int ontarioPurchaseItemUDPPort = 50004;
    private static int ontarioListItemUDPPort = 50005;
    private static int ontarioCustomerBudgetPort = 50011;
    private static int ontarioReturnPort = 50014;
    private static int ontarioPurchasePort = 50017;

    private ClientHelper clientHelper;
    private ManagerHelper managerHelper;

    public StoreImplementation(String branchID) {
        this.branchID = branchID;
        this.managerHelper = new ManagerHelper(this.branchID);
        this.clientHelper = new ClientHelper(this.branchID);
        openAllPorts(branchID);
    }


    @Override
    public ReplicaResponse purchaseItem(String userID, String itemID, String dateOfPurchase) {
        System.out.println("Item has been requested to be purchased");
        userID = userID.toLowerCase();
        itemID = itemID.toLowerCase();
        try {
            return clientHelper.purchaseItem(userID, itemID, dateOfPurchase, this);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ReplicaResponse findItem(String userID, String itemName) {
        System.out.println("Item has been requested to be found by name");
        userID = userID.toLowerCase();
        itemName = itemName.toLowerCase();
        return clientHelper.findItem(userID, itemName, this);
    }

    @Override
    public ReplicaResponse returnItem(String userID, String itemID, String dateOfReturn) {
        System.out.println("Item has been requested to be returned");
        userID = userID.toLowerCase();
        itemID = itemID.toLowerCase();
        return clientHelper.returnItem(userID, itemID, dateOfReturn, this);
    }

    public ReplicaResponse exchange(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        customerID = customerID.toLowerCase();
        newItemID = newItemID.toLowerCase();
        oldItemID = oldItemID.toLowerCase();
        return clientHelper.exchangeItem(customerID, newItemID, oldItemID, dateOfExchange, this);
    }

    @Override
    public ReplicaResponse addItem(String userID, String itemID, String itemName, int quantity, double price) {
        System.out.println("Item has been requested to be added");
        itemID = itemID.toLowerCase();
        itemName = itemName.toLowerCase();
        return managerHelper.addItem(userID, itemID, itemName, quantity, price, this);
    }

    @Override
    public ReplicaResponse removeItem(String userID, String itemID, int quantity) {
        System.out.println("Item has been requested to be removed");
        userID = userID.toLowerCase();
        itemID = itemID.toLowerCase();
        return managerHelper.removeItem(userID, itemID, quantity, this);
    }

    @Override
    public ReplicaResponse listItemAvailability(String userID) {
        userID = userID.toLowerCase();
        return managerHelper.listItemAvailability(userID, this);
    }

    public String checkWaitList(String itemID, int quantity) {
        String actionMessage = "";
        int i = 0;
        int j = quantity;
        String returnMessage = "";
        ReplicaResponse itemPurchased;
        for (Map.Entry<String, List<String>> entry : waitList.entrySet()) {
            for (String waitListItemID : entry.getValue()) {
                if (itemID.equalsIgnoreCase(entry.getKey()) && entry.getValue().size() > 0) {
                    System.out.print(entry.getValue().get(i) + " is at the top of the list and will attempt to purchase " + itemID + "\n");
                    String dateString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date());
                    itemPurchased = purchaseItem(entry.getValue().get(i), itemID, dateString);
                    for (Map.Entry<String, String> response : itemPurchased.getResponse().entrySet()) {
                        actionMessage = response.getValue();
                    }
                    if (actionMessage.contains("\"Item was successfully purchased\"")) {
                        returnMessage = "Purchased Item from inventory Customer who was on waitlist: CustomerID:"+entry.getValue().get(i)+" itemID:"+itemID+"\n";
                        j--;
                        waitList.remove(itemID);
                    } else {
                        i++;
                    }
                }
            }
        }
        return returnMessage;
    }

    public Boolean waitList(String userID, String itemID, Date dateOfPurchase) {
        Boolean isWaitListed = false;
        if (waitList.containsKey(itemID)) {
            waitList.get(itemID).add(userID);

            Logger.writeStoreLog(this.branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())
                    + "Task SUCCESSFUL: Waitlisted user:" + userID + " for the item:" + itemID);
            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())
                    + "Task SUCCESSFUL: Waitlisted user:" + userID + " for the item:" + itemID);
            isWaitListed = true;
        } else {
            List<String> listOfCustomers = new ArrayList<>();
            listOfCustomers.add(userID);
            waitList.put(itemID, listOfCustomers);

            Logger.writeStoreLog(this.branchID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())
                    + "Task SUCCESSFUL: Waitlisted user:" + userID + " for the item:" + itemID);
            Logger.writeUserLog(userID, new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())
                    + "Task SUCCESSFUL: Waitlisted user:" + userID + " for the item:" + itemID);
            isWaitListed = true;
        }
        return isWaitListed;
    }

    private void openAllPorts(String provinceID) {
        switch (provinceID.toLowerCase()) {
            case "on":
                openListItemUDPPort(ontarioListItemUDPPort);
                openPurchaseItemUDPPort(ontarioPurchaseItemUDPPort);
                openUpdateCustomerBudgetLogUDPPort(ontarioCustomerBudgetPort);
                openReturnUDPPort(ontarioReturnPort);
                openUpdateCustomerPurchaseLogUDPPort(ontarioPurchasePort);
                break;
            case "qc":
                openListItemUDPPort(quebecListItemUDPPort);
                openPurchaseItemUDPPort(quebecPurchaseItemUDPPort);
                openUpdateCustomerBudgetLogUDPPort(quebecCustomerBudgetPort);
                openReturnUDPPort(quebecReturnPort);
                openUpdateCustomerPurchaseLogUDPPort(quebecPurchasePort);
                break;
            case "bc":
                openListItemUDPPort(britishColumbiaListItemUDPPort);
                openPurchaseItemUDPPort(britishColumbiaPurchaseItemUDPPort);
                openUpdateCustomerBudgetLogUDPPort(britishColumbiaCustomerBudgetPort);
                openReturnUDPPort(britishColumbiaReturnPort);
                openUpdateCustomerPurchaseLogUDPPort(britishColombiaPurchasePort);
                break;
        }
    }

    public void openListItemUDPPort(int updPort) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(updPort);
            byte[] receiveData = new byte[8];
            String sendString = "ListItemUDPPort now open ...";
            byte[] sendData = sendString.getBytes("UTF-8");

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            ListItemConnectionThread thread = new ListItemConnectionThread(serverSocket, receivePacket);
            thread.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void openPurchaseItemUDPPort(int updPort) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(updPort);
            byte[] receiveData = new byte[1024];
            String sendString = "Purchase item port opened and has received purchase request...";

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            PurchaseConnectionThread thread = new PurchaseConnectionThread(serverSocket, receivePacket);
            thread.start();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void openUpdateCustomerBudgetLogUDPPort(int updPort) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(updPort);
            System.out.println("Update customer budget UPD port: " + updPort);
            byte[] receiveData = new byte[1024];
            String sendString = "Update customer budget item port opened ...";
            byte[] sendData = sendString.getBytes("UTF-8");

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            UpdateCustomerBudgetLogThread thread = new UpdateCustomerBudgetLogThread(serverSocket, receivePacket, this.userBudgetLog);
            thread.start();

        } catch (Exception e) {
            System.out.println("openUpdateCustomerBudgetLogUDPPort \n" + e);
        } finally {
            //  serverSocket.close();
        }
    }

    public void openUpdateCustomerPurchaseLogUDPPort(int updPort) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(updPort);
            System.out.println("Update customer purchase UPD port: " + updPort);
            byte[] receiveData = new byte[1024];
            String sendString = "Update customer purchase port opened ...";
            byte[] sendData = sendString.getBytes("UTF-8");

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            UpdateCustomerPurchaseLogThread thread = new UpdateCustomerPurchaseLogThread(serverSocket, receivePacket, this);
            thread.start();

        } catch (Exception e) {
            System.out.println("openUpdateCustomerPurchaseLogUDPPort \n" + e);
        } finally {
            //  serverSocket.close();
        }
    }

    private void openReturnUDPPort(int returnUDPPort) {
        DatagramSocket serverSocket = null;
        try {
            System.out.println("return UPD port: " + returnUDPPort);
            serverSocket = new DatagramSocket(returnUDPPort);
            System.out.println("Return Item UPD port: " + returnUDPPort);
            byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            ReturnItemThread thread = new ReturnItemThread(serverSocket, receivePacket);
            thread.start();

        } catch (Exception e) {
            System.out.println("Trouble when running openReturnUDPPort \n" + e);
        } finally {
            //  serverSocket.close();
        }
    }


    public ArrayList<Item> getItemsByName(String itemName) {
        ArrayList<Item> itemCollection = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Item>> entry : inventory.entrySet()) {
            for (Item item : entry.getValue()) {
                if (item.getItemName().equalsIgnoreCase(itemName))
                    itemCollection.add(item);
            }
        }
        return itemCollection;
    }


    private class PurchaseConnectionThread extends Thread {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;

        public PurchaseConnectionThread(DatagramSocket serverSocket, DatagramPacket receivePacket) {
            this.serverSocket = serverSocket;
            this.receivePacket = receivePacket;
        }

        @Override
        public void run() {

            while (true) {
                try {
                    serverSocket.receive(receivePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String purchaseRequestString = new String(receivePacket.getData(), 0, receivePacket.getLength());

                HashMap<String, String> purchaseOrder = new HashMap<>();
                String[] strParts = purchaseRequestString.split("\\r?\\n|\\r");
                purchaseOrder.put(strParts[0], strParts[1]);

                ReplicaResponse replicaResponse;
                String userID;

                for (Map.Entry<String, String> entry : purchaseOrder.entrySet()) {
                    userID = entry.getKey();
                    String itemID = entry.getValue();

                    try {
                        InetAddress ip = InetAddress.getLocalHost();
                        String dateString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date());
                        replicaResponse = purchaseItem(userID, itemID, dateString);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(replicaResponse);

                        byte[] data = outputStream.toByteArray();
                        DatagramPacket sendPacket = new DatagramPacket(data, data.length, ip, receivePacket.getPort());
                        serverSocket.send(sendPacket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Item sent to the store that made the request ...");
            }
        }
    }

    private class ListItemConnectionThread extends Thread {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;

        public ListItemConnectionThread(DatagramSocket serverSocket, DatagramPacket receivePacket) {
            this.serverSocket = serverSocket;
            this.receivePacket = receivePacket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    InetAddress host = InetAddress.getLocalHost();
                    serverSocket.receive(receivePacket);
                    String itemName = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Searching for: Item name: " + itemName);

                    ArrayList<Item> itemsFound = getItemsByName(itemName);

                    System.out.println(itemsFound);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(outputStream);
                    os.writeObject(itemsFound);

                    byte[] data = outputStream.toByteArray();
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, host, receivePacket.getPort());
                    serverSocket.send(sendPacket);
                } catch (IOException e) {
                }

            }
        }
    }

    public HashMap<String, HashMap<String, Date>> getUserPurchaseLog() {
        return userPurchaseLog;
    }

    public HashMap<String, ArrayList<Item>> getInventory() {
        return inventory;
    }

    public HashMap<String, Double> getUserBudgetLog() {
        return userBudgetLog;
    }

    public HashMap<String, HashMap<String, Date>> getUserReturnLog() {
        return userReturnLog;
    }

    public HashMap<String, Double> getItemLog() {
        return itemLog;
    }

    public class UpdateCustomerBudgetLogThread extends Thread {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;
        private HashMap<String, Double> customerBudgetLog;

        public UpdateCustomerBudgetLogThread(DatagramSocket serverSocket, DatagramPacket receivePacket, HashMap<String, Double> customerBudgetLog) {
            this.serverSocket = serverSocket;
            this.receivePacket = receivePacket;
            this.customerBudgetLog = customerBudgetLog;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    byte[] incomingData = new byte[1024];
                    serverSocket.receive(receivePacket); //TODO Could be receiving a null object, may need to refactor
                    byte[] data = receivePacket.getData();
                    ByteArrayInputStream in = new ByteArrayInputStream(data);
                    ObjectInputStream is = new ObjectInputStream(in);

                    Pair<String, Double> customerIDandUpdatedBudget = (Pair<String, Double>) is.readObject(); //This ALREADY HAS UPDATED BUDGET DOUBLE

                    if (userBudgetLog.containsKey(customerIDandUpdatedBudget.getKey())) {
                        userBudgetLog.replace(customerIDandUpdatedBudget.getKey(), customerIDandUpdatedBudget.getValue());
                    } else {
                        userBudgetLog.put(customerIDandUpdatedBudget.getKey(), customerIDandUpdatedBudget.getValue());
                    }

                    System.out.println("Set and updated the customer budget log");
                } catch (IOException | ClassNotFoundException e) {
                }
            }
        }
    }

    public class ReturnItemThread extends Thread {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;

        public ReturnItemThread(DatagramSocket serverSocket, DatagramPacket receivePacket) {
            this.serverSocket = serverSocket;
            this.receivePacket = receivePacket;
        }

        @Override
        public void run() {

            while (true) {
                try {
                    serverSocket.receive(receivePacket);
                } catch (IOException e) {
                }

                String returnRequestString = new String(receivePacket.getData(), 0, receivePacket.getLength());

                String[] returnOrder = returnRequestString.split("\\r?\\n|\\r");
                String customerID = returnOrder[0];
                String itemID = returnOrder[1];
                String dateOfPurchase = returnOrder[2];

                ReplicaResponse replicaResponse;

                try {
                    InetAddress ip = InetAddress.getLocalHost();
                    replicaResponse = returnItem(customerID, itemID, dateOfPurchase);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(outputStream);
                    os.writeObject(replicaResponse);

                    byte[] data = outputStream.toByteArray();
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ip, receivePacket.getPort());
                    serverSocket.send(sendPacket); //TODO It is now sending a boolean
                    System.out.println("Item sent to the store that made the request ...");
                } catch (Exception e) {
                    System.out.println("ReturnItem Exception: " + e);
                }
            }
        }
    }

    public class UpdateCustomerPurchaseLogThread extends Thread {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;
        private StoreImplementation store;


        public UpdateCustomerPurchaseLogThread(DatagramSocket serverSocket, DatagramPacket receivePacket, StoreImplementation store){
            this.serverSocket = serverSocket;
            this.receivePacket = receivePacket;
            this.store = store;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    byte[] incomingData = new byte[1024];
                    serverSocket.receive(receivePacket); //TODO Could be receiving a null object, may need to refactor
                    byte[] data = receivePacket.getData();
                    ByteArrayInputStream in = new ByteArrayInputStream(data);
                    ObjectInputStream is = new ObjectInputStream(in);

                    Pair<String, String> customerReturnedItem = (Pair<String, String>) is.readObject();

                    removeUserFromPurchaseLog(customerReturnedItem.getKey(),customerReturnedItem.getValue(),store);

                    System.out.println("Set and updated the customer budget log");
                } catch (IOException | ClassNotFoundException e) {
                }
            }
        }
        private void removeUserFromPurchaseLog(String userID, String itemID, StoreImplementation store) {
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
    }
}

