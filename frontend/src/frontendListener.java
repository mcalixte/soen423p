
import infraCommunication.MessageRequest;
import infraCommunication.OperationCode;
import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import replica.ReplicaResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;

public class frontendListener extends Thread {

    private ConsensusTracker consensusTracker;
    private MulticastSocket socket;
    EntityAddressBook address = EntityAddressBook.FRONTEND;
    RegisteredReplica instanceID = RegisteredReplica.EVERYONE;

    @Override
    public void run() {

        createSocket();

        MessageRequest request = waitForIncommingMessage();

        MessageRequest response = processRequest(request);

            try {
                System.out.println("Replying... " + response);
                socket.send(response.getPacket());
            } catch (IOException ex) {
                System.out.println("Failed to send message: " + ex.getMessage());
            }
        socket.close();
        }

    private void createSocket() {
        try {
            socket = new MulticastSocket(address.getPort());
            socket.joinGroup(address.getAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MessageRequest waitForIncommingMessage() {
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            System.out.println("UDP.RequestListener.waitForIncommingMessage()");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new MessageRequest(packet);
    }
    private MessageRequest processRequest(MessageRequest request) {
        System.out.println("Processing new request...");
        String responsePayload;
        OperationCode responseCode = request.getOpCode().toAck();

        try {
            handleRequestMessage(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        InetAddress address = request.getAddress();
        int port = request.getPort();
        try {
            return new MessageRequest(responseCode, request.getSeqID(),"",  EntityAddressBook.MANAGER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void handleRequestMessage(MessageRequest msg) throws Exception {
        ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(msg.getOperationParameters().getBytes()));
        Object input = iStream.readObject();
        ReplicaResponse replicaResponse;
        iStream.close();

        if (input instanceof ReplicaResponse) {
            replicaResponse = (ReplicaResponse) input;
        } else {
            throw new IOException("Data received is not valid.");
        }

        int sequenceID = msg.getSeqID();

        String answer = "";
        for (Map.Entry<String, String> response : replicaResponse.getResponse().entrySet()) {
            answer = response.getValue();
        }
        RegisteredReplica replicaID = replicaResponse.getReplicaID();

        if (consensusTracker != null) {
            consensusTracker.addRequestConsensus(replicaID, sequenceID, answer);
        }
    }

    public void setTracker(ConsensusTracker tracker) {
        consensusTracker = tracker;
    }
}