package udp;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class MessageRequest {

        private OperationCode m_Code;
        private int m_SeqNum;
        private RegisteredReplica m_Location;
        private String m_operationPrameters;
        private InetAddress m_Addr;
        private int m_Port;

        public MessageRequest(OperationCode code, int seq, String operationPrameters, EntityAddressBook addrInfo) throws Exception {
            if (operationPrameters.length() >= 2048) {
                throw new Exception("data payload is too large!");
            }

            this.m_Code = code;
            this.m_SeqNum = seq;
            this.m_Location = RegisteredReplica.EVERYONE;
            this.m_operationPrameters = operationPrameters;
            this.m_Addr = addrInfo.getAddr();
            this.m_Port = addrInfo.getPort();
        }

        public MessageRequest(OperationCode code, int seq, String operationPrameters, InetAddress address, int port) throws Exception {
            if (operationPrameters.length() >= 2048) {
                throw new Exception("data payload is too large!");
            }

            this.m_Code = code;
            this.m_SeqNum = seq;
            this.m_Location = RegisteredReplica.EVERYONE;
            this.m_operationPrameters = operationPrameters;
            this.m_Addr = address;
            this.m_Port = port;
        }

        // Re-Work logic depending on how we want to structure the strings
        protected MessageRequest(DatagramPacket packet) {

            String payload = new String(packet.getData(), 0, packet.getLength());

            this.m_Code = OperationCode.fromString(payload.substring(0, payload.indexOf("\r\n")));
            payload = payload.substring(payload.indexOf("\r\n") + 2);

            this.m_SeqNum = Integer.valueOf(payload.substring(0, payload.indexOf("\r\n")));
            payload = payload.substring(payload.indexOf("\r\n") + 2);
            this.m_Location = RegisteredReplica.valueOf(payload.substring(0, payload.indexOf("\r\n")));

            this.m_operationPrameters = payload.substring(payload.indexOf("\r\n\r\n"), payload.length());
            this.m_Addr = packet.getAddress();
            this.m_Port = packet.getPort();
        }

        public DatagramPacket getPacket() {

            String payload = m_Code.toString() + "\r\n" + String.valueOf(m_SeqNum) + "\r\n"
                    + m_Location.toString() + "\r\n\r\n" + this.m_operationPrameters;
            return new DatagramPacket(payload.getBytes(), payload.length(), m_Addr, m_Port);
        }

        public String getOperationParameters() {
            return m_operationPrameters;
        }

        public OperationCode getOpCode() {
            return m_Code;
        }

        public InetAddress getAddress() {
            return m_Addr;
        }

        public int getPort() {
            return m_Port;
        }

        @Override
        public String toString() {
            return "Message{" + "code=" + m_Code + ", seq=" + m_SeqNum + ", loc=" + m_Location + ", data=" + m_operationPrameters
                    + ", addr=" + m_Addr + ", port=" + m_Port + '}';
        }

        public int getSeqNum() {
            return m_SeqNum;
        }

        public void setSeqNum(int seqNum) {
            this.m_SeqNum = seqNum;
        }

        public RegisteredReplica getLocation() {
            return m_Location;
        }

        public void setLocation(RegisteredReplica location) {
            this.m_Location = location;
        }
}
