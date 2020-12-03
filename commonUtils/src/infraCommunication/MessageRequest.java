package infraCommunication;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import replica.enums.ParameterType;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageRequest {

        private OperationCode code;
        private int seqID;
        private InetAddress addr;
        private int port;
        private RegisteredReplica registeredReplica;
        private HashMap<ParameterType, Object> operationParameters = new HashMap<>();


        public MessageRequest(OperationCode code, int seq, HashMap<ParameterType, Object> operationParameters, EntityAddressBook addrInfo) throws Exception {
            this.code = code;
            this.seqID = seq;
            this.registeredReplica = RegisteredReplica.EVERYONE;

            HashMap<ParameterType, Object> clonedMap = new HashMap<>();
            clonedMap.putAll(operationParameters);

            this.operationParameters = clonedMap;
            this.addr = addrInfo.getAddress();
            this.port = addrInfo.getPort();
        }

        public MessageRequest(OperationCode code, int seq, EntityAddressBook addrInfo) throws Exception {
            this.code = code;
            this.seqID = seq;
            this.registeredReplica = RegisteredReplica.EVERYONE;
            this.addr = addrInfo.getAddress();
            this.port = addrInfo.getPort();
        }

        public DatagramPacket getPacket() {

            String payload = code.toString() + "\r\n" + String.valueOf(seqID) + "\r\n"
                    + registeredReplica.toString() + "\r\n\r\n" + this.operationParameters;
            return new DatagramPacket(payload.getBytes(), payload.length(), addr, port);
        }

        public HashMap<ParameterType, Object> getOperationParameters() {
            return operationParameters;
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
            return "Message{" + "code=" + code + ", seq=" + seqID + ", loc=" + registeredReplica + ", data=" + printOperationParameters()
                    + ", addr=" + addr + ", port=" + port + '}';
        }

        public String printOperationParameters() {
            String parameters = "";
            for(Map.Entry<ParameterType, Object> entry : operationParameters.entrySet()) {
                 parameters += entry.getValue() + "\n";
            }
            return parameters;
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
