package remote;

import infraCommunication.IClientRequestHandler;
import infraCommunication.MessageRequest;
import infraCommunication.OperationCode;
import infraCommunication.RequestListenerThread;
import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import replica.ClientRequest;
import replica.ReplicaResponse;
import service.StoreImpl;
import service.interfaces.StoreInterface;
import sun.swing.StringUIClientPropertyKey;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class ReplicaServer {

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

            replayAllClientRequests(messageRequest);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void replayAllClientRequests(MessageRequest messageRequest) {
        for(HashMap<OperationCode, ClientRequest> operation: messageRequest.getOperationHistory()) {
            ClientRequest clientRequest = operation.get(operation);

        }
    }
}
