package managers.manager2;

import managers.ReplicaManager;
import networkEntities.RegisteredReplica;

public class ReplicaManagerServer2 {
    public static void main(String[] args) {
        ReplicaManager replicaManager = new ReplicaManager(RegisteredReplica.ReplicaS2);
    }
}
