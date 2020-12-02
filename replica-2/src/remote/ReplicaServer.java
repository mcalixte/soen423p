package remote;

import infraCommunication.IClientRequestHandler;
import infraCommunication.RequestListenerThread;
import networkEntities.EntityAddressBook;
import replica.ClientRequest;
import replica.ReplicaResponse;
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

    public static void main(String[] args) throws IOException {
        System.out.println("Replica Server Started...");

        InetAddress group = EntityAddressBook.ALLREPLICAS.getAddress();
        MulticastSocket multicastSock = new MulticastSocket(5000);
        multicastSock.joinGroup(group);

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        multicastSock.receive(receivePacket);

        try {
            System.out.print(byteToClientRequest(receiveData).getLocation());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


//        IClientRequestHandler clientRequestHandler = new ClientRequestHandler();
//        RequestListenerThread requestListenerThread = new RequestListenerThread(receivePacket, clientRequestHandler, EntityAddressBook.FRONTEND, EntityAddressBook.SEQUENCER);
//        requestListenerThread.start();
    }

    public static ClientRequest byteToClientRequest(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objStream = new ObjectInputStream(byteStream);
        return (ClientRequest) objStream.readObject();
    }



    ///////////////////////////////////////////
    ///     Web Services remote Methods     ///
    ///////////////////////////////////////////


}
