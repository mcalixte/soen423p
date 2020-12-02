package remote;

import infraCommunication.IClientRequestHandler;
import infraCommunication.RequestListenerThread;
import networkEntities.EntityAddressBook;
import replica.ReplicaResponse;
import service.StoreImpl;
import service.interfaces.StoreInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.DatagramPacket;
import java.net.URL;

public class ReplicaServer {

    public static void main(String[] args) {
        System.out.println("Replica Server Started...");

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        IClientRequestHandler clientRequestHandler = new ClientRequestHandler();
        RequestListenerThread requestListenerThread = new RequestListenerThread(receivePacket, clientRequestHandler, EntityAddressBook.FRONTEND, EntityAddressBook.SEQUENCER);

    }



    ///////////////////////////////////////////
    ///     Web Services remote Methods     ///
    ///////////////////////////////////////////


}
