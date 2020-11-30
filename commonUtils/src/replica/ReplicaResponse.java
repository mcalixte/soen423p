package replica;

import networkEntities.RegisteredReplica;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Client Request will be sent by the front end to the sequecer
 */
public class ReplicaResponse implements Serializable {
        private RegisteredReplica replicaID;
        private boolean success;
        private HashMap<String, String> response; // 1st entry is the id of the entity who made th erequest, 2nd is the generic log string

        public ReplicaResponse(RegisteredReplica replicaID, boolean success, HashMap<String, String> response) {
            this.replicaID = replicaID;
            this.success = success;
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
            return "ReplicaResponse [replicaID=" + replicaID + ", success=" + success + ", response=" + response + "]";
        }
}
