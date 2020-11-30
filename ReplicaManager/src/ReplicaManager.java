import interfaces.IReplicaManager;
import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import infraCommunication.MessageRequest;
import infraCommunication.OperationCode;
import infraCommunication.SocketWrapper;

import java.net.SocketException;
import java.util.Stack;

public class ReplicaManager implements IReplicaManager {

    private RegisteredReplica associatedReplica;
	private static final int nonByzantineFailureTolerance = 3;
	private Stack<Integer> nonByzantineFailureStack;

	public ReplicaManager(RegisteredReplica associatedReplica) {
		this.associatedReplica = associatedReplica;
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
            SocketWrapper instance = new SocketWrapper();
            MessageRequest message = new MessageRequest(
                    OperationCode.RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION,
                    seqID,
                    "targetReplica: " + this.getAssociatedReplicaName() + "Command: restore from log from RM",
                    EntityAddressBook.SEQUENCER);
            // Set the Replica in message
            message.setRegisteredReplica(associatedReplica);

            if(!instance.send(message, 10, 1000)) {
                throw new Exception("Failed to send Restore Command to Replica");
            }else {
                return "Restore order sent";
            }

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
            SocketWrapper instance = new SocketWrapper();
            MessageRequest message = new MessageRequest(
                    OperationCode.RESTART_ORDER_NOTIFICATION,
                    seqID,
                    "targetReplica: " + this.getAssociatedReplicaName() + "Command: restart replica",
                    EntityAddressBook.REPLICAS);
            // Set the Replica in message
            message.setRegisteredReplica(associatedReplica);

            if(!instance.send(message, 10, 1000)) {
                throw new Exception("Failed to send Restore Command to Replica");
            }else {
                return "Restart order sent";
            }

        } catch (SocketException e) {
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
