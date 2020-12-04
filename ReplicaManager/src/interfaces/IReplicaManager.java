package interfaces;

import infraCommunication.MessageRequest;

import javax.swing.*;

public interface IReplicaManager {

        public String getAssociatedReplicaName();

        public void registerNonMaliciousByzantineFailure(MessageRequest messageRequest);

        public void registerCrashFailure(MessageRequest messageRequest) ;

        public void restoreReplica(MessageRequest messageRequest);

        public void restartReplica(MessageRequest messageRequest);
}
