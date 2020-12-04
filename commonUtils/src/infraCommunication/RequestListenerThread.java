package infraCommunication;

import networkEntities.EntityAddressBook;
import replica.ClientRequest;
import replica.ReplicaResponse;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.Arrays;

public class RequestListenerThread extends Thread {
    private MulticastSocket serverSocket;
    private DatagramSocket datagramSocket;
    private DatagramPacket incomingPacket;
    private IClientRequestHandler clientRequestHandler;
    private EntityAddressBook targetNetworkEntity;
    private EntityAddressBook sourceNetworkEntity;

    public RequestListenerThread(DatagramPacket incomingPacket, IClientRequestHandler clientRequestHandler, EntityAddressBook targetNetworkEntity, EntityAddressBook sourceNetworkEntity) {
        this.incomingPacket = incomingPacket;
        this.clientRequestHandler = clientRequestHandler;
        this.targetNetworkEntity = targetNetworkEntity;
        this.sourceNetworkEntity = sourceNetworkEntity;
    }


    public void run() {
        createSockets();
        while (true) {
            ClientRequest clientRequest = receiveIncomingClientRequest();

            ReplicaResponse replicaResponse = null;
            if(clientRequest != null)
                replicaResponse =  processRequest(clientRequest);

            try {
                System.out.println("Replying... " + replicaResponse);

                if (replicaResponse != null) {
                    datagramSocket.send(replicaResponse.getPacket(targetNetworkEntity));
                }
                } catch(IOException ex){
                    System.out.println("Failed to send message: " + ex.getMessage());
                }
            }
        }



    private void createSockets() {
        try {
            datagramSocket = new DatagramSocket();
            serverSocket = new MulticastSocket(sourceNetworkEntity.getPort());
            serverSocket.joinGroup(sourceNetworkEntity.getAddress());
        } catch (IOException ex) {
            System.out.println("Failed to create socket due to: " + ex.getMessage());
        }
    }

    private ReplicaResponse processRequest(ClientRequest clientRequest) {
        System.out.println("Processing new request...");

        ReplicaResponse response = null;
        try {
            response = clientRequestHandler.handleRequestMessage(clientRequest);
        } catch(Exception e){
          e.printStackTrace();
        }
        response.setSequenceNumber(clientRequest.getSequenceNumber());
        return response;
    }

    private ClientRequest receiveIncomingClientRequest() {
        try {
            ClientRequest clientRequest;
            serverSocket.receive(incomingPacket);


            byte[] data = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            ObjectInputStream is = new ObjectInputStream(in);
            clientRequest = (ClientRequest) is.readObject();
            is.close();
            return clientRequest;

        } catch (Exception e) {
//            System.out.println("PurchaseItem Exception: " + e);
             e.printStackTrace();
        }
        return null;
    }

}
