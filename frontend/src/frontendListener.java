
import infraCommunication.MessageRequest;
import infraCommunication.OperationCode;
import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import replica.ReplicaResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

public class frontendListener extends Thread {

    private ReplicaErrorTracker replicaErrorTracker;
    private SocketWrapper socketWrapper;
    EntityAddressBook address = EntityAddressBook.FRONTEND;
    Boolean executingRequests = false;

    @Override
    public void run() {

        createSocket();

        while (executingRequests) {
            ClientRequest request = waitForIncomingMessage();
            ReplicaResponse response = processRequest(request);

            try {
                System.out.println("Replying... " + response);
                socketWrapper.sendTo(response.getPacket());
            } catch (IOException ex) {
                System.out.println("Failed to send message: " + ex.getMessage());
            }
            socket.close();
        }
    }

    private void createSocket() {
        try {
            socket = new DatagramSocket(address.getPort(), );
            executingRequests = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ClientRequest waitForIncomingMessage() {
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            System.out.println("Waiting to receive message");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new MessageRequest(packet);
    }
    private ReplicaResponse processRequest(ClientRequest request) {
        System.out.println("Processing new request...");
        OperationCode responseCode = request.getOpCode().toAck();

        try {
            handleRequestMessage(request);

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

        if (replicaErrorTracker != null) {
            replicaErrorTracker.trackPotentialErrors(replicaID, sequenceID, answer);
        }
    }

    public void setTracker(ReplicaErrorTracker tracker) {
        replicaErrorTracker = tracker;
    }
}