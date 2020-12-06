package infraCommunication;

import networkEntities.EntityAddressBook;
import replica.ClientRequest;
import replica.ReplicaResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;

public class RequestListenerThread extends Thread {
    private EntityAddressBook replica;
    private MulticastSocket multicastReceiverSocket;
    private DatagramSocket replaySocket;
    private DatagramSocket datagramSocket;
    private DatagramPacket incomingPacket;
    private IClientRequestHandler clientRequestHandler;
    private EntityAddressBook targetNetworkEntity;
    private EntityAddressBook sourceNetworkEntity;

    public RequestListenerThread(DatagramPacket incomingPacket, IClientRequestHandler clientRequestHandler, EntityAddressBook targetNetworkEntity, EntityAddressBook sourceNetworkEntity, EntityAddressBook replica) {
        this.incomingPacket = incomingPacket;
        this.clientRequestHandler = clientRequestHandler;
        this.targetNetworkEntity = targetNetworkEntity;
        this.sourceNetworkEntity = sourceNetworkEntity;
        this.replica = replica;
    }


    public void run() {
        createSockets();
        while (true) {
            ClientRequest clientRequest = receiveIncomingClientRequest();
//            ReplayThread replayThread = new ReplayThread(replica, clientRequestHandler);
//            replayThread.run();

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
            System.out.println(replica.getPort()+" "+replica.getAddress());
            replaySocket = new DatagramSocket(replica.getPort(), replica.getAddress());

            multicastReceiverSocket = new MulticastSocket(sourceNetworkEntity.getPort());
            multicastReceiverSocket.joinGroup(sourceNetworkEntity.getAddress());
        } catch (IOException ex) {
            System.out.println("Failed to create socket due to: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
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
            multicastReceiverSocket.receive(incomingPacket);
            System.out.println("MKC1 Receiving incoming clientRequest");

            byte[] data = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            ObjectInputStream is = new ObjectInputStream(in);
            clientRequest = (ClientRequest) is.readObject();
            is.close();
            System.out.println("MKC2: HERE");
            return clientRequest;

        } catch (Exception e) {
             e.printStackTrace();
        }
        return null;
    }



}
