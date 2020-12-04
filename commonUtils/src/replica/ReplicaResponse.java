package replica;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.util.HashMap;

/**
 * Client Request will be sent by the front end to the sequecer
 */
public class ReplicaResponse implements Serializable {
        private RegisteredReplica replicaID;
        private boolean success;
        private int sequenceNumber;
        private HashMap<String, String> response = new HashMap<>(); // 1st entry is the id of the entity who made the erequest, 2nd is the generic log string

        public ReplicaResponse() { }

        public ReplicaResponse(RegisteredReplica replicaID, boolean success, int sequenceNumber, HashMap<String, String> response) {
            this.replicaID = replicaID;
            this.success = success;
            this.sequenceNumber = sequenceNumber;
            this.response = response;
        }

        public RegisteredReplica getReplicaID() {
            return replicaID;
        }

        public void setReplicaID(RegisteredReplica replicaID) {
            this.replicaID = replicaID;
        }

        public boolean getSuccessResult() {
            return success;
        }

        public void setSuccessResult(boolean success) {
            this.success = success;
        }

        public HashMap<String, String> getResponse() {
            return response;
        }

        public void setResponse(HashMap<String, String> response) {
            this.response = response;
        }

        @Override
        public String toString() {
            return "ReplicaResponse [replicaID=" + replicaID + ", success=" + success + ", response=" + response + ", sequenceID="+sequenceNumber+"]" ;
        }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public DatagramPacket getPacket(EntityAddressBook networkEntity) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(this);

        byte[] data = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, networkEntity.getAddress(), networkEntity.getPort());
        return sendPacket;
    }
}
