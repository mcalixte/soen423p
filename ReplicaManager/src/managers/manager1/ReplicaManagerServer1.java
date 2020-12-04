package managers.manager1;

import managers.ReplicaManager;
import managers.utils.FrontendListenerThread;
import managers.utils.ReplicaRestorationListenerThread;
import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;

import java.net.DatagramPacket;

public class ReplicaManagerServer1 {
    public static void main(String[] args) {
        ReplicaManager replicaManager = new ReplicaManager(RegisteredReplica.ReplicaS1, EntityAddressBook.REPLICA1);

        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        ReplicaRestorationListenerThread replicaListenerThread = new ReplicaRestorationListenerThread(packet, replicaManager, EntityAddressBook.REPLICA1);

        replicaListenerThread.start();
    }
}
