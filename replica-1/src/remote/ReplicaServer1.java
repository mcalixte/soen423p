package remote;

import infraCommunication.IClientRequestHandler;
import infraCommunication.MessageRequest;
import infraCommunication.OperationCode;
import infraCommunication.RequestListenerThread;
import networkEntities.EntityAddressBook;
import replica.ClientRequest;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.HashMap;

public class ReplicaServer1 {

    public static void main(String[] args) {
        System.out.println("Replica Server Started...");

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        IClientRequestHandler clientRequestHandler = new ClientRequestHandler();
        RequestListenerThread requestListenerThread = new RequestListenerThread(receivePacket, clientRequestHandler, EntityAddressBook.FRONTEND, EntityAddressBook.ALLREPLICAS, EntityAddressBook.REPLICA1);
        requestListenerThread.run();

        sendRestorationRequestAndRestore();
    }

    private static void sendRestorationRequestAndRestore() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(EntityAddressBook.REPLICA1.getPort());

            MessageRequest messageRequest = new MessageRequest(OperationCode.RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION);
            DatagramPacket senderPacket = messageRequest.getPacket(EntityAddressBook.MANAGER1);
            datagramSocket.send(senderPacket);

            byte[] data = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
            datagramSocket.receive(datagramPacket);

            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            messageRequest = (MessageRequest) is.readObject();
            System.out.println("Message Request Received R1: "+ messageRequest.toString());
            replayAllClientRequests(messageRequest);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void replayAllClientRequests(MessageRequest messageRequest) {
        try {
            MulticastSocket multicastSock = new MulticastSocket();
            for(HashMap<OperationCode, ClientRequest> operation: messageRequest.getOperationHistory()) {
                ClientRequest clientRequest = operation.get(operation);

            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(messageRequest);
            byte[] data = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, EntityAddressBook.ALLREPLICAS.getPort());
            multicastSock.send(sendPacket);
        } catch (IOException e) {
           System.out.println(e.getMessage());
        }



       
    }
}
