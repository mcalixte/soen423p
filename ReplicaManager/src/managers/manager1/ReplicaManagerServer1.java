package managers.manager1;

import managers.ReplicaManager;
import networkEntities.RegisteredReplica;

public class ReplicaManagerServer1 {
    public static void main(String[] args) {
        ReplicaManager replicaManager = new ReplicaManager(RegisteredReplica.ReplicaS1);
    }
}
