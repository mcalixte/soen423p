package infraCommunication;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class MessageRequest {

        private OperationCode code;
        private int seqID;
        private RegisteredReplica registeredReplica;
        private String operationPrameters;
        private InetAddress addr;
        private int port;

        public MessageRequest(OperationCode code, int seq, String operationPrameters, EntityAddressBook addrInfo) throws Exception {
            if (operationPrameters.length() >= 2048) {
                throw new Exception("data payload is too large!");
            }

            this.code = code;
            this.seqID = seq;
            this.registeredReplica = RegisteredReplica.EVERYONE;
            this.operationPrameters = operationPrameters;
            this.addr = addrInfo.getAddress();
            this.port = addrInfo.getPort();
        }

        // Re-Work logic depending on how we want to structure the strings
        public MessageRequest(DatagramPacket packet) {

            String payload = new String(packet.getData(), 0, packet.getLength());

            this.code = OperationCode.fromString(payload.substring(0, payload.indexOf("\r\n")));
            payload = payload.substring(payload.indexOf("\r\n") + 2);

            this.seqID = Integer.valueOf(payload.substring(0, payload.indexOf("\r\n")));
            payload = payload.substring(payload.indexOf("\r\n") + 2);
            this.registeredReplica = RegisteredReplica.valueOf(payload.substring(0, payload.indexOf("\r\n")));

            this.operationPrameters = payload.substring(payload.indexOf("\r\n\r\n"), payload.length());
            this.addr = packet.getAddress();
            this.port = packet.getPort();
        }

        public DatagramPacket getPacket() {

            String payload = code.toString() + "\r\n" + String.valueOf(seqID) + "\r\n"
                    + registeredReplica.toString() + "\r\n\r\n" + this.operationPrameters;
            return new DatagramPacket(payload.getBytes(), payload.length(), addr, port);
        }

        public String getOperationParameters() {
            return operationPrameters;
        }

        public OperationCode getOpCode() {
            return code;
        }

        public InetAddress getAddress() {
            return addr;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return "Message{" + "code=" + code + ", seq=" + seqID + ", loc=" + registeredReplica + ", data=" + operationPrameters
                    + ", addr=" + addr + ", port=" + port + '}';
        }

        public int getSeqID() {
            return seqID;
        }

        public void setSeqID(int seqID) {
            this.seqID = seqID;
        }

        public RegisteredReplica getRegisteredReplica() {
            return registeredReplica;
        }

        public void setRegisteredReplica(RegisteredReplica registeredReplica) {
            this.registeredReplica = registeredReplica;
        }
}
