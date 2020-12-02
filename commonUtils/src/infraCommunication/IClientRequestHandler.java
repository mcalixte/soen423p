package infraCommunication;

import replica.ClientRequest;
import replica.ReplicaResponse;

public interface IClientRequestHandler {
    public ReplicaResponse handleRequestMessage(ClientRequest clientRequest);
    public void instantiateStoreServers();
}
