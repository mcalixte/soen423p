package managers;

import interfaces.IReplicaManager;
import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import infraCommunication.MessageRequest;
import infraCommunication.OperationCode;
import infraCommunication.SocketWrapper;
import replica.ClientRequest;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ReplicaManager implements IReplicaManager {

    private RegisteredReplica associatedReplica;
    private EntityAddressBook replicaInEntityForm;
	private static final int nonByzantineFailureTolerance = 3;
	private Stack<Integer> nonByzantineFailureStack;

    private List<HashMap<OperationCode, ClientRequest >> operationHistory;

	public ReplicaManager(RegisteredReplica associatedReplica, EntityAddressBook entity) {
		this.associatedReplica = associatedReplica;
		this.replicaInEntityForm = entity;
		this.nonByzantineFailureStack = new Stack<Integer>();
        nonByzantineFailureStack.setSize(nonByzantineFailureTolerance);

	}

    @Override
    public String getAssociatedReplicaName() {
        return this.associatedReplica.toString();
    }

    @Override
    public String registerNonByzFailure(int seqID) {
        try {
            //TODO: Add logging here
            nonByzantineFailureStack.push(seqID);
            return "Failure registrered";
        }catch(StackOverflowError stackOver) {
            // It means the stack is full we should restart the Replica
            nonByzantineFailureStack.empty();
            return restoreReplica(seqID);

        }catch(Exception ee) {
            System.out.println("Error while registering Non Byzantine Failure " + ee.getMessage());
        }
        return null;
    }

    @Override
    public String registerCrashFailure(int seqID) {
        try {
            return restartReplica(seqID);
        }catch(Exception ee) {
            System.out.println("Error while registerCrashFailure " + ee.getMessage());
            return ee.getMessage();
        }
    }

    @Override
    public String restoreReplica(int seqID) {
        try {
            SocketWrapper socketWrapper = new SocketWrapper(replicaInEntityForm);
            MessageRequest message = new MessageRequest(
                    OperationCode.RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION,
                    List<HashMap<OperationCode, ClientRequest >> operationHistory,
                    EntityAddressBook.SEQUENCER);
            // Set the Replica in message
            message.setRegisteredReplica(associatedReplica);
            socketWrapper.send(message, replicaInEntityForm);
        } catch (SocketException e) {
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String restartReplica(int seqID) {
        try {
            SocketWrapper socketWrapper = new SocketWrapper(replicaInEntityForm);
            MessageRequest message = new MessageRequest(
                    OperationCode.RESTART_ORDER_NOTIFICATION,
                    seqID,
                    "targetReplica: " + this.getAssociatedReplicaName() + "Command: restart replica",
                    EntityAddressBook.REPLICAS);
            // Set the Replica in message
            message.setRegisteredReplica(associatedReplica);
            socketWrapper.send(message, replicaInEntityForm);

        } catch (SocketException e) {
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
