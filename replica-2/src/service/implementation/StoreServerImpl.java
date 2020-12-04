package service.implementation;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import networkEntities.RegisteredReplica;
import replica.*;

import service.interfaces.StoreInterface;
import services.item.Items;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(endpointInterface = "service.interfaces.StoreInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class StoreServerImpl implements StoreInterface {
    public HashMap<String, Items> inventory = new HashMap<String, Items>();
    public HashMap<String, List<String>> waitlist = new HashMap<>();
    public HashMap<String, List<String>> customerLog = new HashMap<>();
    HashMap<String, Double> customerBudget = new HashMap<>();
    RegisteredReplica replicaID = RegisteredReplica.ReplicaS2;
    int sequenceID = 1;
    public String store;
    public int port;
    public int otherStore1;
    public int otherStore2;


    public StoreServerImpl(HashMap<String, Items> inventory, HashMap<String, List<String>> log, HashMap<String, List<String>> waitlist, HashMap<String, Double> customerBudget,String store) {
        this.inventory = inventory;
        this.waitlist = waitlist;
        this.customerLog = log;
        this.customerBudget = customerBudget;
        this.store = store;

        if(store.equalsIgnoreCase("qc")){
            port = 1099;
            otherStore1 = 1098;
            otherStore2 = 1097;
        }
        else if(store.equalsIgnoreCase("on")){
            port = 1098;
            otherStore1 = 1099;
            otherStore2 = 1097;
        }
        else{
            port = 1097;
            otherStore1 = 1099;
            otherStore2 = 1098;
        }
    }

    //******************************************************************************************************************
    //                                              CORE STORE FUNCTIONS
    //******************************************************************************************************************
    @Override
    public ReplicaResponse addItem(String managerID, String itemID, String itemName, int quantity, double price) throws InterruptedException {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();
        response.put("id",managerID);

        if(isManager(managerID).equals("true")){
            if(inventory.containsKey(itemID)){
                inventory.get(itemID).setItemName(itemName);
                inventory.get(itemID).setQuantity((inventory.get(itemID).getQuantity() + quantity));
                inventory.get(itemID).setPrice(price);
                updateWaitlist(itemID);

                try {
                    FileWriter writer = new FileWriter("src\\logs\\server\\" + getStore() +"StoreLogs.txt", true);
                    writer.write("STOCK INCREASE: " + "itemID='" + itemID + ", itemName='" + itemName + ", quantity=" + quantity + ", price=" + price + "\r\n");
                    writer.close();
                    System.out.println("Item Updated");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                returnResponse.setSuccessResult(true);
                response.put(managerID,"Alert: Item will be added ...");
                returnResponse.setResponse(response);
                return(returnResponse);
            }
            else{
                Items newItem = new Items(itemID, itemName, quantity, price);
                inventory.put(itemID, newItem);

                try {
                    FileWriter writer = new FileWriter("src\\logs\\server\\" + getStore() +"StoreLogs.txt", true);
                    writer.write("ADDED: " + "itemID='" + itemID + ", itemName='" + itemName + ", quantity=" + quantity + ", price=" + price + "\r\n");
                    writer.close();
                    System.out.println("Item Added");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                returnResponse.setSuccessResult(true);
                response.put(managerID,"Alert: Item will be added ...");
                returnResponse.setResponse(response);
                return(returnResponse);
            }
        }
        else{
            returnResponse.setSuccessResult(false);
            response.put(managerID,"Alert: You do not have the authorization to perform this task ...");
            returnResponse.setResponse(response);
            return(returnResponse);
        }
    }

    @Override
    public ReplicaResponse removeItem(String managerID, String itemID, int quantity){
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();
        response.put("id",managerID);

        if(isManager(managerID).equals("true")){
            if(quantity == -1 && inventory.containsKey(itemID)){
                inventory.remove(itemID);

                try {
                    FileWriter writer = new FileWriter("src\\logs\\server\\" + getStore() +"StoreLogs.txt", true);
                    writer.write("REMOVED: " + "itemID='" + itemID + ", quantity=" + quantity + "\r\n");
                    writer.close();
                    System.out.println("Item Removed");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                returnResponse.setSuccessResult(true);
                response.put(managerID,"Successful: Completely Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity);
                returnResponse.setResponse(response);
                return(returnResponse);

            }
            else if(inventory.containsKey(itemID)){
                if(quantity >= inventory.get(itemID).getQuantity())
                    inventory.get(itemID).setQuantity(0);
                else
                    inventory.get(itemID).setQuantity((inventory.get(itemID).getQuantity() - quantity));

                try {
                    FileWriter writer = new FileWriter("src\\logs\\server\\" + getStore() +"StoreLogs.txt", true);
                    writer.write("STOCK DECREASE: " + "itemID='" + itemID + ", quantity=" + quantity + "\r\n");
                    writer.close();
                    System.out.println("Item removed");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                returnResponse.setSuccessResult(true);
                response.put(managerID,"Successful: Completely Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity);
                returnResponse.setResponse(response);
                return(returnResponse);
            }
            else{
                returnResponse.setSuccessResult(false);
                response.put(managerID,"\tTask UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity+"\n");
                returnResponse.setResponse(response);
                return(returnResponse);

            }
        }
        else{
            returnResponse.setSuccessResult(false);
            response.put(managerID,"Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity+"\nALERT: You are not permitted to do this action on this store\n");
            returnResponse.setResponse(response);
            return(returnResponse);
        }
    }

    @Override
    public ReplicaResponse listItemAvailability(String managerID) {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();
        response.put("id",managerID);

        if(isManager(managerID).equals("true")){
            if(inventory.isEmpty()){
                returnResponse.setSuccessResult(true);
                response.put(managerID,"The store inventory is currently empty! ");
                returnResponse.setResponse(response);
                return(returnResponse);
            }
            else{
                StringBuilder returnMessage = new StringBuilder("This store contains the following items: \r\n"+"\t");
                for(Map.Entry<String, Items> entry : inventory.entrySet()){
                    returnMessage.append("\t"+entry.getValue().toString() +"\n");
                }
                returnResponse.setSuccessResult(true);
                response.put(managerID,returnMessage.toString());
                returnResponse.setResponse(response);
                return(returnResponse);
            }
        }
        else{
            returnResponse.setSuccessResult(false);
            response.put(managerID,"You do not have the authorization to perform this task");
            returnResponse.setResponse(response);
            return(returnResponse);
        }
    }

    @Override
    public ReplicaResponse purchaseItem(String customerID, String itemID, String dateOfPurchase) throws InterruptedException {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();

        if(inventory.containsKey(itemID)) {

            if (inventory.get(itemID).getQuantity() == 0) {
                addToWaitlist(customerID,itemID,dateOfPurchase);
                returnResponse.setSuccessResult(false);
                response.put(customerID,"Task UNSUCCESSFUL: However customer added to the waitlist for this item. "+customerID + "," + itemID + "," + dateOfPurchase );
                returnResponse.setResponse(response);
                return(returnResponse);
            }

            if(hasMadeRemotePurchase(customerID,itemID)){
                returnResponse.setSuccessResult(false);
                response.put(customerID,"Task UNSUCCESSFUL: Foreign Customer has a foreign item in their possession, can not purchase another. " + customerID + ", " + itemID + ", " + dateOfPurchase);
                returnResponse.setResponse(response);
                return(returnResponse);
            }

            inventory.get(itemID).setQuantity(inventory.get(itemID).getQuantity() - 1);
            logPurchase(customerID, itemID, dateOfPurchase);

            try {
                FileWriter writer = new FileWriter("src\\logs\\users\\" + customerID.substring(0, 2).toUpperCase() + "CustomerLog.txt");
                FileWriter writer2 = new FileWriter("src\\logs\\server\\" + store + "StoreLogs.txt", true);
                writer.write("PURCHASE: " + "customerID=" + customerID + ", itemID=" + itemID + ", dateOfPurchase=" + dateOfPurchase + "\r\n");
                writer2.write("ITEM PURCHASE: " + "itemID='" + itemID + ", itemName='" +
                        inventory.get(itemID).getItemName() + ", quantity=" +
                        inventory.get(itemID).getQuantity() + ", price=" +
                        inventory.get(itemID).getPrice() + "\r\n");
                writer.close();
                writer2.close();
                System.out.println("Item Purchased");
            } catch (IOException e) {
                e.printStackTrace();
            }

            returnResponse.setSuccessResult(true);
            response.put(customerID,"Task SUCCESSFUL: Customer purchased Item "+customerID + "," + itemID + "," + dateOfPurchase);
            returnResponse.setResponse(response);
            return(returnResponse);
        }
        else {
            returnResponse.setSuccessResult(false);
            response.put(customerID,"Task UNSUCCESSFUL: An item of that name does not exist in this store or has been removed");
            returnResponse.setResponse(response);
            return(returnResponse);
        }
    }

    @Override
    public String findItem(String customerID, String itemName) {
        String response = "";

        for (HashMap.Entry<String, Items> entry: inventory.entrySet()) {
            if(entry.getValue().getItemName().equalsIgnoreCase(itemName)){
                response = response.concat(inventory.get(entry.getValue().getItemID()).toString() +" " + inventory.get(entry.getValue().getItemID()).getQuantity()+"/");
            }
        }

        return response;
    }

    @Override
    public ReplicaResponse returnItem(String customerID, String itemID, String dateOfReturn) throws FileNotFoundException, InterruptedException {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();

        String purchaseDate = "";

        if(!isSameStore(itemID)) {
            returnResponse = runRemoteReturn(customerID, itemID, dateOfReturn);
            if(returnResponse.getResponse().get(customerID).contains("successful"))
                if(customerBudget.containsKey(customerID))
                    updateBudget(customerID,itemID,'r');

            return returnResponse;
        }

        if(customerLog.containsKey(customerID)){
            for(int i = 0; i < customerLog.get(customerID).size(); i++) {
                if (customerLog.get(customerID).get(i).contains(itemID)) {
                    purchaseDate = customerLog.get(customerID).get(i).substring(9);

                    if (!(within30Days(purchaseDate, dateOfReturn))){
                        returnResponse.setSuccessResult(false);
                        response.put(customerID,"Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\nAlert: Customer has purchased this item in the past, but item purchase date exceeds 30days");
                        returnResponse.setResponse(response);
                        return(returnResponse);
                    }

                    else {

                        customerLog.get(customerID).remove(0);

                        if(customerLog.get(customerID).size() == 0)
                            customerLog.remove(customerID);

                        inventory.get(itemID).setQuantity(inventory.get(itemID).getQuantity() + 1);

                        try {
                            FileWriter writer = new FileWriter("src\\logs\\users\\" + customerID.substring(0, 2).toUpperCase() + "CustomerLog.txt", true);
                            FileWriter writer2 = new FileWriter("src\\logs\\server\\" + store + "StoreLogs.txt", true);
                            writer.write("RETURNED: " + "customerID=" + customerID + ", itemID=" + itemID + ", dateOfReturn=" + dateOfReturn + "\r\n");
                            writer2.write("ITEM RETURN: " + "itemID='" + itemID + ", itemName='" +
                                    inventory.get(itemID).getItemName() + ", quantity=" +
                                    inventory.get(itemID).getQuantity() + ", price=" +
                                    inventory.get(itemID).getPrice() + "\r\n");
                            writer.close();
                            writer2.close();
                            System.out.println("Item Returned");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        updateWaitlist(itemID);

                        if(customerBudget.containsKey(customerID))
                            updateBudget(customerID,itemID,'r');

                        returnResponse.setSuccessResult(true);
                        response.put(customerID,"Task SUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n");
                        returnResponse.setResponse(response);
                        return(returnResponse);
                    }
                }
            }
            returnResponse.setSuccessResult(false);
            response.put(customerID,"Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n"+"Alert: Customer has past purchases, but NOT of this item");
            returnResponse.setResponse(response);
            return(returnResponse);
        }
        else{
            returnResponse.setSuccessResult(false);
            response.put(customerID,"Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n"+"Alert: Customer has past purchases, but NOT of this item");
            returnResponse.setResponse(response);
            return(returnResponse);
        }
    }

    @Override
    public ReplicaResponse exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) throws InterruptedException, FileNotFoundException {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();

        if(findItemRequest(customerID,newItemID).getResponse().get(customerID).equals("")){
            returnResponse.setSuccessResult(false);
            response.put(customerID,"New Item ("+newItemID+"), not found cannot process exchange. \n");
            returnResponse.setResponse(response);
            return(returnResponse);
        }

        if(customerBudget.get(customerID)+getRemotePrice(oldItemID) < getRemotePrice(newItemID)){
            returnResponse.setSuccessResult(false);
            response.put(customerID,"you will not have the funds to buy the New Item ("+newItemID+"). \n");
            returnResponse.setResponse(response);
            return(returnResponse);
        }

        String itemReturn = returnItem(customerID,oldItemID,dateOfExchange).getResponse().get(customerID);
        updateWaitlist(oldItemID);
        if(itemReturn.contains("successful")){

            itemReturn = runRemotePurchase(customerID,newItemID,dateOfExchange).getResponse().get(customerID);

            if(itemReturn.contains("successful")){
                returnResponse.setSuccessResult(true);
                response.put(customerID,"Exchange successful. The following was purchased: " + newItemID);
                returnResponse.setResponse(response);
                return(returnResponse);
            }
        }

        returnResponse.setSuccessResult(false);
        response.put(customerID,itemReturn);
        returnResponse.setResponse(response);
        return(returnResponse);
    }

    //******************************************************************************************************************
    //                                              ADDITIONAL STORE FUNCTIONS
    //******************************************************************************************************************

    @Override
    public double getItemPrice(String itemID) {
        if(inventory.containsKey(itemID)) {
            return inventory.get(itemID).getPrice();
        }

        return 0;
    }

    @Override
    public String addToWaitlist(String customerID, String itemId, String dateOfPurchase) throws InterruptedException {
        if(isSameStore(itemId)) {
            if (waitlist.containsKey(itemId))
                if (waitlist.get(itemId).contains(customerID))
                    return ("You are already on the waitlist for this item.");
                else {
                    waitlist.get(itemId).add(customerID+" - " +dateOfPurchase);
                    return ("You have been added to the waitlist for the following item: " + itemId);
                }
            else {
                List<String> wait = new ArrayList<>();
                wait.add(customerID +" - " +dateOfPurchase);
                waitlist.put(itemId, wait);
                return ("You have been added to the waitlist for the following item: " + itemId);
            }
        }
        else{
            return runRemoteAddToWait(customerID, itemId,dateOfPurchase);
        }

    }

    public void logPurchase(String customerID, String itemId, String dateOfPurchase){
        if(customerLog.containsKey(customerID))
            customerLog.get(customerID).add(itemId + " - " + dateOfPurchase);
        else{
            List<String> log = new ArrayList<>();
            log.add(itemId + " - " + dateOfPurchase);
            customerLog.put(customerID, log);
        }
    }

    public void updateWaitlist(String itemID) throws InterruptedException {
        if (waitlist.containsKey(itemID)) {

            int stockToBeProcessed = 0;

            if(inventory.get(itemID).getQuantity() - waitlist.size() > 0)
                stockToBeProcessed = waitlist.size();
            else if(inventory.get(itemID).getQuantity() - waitlist.size() < 0)
                stockToBeProcessed = inventory.get(itemID).getQuantity();
            else if(inventory.get(itemID).getQuantity() - waitlist.size() == 0)
                stockToBeProcessed = waitlist.size();

            for (int i = 0; i < stockToBeProcessed; i++) {
                    purchaseItem(waitlist.get(itemID).get(0).substring(0, 7), itemID, waitlist.get(itemID).get(0).substring(9));
                    waitlist.get(itemID).remove(0);
            }

            if(waitlist.get(itemID).size() == 0)
                waitlist.remove(itemID);
        }
    }

    public String showWaitlist(){
        if(waitlist.isEmpty())
            return(" The store waitlist is currently empty! ");
        else{
            String list = "";

            for(Map.Entry<String, List<String>> entry : waitlist.entrySet()) {
                String temp = entry.getKey() + ": " + entry.getValue().toString() +"/";
                list = list.concat(temp);
            }

            return(list);
        }

    }

    public String showCustomerLog(){
        if(customerLog.isEmpty())
            return(" The store customer log is currently empty! ");
        else{
            String list = "";

            for(Map.Entry<String, List<String>> entry : customerLog.entrySet()) {
                String temp = entry.getKey() + ": " + entry.getValue().toString() +"/";
                list = list.concat(temp);
            }

            System.out.print(list);
            return(list);
        }
    }

    public void addToBudget(String customerID){
        customerBudget.put(customerID,1000.00);
    }

    public void updateBudget(String customerID, String itemID, char action) throws InterruptedException {
        if(action == 'p')
            customerBudget.put(customerID, customerBudget.get(customerID) - getRemotePrice(itemID));
        else if(action == 'r')
            customerBudget.put(customerID, customerBudget.get(customerID) + getRemotePrice(itemID));
    }

    //******************************************************************************************************************
    //                                              UDP REQUEST FUNCTIONS
    //******************************************************************************************************************

    public String remoteFindItem(int portNumber, String customerID, String itemName){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            String msg = customerID + itemName +"f";
            byte[] message = msg.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, portNumber);
            aSocket.send(request);
            System.out.println("Request message sent from the client to server with port number " + portNumber + " is: "
                    + new String(request.getData()));
            byte[] buffer = new byte[3000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            System.out.println("Reply message from port number " + reply.getPort() + " is: "
                    + new String(reply.getData()));
            return new String(reply.getData()).substring(0, reply.getLength());
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

        return "";
    }

    public ReplicaResponse findItemRequest(String customerID, String itemName) throws InterruptedException {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();

        final String[] udpResponse = new String[2];
        Thread currentThread = Thread.currentThread();

        Runnable task = () -> {
            udpResponse[0] = remoteFindItem(otherStore1, customerID, itemName);
            udpResponse[1] = remoteFindItem(otherStore2, customerID, itemName);
            synchronized (currentThread){
                currentThread.notify();
            }
        };

        Thread thread = new Thread(task);
        thread.start();

        synchronized (currentThread){
            currentThread.wait();
        }

        String strResponse = findItem(customerID,itemName) + udpResponse[0] + udpResponse[1];
        String [] resArr = strResponse.split("/");
        StringBuilder foundItems = new StringBuilder();
        foundItems.append(">>>>>>>>>>>> All Items Found <<<<<<<<<<<< \n");

        for (String item : resArr)
            foundItems.append("\t" + item.toString() +"\n");

        returnResponse.setSuccessResult(true);
        response.put(customerID,foundItems.toString());
        returnResponse.setResponse(response);
        return(returnResponse);
    }

    public byte[] retrieveItems(DatagramPacket request, StoreServerImpl store){
        String customerID = new String(request.getData()).substring(0, 7);
        String itemName = new String(request.getData()).substring(7, charLength(request) - 1);
        System.out.println(itemName);
        return store.findItem(customerID, itemName).getBytes();
    }

    public int charLength(DatagramPacket request){
        int length;
        for (length = 0; length < request.getLength(); length++){
            if(!Character.isLetterOrDigit(new String(request.getData()).charAt(length)))
                break;
        }
        return length;
    }

    public String requestRemotePrice(int portNumber, String itemID){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            String msg = itemID +"p";
            byte[] message = msg.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, portNumber);
            aSocket.send(request);
            System.out.println("Request message sent from the client to server with port number " + portNumber + " is: "
                    + new String(request.getData()));
            byte[] buffer = new byte[3000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            System.out.println("Reply message from port number " + reply.getPort() + " is: "
                    + new String(reply.getData()));
            return new String(reply.getData()).substring(0, reply.getLength());
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

        return "Price fetch unsuccessful";
    }

    public double getRemotePrice(String itemID) throws InterruptedException {
        final double[] udpResponse = new double[2];
        Thread currentThread = Thread.currentThread();

        Runnable task = () -> {
            udpResponse[0] = Double.parseDouble(requestRemotePrice(otherStore1, itemID));
            udpResponse[1] = Double.parseDouble(requestRemotePrice(otherStore2, itemID));
            synchronized (currentThread){
                currentThread.notify();
            }
        };

        Thread thread = new Thread(task);
        thread.start();

        synchronized (currentThread){
            currentThread.wait();
        }

        return (getItemPrice(itemID) + udpResponse[0] + udpResponse[1]);
    }

    public byte[] retrieveRemotePrice(DatagramPacket request, StoreServerImpl store){
        String itemID = new String(request.getData()).substring(0, 6);
        System.out.println(itemID);
        return String.valueOf(store.getItemPrice(itemID)).getBytes();
    }

    public String requestRemotePurchase(int portNumber, String customerID, String itemID, String dateOfPurchase){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            String msg = customerID + itemID + dateOfPurchase +"b";
            byte[] message = msg.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, portNumber);
            aSocket.send(request);
            System.out.println("Request sent from "+ store +" to server with port number " + portNumber + " is: "
                    + new String(request.getData()));
            byte[] buffer = new byte[3000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            System.out.println("Reply message from port number " + reply.getPort() + " is: " + new String(reply.getData()));

            return new String(reply.getData()).substring(0, reply.getLength());
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

        return "Could Not Make Purchase";
    }

    public ReplicaResponse runRemotePurchase(String customerID, String itemID, String dateOfPurchase) throws InterruptedException {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();

        if(!customerBudget.containsKey(customerID))
            addToBudget(customerID);

        if(isSameStore(itemID)){
            if(customerBudget.get(customerID) < inventory.get(itemID).getPrice()){
                returnResponse.setSuccessResult(false);
                response.put(customerID,"Task UNSUCCESSFUL: Customer does not have the funds for this item,"+customerID + "," + itemID + "," + dateOfPurchase);
                returnResponse.setResponse(response);
                return(returnResponse);
            }
        }
        else{
            if(customerBudget.get(customerID) < getRemotePrice(itemID)){
                returnResponse.setSuccessResult(false);
                response.put(customerID,"Task UNSUCCESSFUL: Customer does not have the funds for this item,"+customerID + "," + itemID + "," + dateOfPurchase);
                returnResponse.setResponse(response);
                return(returnResponse);
            }
        }


        String strResponse = purchaseItem(customerID, itemID, dateOfPurchase).getResponse().get(customerID);

        final String[] udpResponse = new String[1];
        Thread currentThread = Thread.currentThread();

        Runnable task = () -> {
            udpResponse[0] = requestRemotePurchase(otherStore1, customerID, itemID, dateOfPurchase);

            if(udpResponse[0].contains("not"))
                udpResponse[0] = requestRemotePurchase(otherStore2, customerID, itemID, dateOfPurchase);

            synchronized (currentThread){
                currentThread.notify();
            }
        };

        if(!strResponse.contains("not")){
            updateBudget(customerID,itemID,'p');
            returnResponse.setSuccessResult(true);
            response.put(customerID,strResponse);
            returnResponse.setResponse(response);
            return(returnResponse);
        }

        Thread thread = new Thread(task);
        thread.start();

        synchronized (currentThread){
            currentThread.wait();
        }

        if(udpResponse[0].contains("successfully")){
            updateBudget(customerID,itemID,'p');
            returnResponse.setSuccessResult(true);
        }
        else
            returnResponse.setSuccessResult(false);

        response.put(customerID,udpResponse[0]);
        returnResponse.setResponse(response);
        return(returnResponse);
    }

    public byte[] processRemotePurchase(DatagramPacket request, StoreServerImpl store) throws InterruptedException {
        String customerID = new String(request.getData()).substring(0, 7);
        String itemName = new String(request.getData()).substring(7, 13);
        String purchaseDate = new String(request.getData()).substring(13, 21);
        System.out.println(customerID+" "+itemName+" "+purchaseDate);
        return store.purchaseItem(customerID, itemName, purchaseDate).getResponse().get(customerID).getBytes();
    }

    public String requestRemoteLog(int portNumber, String customerID, String itemID, String dateOfPurchase, char req){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            String msg = customerID + itemID + dateOfPurchase + req;
            byte[] message = msg.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, portNumber);
            aSocket.send(request);
            System.out.println("Request message sent from the client to server with port number " + portNumber + " is: "
                    + new String(request.getData()));
            byte[] buffer = new byte[3000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            System.out.println("Reply message from port number " + reply.getPort() + " is: " + new String(reply.getData()));

            return new String(reply.getData()).substring(0, reply.getLength());
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

        return "Could Not Log Purchase";
    }

    public String runRemoteAddToLog(String customerID, String itemID, String dateOfPurchase) throws InterruptedException {
        final String[] udpResponse = new String[1];
        Thread currentThread = Thread.currentThread();
        int port = 0;

        if(customerID.substring(0,2).equalsIgnoreCase("qc"))
            port = 1099;
        else if(customerID.substring(0,2).equalsIgnoreCase("on"))
            port = 1098;
        else
            port = 1097;

        int finalPort = port;
        Runnable task = () -> {
            udpResponse[0] = requestRemoteLog(finalPort, customerID, itemID, dateOfPurchase, 'l');

            synchronized (currentThread){
                currentThread.notify();
            }
        };

        Thread thread = new Thread(task);
        thread.start();

        synchronized (currentThread){
            currentThread.wait();
        }

        return (udpResponse[0]);
    }

    public byte[] processRemoteAddToLog(DatagramPacket request, StoreServerImpl store){
        String customerID = new String(request.getData()).substring(0, 7);
        String itemID = new String(request.getData()).substring(7, 13);
        String date = new String(request.getData()).substring(13, 21);
        System.out.println(customerID+" "+itemID);
        store.logPurchase(customerID, itemID, date);
        return "Purchase logged".getBytes();
    }

    public String runRemoteAddToWait(String customerID, String itemID, String dateOfPurchase) throws InterruptedException {
        final String[] udpResponse = new String[1];
        Thread currentThread = Thread.currentThread();
        int port = 0;

        if(itemID.substring(0,2).equalsIgnoreCase("qc"))
            port = 1099;
        else if(itemID.substring(0,2).equalsIgnoreCase("on"))
            port = 1098;
        else
            port = 1097;

        int finalPort = port;
        Runnable task = () -> {
            udpResponse[0] = requestRemoteLog(finalPort, customerID, itemID, dateOfPurchase, 'w');

            synchronized (currentThread){
                currentThread.notify();
            }
        };

        Thread thread = new Thread(task);
        thread.start();

        synchronized (currentThread){
            currentThread.wait();
        }

        return (udpResponse[0]);
    }

    public byte[] processRemoteAddToWait(DatagramPacket request, StoreServerImpl store) throws InterruptedException {
        String customerID = new String(request.getData()).substring(0, 7);
        String itemID = new String(request.getData()).substring(7, 13);
        String date = new String(request.getData()).substring(13, 21);
        System.out.println(customerID+" "+itemID);
        store.addToWaitlist(customerID, itemID, date);
        return ("You have been added to the waitlist for the following item: " + itemID+"\n").getBytes();
    }

    public ReplicaResponse runRemoteReturn(String customerID, String itemID, String dateOfReturn) throws InterruptedException {
        ReplicaResponse returnResponse = new ReplicaResponse();
        returnResponse.setReplicaID(replicaID);
        HashMap<String, String> response = new HashMap<>();

        final String[] udpResponse = new String[1];
        Thread currentThread = Thread.currentThread();
        int port = 0;

        if(itemID.substring(0,2).equalsIgnoreCase("qc"))
            port = 1099;
        else if(itemID.substring(0,2).equalsIgnoreCase("on"))
            port = 1098;
        else
            port = 1097;

        int finalPort = port;
        Runnable task = () -> {
            udpResponse[0] = requestRemoteReturn(finalPort, customerID, itemID, dateOfReturn);

            synchronized (currentThread){
                currentThread.notify();
            }
        };

        Thread thread = new Thread(task);
        thread.start();

        synchronized (currentThread){
            currentThread.wait();
        }

        returnResponse.setSuccessResult(false);
        response.put(customerID,udpResponse[0]);
        returnResponse.setResponse(response);
        return(returnResponse);
    }

    public String requestRemoteReturn(int portNumber, String customerID, String itemID, String dateOfReturn){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            String msg = customerID + itemID+ dateOfReturn + 'r';
            byte[] message = msg.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, portNumber);
            aSocket.send(request);
            System.out.println("Request message sent from the client to server with port number " + portNumber + " is: "
                    + new String(request.getData()));
            byte[] buffer = new byte[3000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            System.out.println("Reply message from port number " + reply.getPort() + " is: " + new String(reply.getData()));

            return new String(reply.getData()).substring(0, reply.getLength());
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

        return "";
    }

    public byte[] processRemoteReturn(DatagramPacket request, StoreServerImpl store) throws InterruptedException, FileNotFoundException {
        String customerID = new String(request.getData()).substring(0, 7);
        String itemID = new String(request.getData()).substring(7, 13);
        String date = new String(request.getData()).substring(13, 21);
        System.out.println("RETURN:" + customerID + " " + itemID + " " + date);
        String response = store.returnItem(customerID, itemID,date).getResponse().get(customerID);
        return (response +"\n").getBytes();
    }

    //******************************************************************************************************************
    //                                              HELPER FUNCTIONS
    //******************************************************************************************************************

    public String isManager(String managerID){
        if(managerID.charAt(2) == 'M' || managerID.charAt(2) == 'm')
            return "true";
        else {
            System.out.println("You do not have the authorization to perform this task");
            return "false";
        }
    }

    public boolean within30Days(String order, String dateOfReturn){

        //String dateOfPurchase = order.substring(order.lastIndexOf("=") + 1);
        String dateOfPurchase = order;
        String thirtyOneDayMonths = "{01, 03, 05, 07, 08, 10, 12}";

        int buyMonth = Integer.parseInt(dateOfPurchase.substring(0,2));
        int returnMonth = Integer.parseInt(dateOfReturn.substring(0,2));
        int buyDate = Integer.parseInt(dateOfPurchase.substring(3,5));
        int returnDate = Integer.parseInt(dateOfReturn.substring(3,5));
        int buyYear = Integer.parseInt(dateOfPurchase.substring(6));
        int returnYear = Integer.parseInt(dateOfReturn.substring(6));

        if(returnYear - buyYear > 1 || returnMonth - buyMonth > 1)
            return false;
        else if(returnMonth - buyMonth == 0 && returnDate >= buyDate)
            return true;
        else if(returnMonth - buyMonth == 1){
            if(thirtyOneDayMonths.contains(dateOfPurchase.substring(2,4))) {
                buyDate = (buyDate + 30) % 31;
                if(returnDate <= buyDate )
                    return true;
            }
        }

        return false;
    }

    public String getStore(){
        return inventory.keySet().toString().substring(1,3);
    }

    public boolean hasMadeRemotePurchase(String customerID, String itemId){
        if(customerLog.containsKey(customerID))
            return true;
        else if(waitlist.containsKey(customerID))
            return true;
        else{
//            String remoteStore = itemId.substring(0,2);
//
//            for(int i = 0; i < customerLog.get(customerID).size(); i++){
//                if(!isSameStore(customerLog.get(customerID).get(i)))
//                    if(remoteStore.equalsIgnoreCase(customerLog.get(customerID).get(i).substring(0,2)))
//                        return true;
//            }
            return false;
        }
    }

    public int getPort(){
        if(getStore().equalsIgnoreCase("qc"))
            return 1099;
        else if(getStore().equalsIgnoreCase("on"))
            return 1098;
        else
            return 1097;
    }

    public int getPort(String store){
        if(store.equalsIgnoreCase("qc"))
            return 1099;
        else if(store.equalsIgnoreCase("on"))
            return 1098;
        else
            return 1097;
    }

    public boolean isSameStore(String id){
        if(this.store.equalsIgnoreCase(id.substring(0,2)))
            return true;
        else
            return false;
    }
}
