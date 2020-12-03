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

    private DatagramSocket socket;
    private MessageRequest response;

    public SocketWrapper() throws SocketException {
        this.socket = new DatagramSocket();
    }

    public MessageRequest getResponse() {
        return response;
    }

//    public boolean send(MessageRequest msg, int tries, int timeout) throws IOException {
//        System.out.println("Trying to send Sending... " + msg);
//        sendRaw(msg); // Dont catch this exception, likely to be the internal socket is bad
//
//        try {
//            MessageRequest hopefulAck = receiveRaw(timeout);
//            System.out.println("Obtained the response... " + hopefulAck);
//
//            if (hopefulAck.getOpCode() != msg.getOpCode().toAck()) {
//                throw new Exception("RUDP: Rx a message but wasnt the correct ACK OpCode");
//            }
//
//            if (hopefulAck.getRegisteredReplica() != msg.getRegisteredReplica()) {
//                throw new Exception("RUDP: Rx a message but Location did not match");
//            }
//
//            response = hopefulAck;
//
//        } catch (Exception ex) {
//            if (--tries > 0) {
//                System.out.println(" Attempt #" + tries + " failed due to: " + ex.getMessage());
//                return send(msg, tries, timeout);
//            } else {
//                System.out.println("Failed to communicate after 10 successive tries ...");
//                return false;
//            }
//        }
//        return true;
//    }
//


    public void send(IGenericMessage request, EntityAddressBook entity) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput os = new ObjectOutputStream(outputStream);
        os.writeObject(request);
        os.close();
        os.flush();

        byte[] data = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, entity.getAddress(), entity.getPort());

        socket.send(sendPacket);
    }

    public ObjectInputStream receive(int timeout) throws IOException {
        byte[] incomingData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(incomingData, incomingData.length);

        socket.receive(receivePacket);

        ByteArrayInputStream in = new ByteArrayInputStream(incomingData);
        ObjectInputStream is = new ObjectInputStream(in);

        return is;
    }
}
