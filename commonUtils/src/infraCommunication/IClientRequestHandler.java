package infraCommunication;

import replica.ClientRequest;
import replica.ReplicaResponse;

import java.io.FileNotFoundException;

public interface IClientRequestHandler {
    public ReplicaResponse handleRequestMessage(ClientRequest clientRequest) throws InterruptedException, FileNotFoundException;
    public void instantiateStoreServers();
}
