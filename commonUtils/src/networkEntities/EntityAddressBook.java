package networkEntities;

import java.net.InetAddress;
import java.net.UnknownHostException;

public enum EntityAddressBook {
        CLIENT("Front-End", "FE", "225.4.5.5", 13000),
        FRONTEND("Front-End", "FE", "225.4.5.6", 14000),
        SEQUENCER("Sequencer", "SEQ", "127.0.0.1", 15000),
        MANAGER("Replica-Manager", "RM", "225.4.5.8", 16000),
        ALLREPLICAS("ALL-REPLICAS", "ALL", "225.4.5.9", 18000),
        REPLICA1("REPLICA-INSTANCE-1", "RI-1", "225.4.5.10", 18001),
        REPLICA2("REPLICA-INSTANCE-2", "RI-2", "225.4.5.11", 18002),
        REPLICA3("REPLICA-INSTANCE-3", "RI-3", "225.4.5.12", 18003);

        private InetAddress m_Addr;
        private final int m_Port;
        private final String m_Name;
        private final String m_ShortHand;

        private EntityAddressBook(String name, String shortHand, String addr, int port) {
            m_Name = name;
            m_ShortHand = shortHand;
            try {
                m_Addr = InetAddress.getByName(addr);
            } catch (UnknownHostException ex) {
                System.out.println("Could not get this address... " + ex.getMessage());
                m_Addr = InetAddress.getLoopbackAddress();
            }
            m_Port = port;
        }

        public String getShortHandName() {
            return m_ShortHand;
        }

        public InetAddress getAddress() {
            return m_Addr;
        }

        public int getPort() { return m_Port; }

        @Override
        public String toString() {
            return m_Name;
        }

}
