package managers.utils;

import infraCommunication.MessageRequest;
import managers.ReplicaManager;
import networkEntities.EntityAddressBook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;

public class ReplicaRestorationListenerThread extends Thread{
    private MulticastSocket serverReceiverSocket;
    private DatagramSocket datagramSenderSocket;
    private DatagramPacket incomingPacket;
    private ReplicaManager replicaManager;
    private EntityAddressBook targetNetworkEntity;
    private EntityAddressBook sourceNetworkEntity;

    public ReplicaRestorationListenerThread(DatagramPacket incomingPacket, ReplicaManager replicaManager, EntityAddressBook targetNetworkEntity) {
        this.incomingPacket = incomingPacket;
        this.replicaManager = replicaManager;
        this.replicaManager = replicaManager;
        this.targetNetworkEntity = targetNetworkEntity;
    }

    public void run() {
        createSockets();
        while (true) {
            MessageRequest messageRequest = receiveIncomingMessageRequest();

            MessageRequest response = null;
            if(messageRequest != null)
                processRequest(messageRequest);

            try {
                System.out.println("Replying... " + response);

                if (response != null) {
                    datagramSenderSocket.send(response.getPacket(targetNetworkEntity));
                }
            } catch(IOException ex){
                System.out.println("Failed to send message: " + ex.getMessage());
            }
        }
    }



    private void createSockets() {
        try {
            datagramSenderSocket = new DatagramSocket();
            serverReceiverSocket = new MulticastSocket(EntityAddressBook.MANAGER.getPort());
            serverReceiverSocket.joinGroup(EntityAddressBook.MANAGER.getAddress());
        } catch (IOException ex) {
            System.out.println("Failed to create socket due to: " + ex.getMessage());
        }
    }

    private void processRequest(MessageRequest messageRequest) {
        System.out.println("Processing new request...");

        try {
             replicaManager.handleRequestMessage(messageRequest);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private MessageRequest receiveIncomingMessageRequest() {
        try {
            MessageRequest messageRequest;
            serverReceiverSocket.receive(incomingPacket);

            byte[] data = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            ObjectInputStream is = new ObjectInputStream(in);
            messageRequest = (MessageRequest) is.readObject();
            is.close();
            return messageRequest;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
