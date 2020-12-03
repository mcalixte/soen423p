package service.stores;

import service.implementation.StoreServerImpl;
import services.item.Items;

import javax.xml.ws.Endpoint;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.List;

public class BcStoreServer {
    public static void main(String[] args) {
        try {
            HashMap<String, Items> inventory = new HashMap<String, Items>();
            HashMap<String, List<String>> waitlist = new HashMap<>();
            HashMap<String, List<String>> customerLog = new HashMap<>();
            HashMap<String, Double> customerBudget = new HashMap<>();

            StoreServerImpl store = new StoreServerImpl(inventory, customerLog, waitlist, customerBudget,"BC");
            inventory = startBCStore(inventory);

            Endpoint endpoint = Endpoint.publish("http://localhost:8085/bcStore", store);
            System.out.println("BC Store ready and waiting...");

            DatagramSocket aSocket = null;

            try {

                aSocket = new DatagramSocket(1097);
                byte[] buffer = new byte[1000];
                System.out.println("UDP Server started.....");

                while (true) {

                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    String requestData = new String(request.getData(),0,request.getLength());
                    System.out.println(requestData.charAt(requestData.length()-1));
                    byte[] response = "null".getBytes();
                    if(requestData.charAt(requestData.length()-1) == 'f')
                        response = store.retrieveItems(request, store);
                    else if(requestData.charAt(requestData.length()-1) == 'p')
                        response = store.retrieveRemotePrice(request, store);
                    else if(requestData.charAt(requestData.length()-1) == 'b')
                        response = store.processRemotePurchase(request, store);
                    else if(requestData.charAt(requestData.length()-1) == 'l')
                        response = store.processRemoteAddToLog(request, store);
                    else if(requestData.charAt(requestData.length()-1) == 'w')
                        response = store.processRemoteAddToWait(request, store);
                    else if(requestData.charAt(requestData.length()-1) == 'r')
                        response = store.processRemoteReturn(request, store);
                    System.out.println(new String(response));
                    DatagramPacket reply = new DatagramPacket(response, response.length, request.getAddress(), request.getPort());
                    aSocket.send(reply);
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        } catch(Exception e){
            System.err.println("ERROR:" + e);
            e.printStackTrace(System.out);
        }
    }

    public static HashMap<String, Items> startBCStore(HashMap<String, Items> inventory){
        Items item1 = new Items("BC1000","Phone", 100, 79.99);
        Items item2 = new Items("BC1050","Laptop", 150, 139.99);
        Items item3 = new Items("BC1100","Computer", 75, 149.99);
        Items item4 = new Items("BC1150","TV", 25, 279.99);
        Items item5 = new Items("BC1200","Monitor", 150, 139.99);

        inventory.put(item1.getItemID(),item1);
        inventory.put(item2.getItemID(),item2);
        inventory.put(item3.getItemID(),item3);
        inventory.put(item4.getItemID(),item4);
        inventory.put(item5.getItemID(),item5);


        try {
            FileWriter writer = new FileWriter("src\\logs\\server\\BCStoreLogs.txt");
            writer.write("******************** QC STORE INVENTORY ********************");
            writer.write("\r\n");   // write new line
            writer.write("ADDED: " + item1.toString() + "\r\n");
            writer.write("ADDED: " + item2.toString() + "\r\n");
            writer.write("ADDED: " + item3.toString() + "\r\n");
            writer.write("ADDED: " + item4.toString() + "\r\n");
            writer.write("ADDED: " + item5.toString() + "\r\n");
            writer.close();
            //System.out.println("Store intiated!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inventory;
    }
}
