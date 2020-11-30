package replica;

import java.io.Serializable;
import java.util.HashMap;

public class ClientRequest implements Serializable {

    private String method;
    private String location; // TODO Will be parsed by the replica server in order to direct the request towards a store
    private HashMap<String, Object> methodParameters = new HashMap<>();

    public ClientRequest(String method, String location) {
        this.method = method;
        this.location = location;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
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
}
