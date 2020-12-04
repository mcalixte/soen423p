package remote;

import infraCommunication.RequestListenerThread;
import networkEntities.EntityAddressBook;
import infraCommunication.IClientRequestHandler;

import java.net.DatagramPacket;

public class ReplicaServer3 {

        public static void main(String[] args) {
            System.out.println("Replica Server Started...");

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            IClientRequestHandler clientRequestHandler = new ClientRequestHandler();
            RequestListenerThread requestListenerThread = new RequestListenerThread(receivePacket, clientRequestHandler, EntityAddressBook.FRONTEND, EntityAddressBook.ALLREPLICAS);
            requestListenerThread.start();
        }
    }
