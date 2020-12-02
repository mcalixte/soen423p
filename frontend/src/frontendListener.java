
import infraCommunication.OperationCode;
import infraCommunication.RequestListenerThread;
import networkEntities.EntityAddressBook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;

/**
 *
 * @author cmcarthur
 */
public class frontendListener implements RequestListenerThread{

    final private RequestListener m_Listener;
    private ConsensusTracker m_ConsensusTracker;
    private Thread m_ListenerThread;

    public Listener() throws SocketException {
        m_Listener = new RequestListener(this, EntityAddressBook.FRONTEND);
    }

    public void launch() {
        m_ListenerThread = new Thread(m_Listener);
        m_ListenerThread.start();
        m_Listener.Wait(); // Make sure it's running before getting any farther
        System.out.println("Front-End started to listen for UDP requests on port: " + EntityAddressBook.FRONTEND.getPort());
    }

    public void shutdown() throws InterruptedException {
        m_Listener.Stop();
        m_ListenerThread.join();
    }

    @Override
    public String handleRequestMessage(MessageRequest msg) throws Exception {
        if (msg.getOpCode() == OperationCode.OPERATION_RETVAL && msg.getData().contains("ReplicaResponse")) {
            ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(msg.getData().getBytes()));
            Object input = iStream.readObject();
            ReplicaResponse replicaResponse;
            iStream.close();

            if (input instanceof ReplicaResponse) {
                replicaResponse = (ReplicaResponse) input;
            } else {
                throw new IOException("Data received is not valid.");
            }

            int sequenceID = msg.getSeqNum();
            String answer = replicaResponse.getResponse();
            RegisteredReplica replicaID = replicaResponse.getReplicaID();

            if (m_ConsensusTracker != null) {
                m_ConsensusTracker.addRequestConsensus(replicaID, sequenceID, answer);
            }
        }
        return "";
    }

    /**
     *
     * @param tracker
     */
    public void setTracker(ConsensusTracker tracker) {
        m_ConsensusTracker = tracker;
    }
}