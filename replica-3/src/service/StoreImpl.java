package service;

import networkEntities.RegisteredReplica;
import replica.ReplicaResponse;
import service.utilities.entities.item.Item;
import service.utilities.entities.threads.ListItemThread;
import service.utilities.entities.threads.PurchaseItemThread;
import service.utilities.entities.threads.ReturnItemThread;
import service.utilities.entities.threads.UpdateCustomerBudgetLogThread;
import service.interfaces.StoreInterface;
import service.utilities.helpers.ClientHelper;
import service.utilities.helpers.ManagerHelper;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@WebService(endpointInterface = "service.interfaces.StoreInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class StoreImpl implements StoreInterface {

    private ClientHelper clientHelper;
    private ManagerHelper managerHelper;

    private String provinceID;

    ////////////////////////////////////
    ///    Store data structures     ///
    ////////////////////////////////////
    private HashMap<String, List<Item>> inventory = new HashMap<>();
    private HashMap<String, List<HashMap<String, Date>>> customerPurchaseLog = new HashMap<>();
    private HashMap<String, HashMap<String, Date>> customerReturnLog = new HashMap<>();
    private HashMap<String, Double> customerBudgetLog = new HashMap<>();
    private HashMap<String, List<String>> itemWaitList = new HashMap<>(); //String = itemID, List of customerIDs
    private List<Item> itemLog = new ArrayList<>();
    private HashMap<String, List<Item>> customerItemDetention = new HashMap<>();


    ////////////////////////////////////
    ///       UDP Related Ports      ///
    ////////////////////////////////////
    private static int quebecPurchaseItemUDPPort = 40999;
    private static int quebecListItemUDPPort = 40998;
    private static int quebecCustomerBudgetPort = 40997;
    private static int quebecReturnUDPPort = 40996;

    private static int britishColumbiaPurchaseItemUDPPort = 50004;
    private static int britishColumbiaListItemUDPPort = 50005;
    private static int britishColumbiaCustomerBudgetPort = 50006;
    private static int britishColumbiaReturnUDPPort = 50007;

    private static int ontarioPurchaseItemUDPPort = 50008;
    private static int ontarioListItemUDPPort = 50009;
    private static int ontarioCustomerBudgetPort = 50010;
    private static int ontarioReturnUDPPort = 50011;


    public StoreImpl(String provinceID) {
        this.provinceID = provinceID;
        this.clientHelper = new ClientHelper(this.provinceID);
        this.managerHelper = new ManagerHelper(this.provinceID);

        openAllPorts(this.provinceID);
    }
    @Override
    public ReplicaResponse addItem(String managerID, String itemID, String itemName, int quantity, double price) {
        //TODO For waitlisted customers, check their budget before giving them the item.
        return getManagerHelper().addItem(managerID, itemID, itemName, quantity, price, this);
    }

    @Override
    public ReplicaResponse removeItem(String managerID, String itemID, int quantity) {
        return getManagerHelper().removeItem(managerID, itemID, quantity, getInventory());
    }

    @Override
    public ReplicaResponse listItemAvailability(String managerID) {
        return getManagerHelper().listItemAvailability(managerID, getInventory());
    }

    @Override
    public ReplicaResponse purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        return getClientHelper().purchaseItem(customerID, itemID, dateOfPurchase, this);
    }

    @Override
    public ReplicaResponse findItem(String customerID, String itemName) {
        return getClientHelper().findItem(customerID, itemName, getInventory());
    }

    @Override
    public ReplicaResponse returnItem(String customerID, String itemID, String dateOfReturn) {
        return getClientHelper().returnItem(customerID, itemID, dateOfReturn, this);
    }

    @Override
    public ReplicaResponse exchange(String customerID, String newItemID, String oldItemID, String dateOfReturn) {
        String dateOfPurchase = new SimpleDateFormat("mm/dd/yyyy").format(new Date());

        ReplicaResponse returnReplicaResponse = returnItem(customerID, oldItemID, dateOfReturn);

        boolean isReturnSuccessful = false;
        boolean isPurchaseSuccessful = false;
        String isPurchaseSuccessfulString = "";

        isReturnSuccessful = returnReplicaResponse.getResponse().get(customerID.toLowerCase()).contains("Task SUCCESSFUL:");
        System.out.println("MKC1: isReturnSuccessful "+isReturnSuccessful);

        StringBuilder exchangeResult = new StringBuilder();
        exchangeResult.append("\t>>>>>>>>>>>>> Exchange Item Result <<<<<<<<<<<<<<\n");
        exchangeResult.append(returnReplicaResponse.toString()+"\n");

        if(isReturnSuccessful) {
            ReplicaResponse purchaseReplicaResponse  = purchaseItem(customerID, newItemID, dateOfPurchase);
            isPurchaseSuccessfulString = purchaseReplicaResponse.toString();
            isPurchaseSuccessful = isPurchaseSuccessfulString.contains("Task SUCCESSFUL:");
            System.out.println("MKC1: isPurchaseSuccessful"+isPurchaseSuccessful);
            if(!isPurchaseSuccessful)
                purchaseItem(customerID, oldItemID, dateOfPurchase);

            exchangeResult.append(isPurchaseSuccessfulString+"\n");
        }
        else {
            exchangeResult.append("Purchase Did not execute since return was not successful. Can not exchange.\n");
        }


        boolean isExchangeSuccessful = isPurchaseSuccessful && isReturnSuccessful;
        String exchangeSuccessful = isExchangeSuccessful? "Exchange Successful": "Exchange Not Successful";
        exchangeResult.append(exchangeSuccessful+"\n");
        exchangeResult.append("\t>>>>>>>>>>>>> END <<<<<<<<<<<<<<");


        ReplicaResponse finalReplicaResponse = new ReplicaResponse();
        finalReplicaResponse.setSuccessResult(isExchangeSuccessful);
        finalReplicaResponse.setReplicaID(RegisteredReplica.ReplicaS3);
        finalReplicaResponse.getResponse().put(customerID, exchangeResult.toString());

        return finalReplicaResponse;
    }

    @Override
    public void requestUpdateOfCustomerBudgetLog(String customerID, double budget) { //TODO CHange the name OF THIS METHOD
        switch (this.provinceID.toLowerCase()) {
            case "qc":
                getClientHelper().sendCustomerBudgetUpdate(ontarioCustomerBudgetPort, customerID, budget, this);
                getClientHelper().sendCustomerBudgetUpdate(britishColumbiaCustomerBudgetPort, customerID, budget, this);
                break;
            case "on":
                getClientHelper().sendCustomerBudgetUpdate(quebecCustomerBudgetPort, customerID, budget, this);
                getClientHelper().sendCustomerBudgetUpdate(britishColumbiaCustomerBudgetPort, customerID, budget, this);
                break;
            case "bc":
                getClientHelper().sendCustomerBudgetUpdate(britishColumbiaCustomerBudgetPort, customerID, budget, this);
                getClientHelper().sendCustomerBudgetUpdate(quebecCustomerBudgetPort, customerID, budget, this);
                break;
        }
    }

    @Override
    public boolean waitList(String customerID, String itemID, String dateOfPurchase) {
        Boolean isWaitListed = false;
        //if(getCustomerBudgetLog().get(customerID.toLowerCase())) //TODO Move logic for deciding to put person on waiutlist here, check their budget AND if they're foreign and bought here before
        if(itemWaitList.containsKey(itemID)) {
            itemWaitList.get(itemID).add(customerID);

            // String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Waitlisted customer:" + customerID + " for the item: "+itemID;
            //Logger.writeStoreLog(this.provinceID, logString);
            //Logger.writeUserLog(customerID, logString);
            isWaitListed = true;
        }
        else {
            List<String> listOfCustomers = new ArrayList<>();
            listOfCustomers.add(customerID);
            itemWaitList.put(itemID, listOfCustomers);

            // String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Waitlisted customer:" + customerID + " for the item: "+itemID;
            //Logger.writeStoreLog(this.provinceID, logString);
            //Logger.writeUserLog(customerID, logString);
            isWaitListed = true;
        }
        return isWaitListed;
    }

    private void openAllPorts(String provinceID) {
        switch(provinceID.toLowerCase()){
            case "on":
                openListItemUDPPort(ontarioListItemUDPPort);
                openPurchaseItemUDPPort(ontarioPurchaseItemUDPPort);
                openUpdateCustomerBudgetLogUDPPort(ontarioCustomerBudgetPort);
                openReturnUDPPort(ontarioReturnUDPPort);
                break;
            case "qc":
                openListItemUDPPort(quebecListItemUDPPort);
                openPurchaseItemUDPPort(quebecPurchaseItemUDPPort);
                openUpdateCustomerBudgetLogUDPPort(quebecCustomerBudgetPort);
                openReturnUDPPort(quebecReturnUDPPort);
                break;
            case "bc":
                openListItemUDPPort(britishColumbiaListItemUDPPort);
                openPurchaseItemUDPPort(britishColumbiaPurchaseItemUDPPort);
                openUpdateCustomerBudgetLogUDPPort(britishColumbiaCustomerBudgetPort);
                openReturnUDPPort(britishColumbiaReturnUDPPort);
                break;
        }
    }

    private void openReturnUDPPort(int returnUDPPort) {
        DatagramSocket serverSocket = null;
        try {
            System.out.printf("Listening on udp:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), returnUDPPort);
            serverSocket = new DatagramSocket(returnUDPPort);
            System.out.println("Return Item UPD port: "+ returnUDPPort);
            byte[] receiveData = new byte[1024];

            // System.out.println("Opening purchase UDP port"+updPort+"for store");
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            ReturnItemThread thread = new ReturnItemThread(serverSocket, receivePacket, this);
            thread.start();

        } catch (Exception e) {
            System.out.println("Trouble when running openReturnUDPPort \n"+ e);
           // e.printStackTrace();
        } finally {
            //  serverSocket.close();
        }
    }

    public void openListItemUDPPort(int updPort) { //Receiving other stores request for a list of items
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(updPort);
            System.out.println("Open List Item Port: "+updPort);
            byte[] receiveData = new byte[8];

            System.out.printf("Listening on udp:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), updPort);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            ListItemThread thread = new ListItemThread(serverSocket, receivePacket, this);
            thread.start();
        } catch (Exception e) {
            System.out.println("openListItemUDPPort \n"+ e);
           // e.printStackTrace();
        } finally {
            //serverSocket.close();
        }
    }

    public void openPurchaseItemUDPPort(int updPort) { //When receiving requests to purchase items
        DatagramSocket serverSocket = null;
        try {
            System.out.printf("Listening on udp:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), updPort);
            serverSocket = new DatagramSocket(updPort);
            System.out.println("Purchase Item UPD port: "+ updPort);
            byte[] receiveData = new byte[1024];

            // System.out.println("Opening purchase UDP port"+updPort+"for store");
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            PurchaseItemThread thread = new PurchaseItemThread(serverSocket, receivePacket, this);
            thread.start();

        } catch (Exception e) {
            System.out.println("openPurchaseItemUDPPort \n"+ e);
           // e.printStackTrace();
        } finally {
            //  serverSocket.close();
        }
    }

    public void openUpdateCustomerBudgetLogUDPPort(int updPort) { //When receiving requests to purchase items
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(updPort);
            System.out.println("Update customer budget UPD port: "+ updPort);
            byte[] receiveData = new byte[1024];
            String sendString = "Update customer budget item port opened ...";
            byte[] sendData = sendString.getBytes("UTF-8");

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            UpdateCustomerBudgetLogThread thread = new UpdateCustomerBudgetLogThread(serverSocket, receivePacket, this.customerBudgetLog);
            thread.start();

        } catch (Exception e) {
            System.out.println("openUpdateCustomerBudgetLogUDPPort \n"+ e);
           // e.printStackTrace();
        } finally {
            //  serverSocket.close();
        }
    }

    ////////////////////////////////////
    ///     Getters and Setters      ///
    ////////////////////////////////////
    public HashMap<String, List<String>> getItemWaitList() {
        return this.itemWaitList;
    }
    public HashMap<String, List<Item>> getInventory() { return this.inventory; }
    public HashMap<String, Double> getCustomerBudgetLog() { return this.customerBudgetLog; }
    public HashMap<String, List<HashMap<String, Date>>> getCustomerPurchaseLog() { return this.customerPurchaseLog; }
    public HashMap<String, HashMap<String, Date>> getCustomerReturnLog() { return this.customerReturnLog; }
    public List<Item> getItemLog() { return this.itemLog; }
    public HashMap<String, List<Item>> getCustomerItemDetention() { return customerItemDetention; }
    public ClientHelper getClientHelper() { return this.clientHelper; }
    public ManagerHelper getManagerHelper() { return this.managerHelper; }
}
