package managers.manager3;

import managers.ReplicaManager;
import networkEntities.RegisteredReplica;

public class ReplicaManagerServer3 {
    public static void main(String[] args) {
        ReplicaManager replicaManager = new ReplicaManager(RegisteredReplica.ReplicaS3);
    }
}
