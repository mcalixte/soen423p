package replica;

import replica.enums.Location;
import replica.enums.UserType;

import java.io.Serializable;
import java.util.HashMap;

public class ClientRequest implements Serializable {

    private String method;
    private Location location; // TODO Will be parsed by the replica server in order to direct the request towards a store
    private UserType userType;
    private int sequenceNumber;
    private HashMap<String, Object> methodParameters = new HashMap<>();

    public ClientRequest(String method, Location location, UserType userType) {
        this.method = method;
        this.location = location;
        this.userType = userType;
    }

    public ClientRequest(String method, Location location, UserType userType, int sequenceNumber) {
        this.method = method;
        this.location = location;
        this.userType = userType;
        this.sequenceNumber = sequenceNumber;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public HashMap<String, Object> getMethodParameters() {
        return methodParameters;
    }

    public void addRequestDataEntry(String fieldName, Object value) {
        methodParameters.put(fieldName, value);
    }

    @Override
    public String toString() {
        return "ClientRequest [method=" + method + ", location=" + location + ", data=" + methodParameters + "]";
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
