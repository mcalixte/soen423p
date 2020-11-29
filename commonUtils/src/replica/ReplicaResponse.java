package replica;

import networkEntities.RegisteredReplica;

import java.io.Serializable;

public class ReplicaResponse implements Serializable {
        private RegisteredReplica replicaID;
        private boolean success;
        private String response;

        public ReplicaResponse(RegisteredReplica replicaID, boolean success, String response) {
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

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        @Override
        public String toString() {
            return "ReplicaResponse [replicaID=" + replicaID + ", success=" + success + ", response=" + response + "]";
        }
}
