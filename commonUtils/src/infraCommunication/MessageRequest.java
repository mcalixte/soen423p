package infraCommunication;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import replica.ClientRequest;
import replica.enums.ParameterType;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageRequest implements IGenericMessage {
    // Network Fields
    private InetAddress addr;
    private int port;

    // Crash related fields
    private RegisteredReplica registeredReplica;
    private HashMap<ParameterType, Object> operationParameters = new HashMap<>();
    private OperationCode errorType;

    // Restoration related fields
    private List<HashMap<OperationCode, ClientRequest>> operationHistory;

    private List<RegisteredReplica> erroneousReplicas = new ArrayList<>();

    public MessageRequest(OperationCode code, HashMap<ParameterType, Object> operationParameters, EntityAddressBook addrInfo, List<HashMap<OperationCode, ClientRequest>> operationHistory) throws Exception {
        this.errorType = code;
        this.operationHistory = operationHistory;
        this.registeredReplica = RegisteredReplica.EVERYONE;

        HashMap<ParameterType, Object> clonedMap = new HashMap<>();
        clonedMap.putAll(operationParameters);

        this.operationParameters = clonedMap;
        this.addr = addrInfo.getAddress();
        this.port = addrInfo.getPort();
    }

    public MessageRequest(OperationCode code) throws Exception {
        this.errorType = code;
        this.registeredReplica = RegisteredReplica.EVERYONE;
    }

    public MessageRequest(OperationCode faultyRespReceivedNotification, EntityAddressBook addrInfo) {
        this.errorType = faultyRespReceivedNotification;
        this.addr = addrInfo.getAddress();
        this.port = addrInfo.getPort();
    }

    public DatagramPacket getPacket() {

        String payload = errorType.toString() + registeredReplica.toString() + "\r\n\r\n" + this.operationParameters;
        return new DatagramPacket(payload.getBytes(), payload.length(), addr, port);
    }

    public HashMap<ParameterType, Object> getOperationParameters() {
        return operationParameters;
    }

    public OperationCode getOpCode() {
        return errorType;
    }

    public InetAddress getAddress() {
        return addr;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Message{" + "code=" + errorType + ", loc=" + registeredReplica + ", data=" + printOperationParameters()
                + ", addr=" + addr + ", port=" + port + '}';
    }

    public String printOperationParameters() {
        String parameters = "";
        for (Map.Entry<ParameterType, Object> entry : operationParameters.entrySet()) {
            parameters += entry.getValue() + "\n";
        }
        return parameters;
    }

    public RegisteredReplica getRegisteredReplica() {
        return registeredReplica;
    }

    public void setRegisteredReplica(RegisteredReplica registeredReplica) {
        this.registeredReplica = registeredReplica;
    }

    public List<HashMap<OperationCode, ClientRequest>> getOperationHistory() {
        return operationHistory;
    }

    public void setOperationHistory(List<HashMap<OperationCode, ClientRequest>> operationHistory) {
        this.operationHistory = operationHistory;
    }

    public List<RegisteredReplica> getErroneousReplicas() {
        return erroneousReplicas;
    }

    public void setErroneousReplicas(List<RegisteredReplica> erroneousReplicas) {
        this.erroneousReplicas = erroneousReplicas;
    }
}
