package remote;

import infraCommunication.IClientRequestHandler;
import infraCommunication.RequestListenerThread;
import networkEntities.EntityAddressBook;
import replica.ClientRequest;
import replica.ReplicaResponse;
import service.StoreImpl;
import service.interfaces.StoreInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;

public class ReplicaServer {

    public static void main(String[] args) {
        System.out.println("Replica Server Started...");

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        IClientRequestHandler clientRequestHandler = new ClientRequestHandler();
        RequestListenerThread requestListenerThread = new RequestListenerThread(receivePacket, clientRequestHandler, EntityAddressBook.FRONTEND, EntityAddressBook.ALLREPLICAS);
        requestListenerThread.run();
    }
}
