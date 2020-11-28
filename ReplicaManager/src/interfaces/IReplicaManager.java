package interfaces;

public interface IReplicaManager {

        public String getAssociatedReplicaName();

        public String registerNonByzFailure(int seqId);

        public String registerCrashFailure(int seqId) ;

        public String restoreReplica(int seqID);

        public String restartReplica(int seqID);
}
