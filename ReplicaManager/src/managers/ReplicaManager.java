package managers;

import interfaces.IReplicaManager;
import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import infraCommunication.MessageRequest;
import infraCommunication.OperationCode;
import infraCommunication.SocketWrapper;
import replica.ClientRequest;
import replica.ReplicaResponse;
import replica.enums.ParameterType;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ReplicaManager implements IReplicaManager {

    private RegisteredReplica associatedReplica;
    private EntityAddressBook replicaInEntityForm;
	private static final int nonByzantineFailureTolerance = 1;
	private Stack<MessageRequest> nonByzantineFailureStack;


    private List<HashMap<OperationCode, ClientRequest >> operationHistory;

	public ReplicaManager(RegisteredReplica associatedReplica, EntityAddressBook entity) {
		this.associatedReplica = associatedReplica;
		this.replicaInEntityForm = entity;

		this.nonByzantineFailureStack = new Stack<>();
        nonByzantineFailureStack.setSize(nonByzantineFailureTolerance);
	}

    @Override
    public String getAssociatedReplicaName() {
        return this.associatedReplica.toString();
    }

    @Override
    public void registerNonMaliciousByzantineFailure(MessageRequest messageRequest) {
        try {
            //TODO: Add logging here
            nonByzantineFailureStack.push(messageRequest);
            System.out.println("Failure registrered");
        }catch(StackOverflowError stackOver) {
            // It means the stack is full we should restore the incorrect Replica
            nonByzantineFailureStack.empty();
            restoreReplica(messageRequest);

        }catch(Exception ee) {
            System.out.println("Error while registering Non Byzantine Failure " + ee.getMessage());
        }
    }

    @Override
    public void registerCrashFailure(MessageRequest messageRequest) {
        try {
            restartReplica(messageRequest);
        }catch(Exception ee) {
            System.out.println("Error while registerCrashFailure " + ee.getMessage());
        }
    }



    private EntityAddressBook returnProperEntity(RegisteredReplica registeredReplica) {
	    switch (registeredReplica) {
            case ReplicaS1:
                return EntityAddressBook.REPLICA1;

            case ReplicaS2:
                return EntityAddressBook.REPLICA2;
            case ReplicaS3:
                return EntityAddressBook.REPLICA3;
        }
	    return EntityAddressBook.ALLREPLICAS;
    }

    @Override
    public void restoreReplica(MessageRequest messageRequest) {
        try {
            SocketWrapper socketWrapper = new SocketWrapper(replicaInEntityForm);

            MessageRequest message = new MessageRequest(
                    OperationCode.RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION,
                    returnProperEntity(messageRequest.getRegisteredReplica()),
                    messageRequest.getOperationHistory());

            setOperationHistory(messageRequest.getOperationHistory());
            // Set the Replica in message
            message.setRegisteredReplica(associatedReplica);
            socketWrapper.send(message, replicaInEntityForm);
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public void restartReplica(MessageRequest messageRequest) {
        try {
            SocketWrapper socketWrapper = new SocketWrapper(replicaInEntityForm);
            MessageRequest message = new MessageRequest(
                    OperationCode.RESTART_ORDER_NOTIFICATION,
                    returnProperEntity(messageRequest.getRegisteredReplica()),
                    messageRequest.getOperationHistory());
            
            setOperationHistory(messageRequest.getOperationHistory());
            // Set the Replica in message
            message.setRegisteredReplica(associatedReplica);
            socketWrapper.send(message, replicaInEntityForm);

        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleRequestMessage(MessageRequest messageRequest) {
	    switch(messageRequest.getOpCode()) {
            case RESTART_ORDER_NOTIFICATION:
                registerCrashFailure(messageRequest);
                break;
            case RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION:
                restoreReplica(messageRequest);
                break;
        }
    }

    public List<HashMap<OperationCode, ClientRequest>> getOperationHistory() {
        return operationHistory;
    }

    public void setOperationHistory(List<HashMap<OperationCode, ClientRequest>> operationHistory) {
        this.operationHistory = operationHistory;
    }
}
