package replica;

import infraCommunication.IGenericMessage;
import infraCommunication.OperationCode;
import replica.enums.Location;
import replica.enums.ParameterType;
import replica.enums.UserType;

import java.io.Serializable;
import java.util.HashMap;

public class ClientRequest implements Serializable, IGenericMessage {

    private OperationCode method;
    private Location location; // TODO Will be parsed by the replica server in order to direct the request towards a store
    private UserType userType;
    private int sequenceNumber;
    private HashMap<ParameterType, Object> methodParameters = new HashMap<>();

    public ClientRequest(OperationCode method, Location location, UserType userType) {
        this.method = method;
        this.location = location;
        this.userType = userType;
    }

    public ClientRequest(OperationCode method, Location location, UserType userType, int sequenceNumber) {
        this.method = method;
        this.location = location;
        this.userType = userType;
        this.sequenceNumber = sequenceNumber;
    }

    public OperationCode getMethod() {
        return method;
    }

    public void setMethod(OperationCode method) {
        this.method = method;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public HashMap<ParameterType, Object> getMethodParameters() {
        return methodParameters;
    }

    public void addRequestDataEntry(ParameterType fieldName, Object value) {
        methodParameters.put(fieldName, value);
    }

    @Override
    public String toString() {
        return "ClientRequest [method=" + method + ", location=" + location + ", data=" + methodParameters + ", seqeunceID="+sequenceNumber+"]";
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public UserType getUserType() {
        return userType;
    }
}
