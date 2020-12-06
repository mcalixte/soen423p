package infraCommunication;

import networkEntities.EntityAddressBook;
import replica.ClientRequest;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ReplayThread extends Thread{
    private EntityAddressBook replica;
    private IClientRequestHandler clientRequestHandler;
    private DatagramSocket replaySocket;

    public ReplayThread(EntityAddressBook replica, IClientRequestHandler clientRequestHandler) {
        this.replica = replica;
        this.clientRequestHandler = clientRequestHandler;
        try {
            this.replaySocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {
            ClientRequest clientRequest = receiveIncomingReplayRequest();
            clientRequestHandler.handleRequestMessage(clientRequest);
        }
    }

    private ClientRequest receiveIncomingReplayRequest() {
        try {
            ClientRequest clientRequest;
            byte[] data = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(data, data.length);
            replaySocket.receive(incomingPacket);


            byte[] incomingData = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(incomingData);

            ObjectInputStream is = new ObjectInputStream(in);
            clientRequest = (ClientRequest) is.readObject();
            is.close();
            return clientRequest;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
