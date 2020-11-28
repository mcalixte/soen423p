package networkEntities;

import java.net.InetAddress;
import java.net.UnknownHostException;

public enum EntityAddressBook {
        FRONTEND("Front-End", "FE", "127.0.0.1", 14000),
        SEQUENCER("Sequencer", "SEQ", "127.0.0.1", 15000),
        MANAGER("Replica-Manager", "RM", "127.0.0.1", 16000),
        REPLICAS("Replica-Instance", "PI", "127.0.0.1", 18000);

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

        public InetAddress getAddr() {
            return m_Addr;
        }

        public int getPort() { return m_Port; }

        @Override
        public String toString() {
            return m_Name;
        }

}
