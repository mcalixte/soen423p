package infraCommunication;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import replica.ClientRequest;
import replica.ReplicaResponse;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

public class SocketWrapper {

    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private MessageRequest response;

    public SocketWrapper(EntityAddressBook entity) throws SocketException {
        this.receiverSocket = new DatagramSocket(entity.getPort(), entity.getAddress());
        this.senderSocket =  new DatagramSocket();
    }

    public MessageRequest getResponse() {
        return response;
    }

    public void send(IGenericMessage request, EntityAddressBook entity) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput os = new ObjectOutputStream(outputStream);
        os.writeObject(request);
        os.close();
        os.flush();

        byte[] data = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, entity.getAddress(), entity.getPort());
        
        senderSocket.send(sendPacket);
    }

    public ObjectInputStream receive(int timeout) {
        byte[] incomingData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(incomingData, incomingData.length);
        try {
            receiverSocket.setSoTimeout(10000);
            receiverSocket.receive(receivePacket);
            ByteArrayInputStream in = new ByteArrayInputStream(incomingData);
            ObjectInputStream is = new ObjectInputStream(in);

            return is;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;


    }
}
