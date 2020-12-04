import infraCommunication.*;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import org.omg.CORBA.IFrontendPOA;
import org.omg.CORBA.ORB;
import replica.ClientRequest;
import replica.ReplicaResponse;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frontend extends IFrontendPOA {

    //Final Fields
    private int requiredAnswersForAgreement = 3;
    private final SocketWrapper socket = new SocketWrapper();

    private ORB orb;
    private FrontEndHelper frontEndHelper = new FrontEndHelper();

    private HashMap<OperationCode, ClientRequest> requestOperation = new HashMap<>();
    private List<HashMap<OperationCode, ClientRequest>> operationHistory = new ArrayList<>();

    public Frontend() throws SocketException {
    }

    @Override
    public String createAddItem(String managerID, String itemID, String itemName, int quantity, double price) {
        ClientRequest request = frontEndHelper.createAddItem(managerID, itemID, itemName, quantity, price);
        requestOperation.put(OperationCode.ADD_ITEM, request);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.ADD_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createRemoveItem(String managerID, String itemID, int quantity) {
        ClientRequest request = frontEndHelper.createRemoveItem(managerID, itemID, quantity);
        requestOperation.put(OperationCode.REMOVE_ITEM, request);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.REMOVE_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createListItems(String managerID) {
        ClientRequest request = frontEndHelper.createListItems(managerID);
        requestOperation.put(OperationCode.LIST_ITEMS, request);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.LIST_ITEMS, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createPurchaseItems(String customerID, String itemID, String dateOfPurchase) {
        ClientRequest request = frontEndHelper.createPurchaseItems(customerID, itemID, dateOfPurchase);
        requestOperation.put(OperationCode.PURCHASE_ITEM, request);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.PURCHASE_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createReturnItems(String customerID, String itemID, String dateOfReturn) {
        ClientRequest request = frontEndHelper.createReturnItems(customerID, itemID, dateOfReturn);
        requestOperation.put(OperationCode.RETURN_ITEM, request);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.RETURN_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createFindItem(String customerID, String itemName) {
        ClientRequest request = frontEndHelper.createFindItem(customerID, itemName);
        requestOperation.put(OperationCode.FIND_ITEM, request);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.FIND_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createExchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        ClientRequest request = frontEndHelper.createExchangeItem(customerID, newItemID, oldItemID, dateOfExchange);
        requestOperation.put(OperationCode.EXCHANGE_ITEM, request);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.EXCHANGE_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }


    @Override
    public void softwareFailure(String managerID) {
        requiredAnswersForAgreement--;
    }

    @Override
    public void replicaCrash(String managerID) {
        requiredAnswersForAgreement--;
    }


    private List<ReplicaResponse> sendRequestToSequencer(OperationCode method, ClientRequest clientRequest) {
        try {
            //TODO Create a method that listens for all responses and collects them BEFORE doing consensus handling
            //METHOD wil handle grouping the responses by sequence number
            socket.send(clientRequest, EntityAddressBook.SEQUENCER);
            List<ReplicaResponse> list = receiveReplicaResponse(socket);

            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<ReplicaResponse>();
    }

    /**
     * Initially just wait for 3 seconds for the first packet, if its not enough, time and reset timeout
     * */
    public List<ReplicaResponse> receiveReplicaResponse(SocketWrapper replicaSocket) {
        List<ReplicaResponse> receivedResponses = new ArrayList<>();
        int index = 3;

        try {
            while (index > 0) {
                ObjectInputStream is = replicaSocket.receive(3000);
                ReplicaResponse replicaResponse = (ReplicaResponse) is.readObject();
                receivedResponses.add(replicaResponse);
                index--;
            }
        } catch (Exception e) {
            int seqID = receivedResponses.get(0).getSequenceNumber();
            for (ReplicaResponse r : receivedResponses) {
                if (seqID != r.getSequenceNumber()) {
                    receivedResponses.remove(r);
                    System.out.print("Invalid sequence number received");
                }
            }
            return receivedResponses; //TODO Needs to be tested
        }


        int seqID = receivedResponses.get(0).getSequenceNumber();
        for (ReplicaResponse r : receivedResponses) {
            if (seqID != r.getSequenceNumber()) {
                System.out.print("Invalid sequence number received");
                receivedResponses.remove(r);
            }
        }
        return receivedResponses;
    }


    private ReplicaResponse handleReceivedResponses(List<ReplicaResponse> replicaResponseList) {
        if (replicaResponseList == null || replicaResponseList.size() == 0) {
            HashMap<String, String> stringResponses = new HashMap<>();
            stringResponses.put("", "No replica has answered your request, try again...");
            return new ReplicaResponse(RegisteredReplica.NONE, false, 0, stringResponses);
        }

        int seqNumber = replicaResponseList.get(0).getSequenceNumber();
        ReplicaErrorTracker tracker = new ReplicaErrorTracker(seqNumber);

        tracker.trackPotentialErrors(replicaResponseList);

        List<RegisteredReplica> crashedReplicas = tracker.getCrashedReplicas();
        List<RegisteredReplica> erroneousReplicas = tracker.getErroneousReplicas();

        HashMap<String, String> stringResponses = new HashMap<>();

        if (crashedReplicas.size() == 0 && erroneousReplicas.size() == 0)
            return replicaResponseList.get(0);
        else if (crashedReplicas.size() != 0) {
            processesMissingResponses(crashedReplicas);
            stringResponses.put("", "System needs to be restarted and restored , wait for acknowledgment of restart and restoration...");
            return new ReplicaResponse(RegisteredReplica.NONE, false, 0, stringResponses);
        } else if (erroneousReplicas.size() != 0) {
            processesFailuresBadResponses(erroneousReplicas);
            return tracker.getValidReplicas().get(0);
        }
        return new ReplicaResponse();
    }


    private void processesFailuresBadResponses(List<RegisteredReplica> erroneousReplicas) {
        MessageRequest errorMessage = null;
        try {
            // 5. Sequence # in every single client Request
            errorMessage = new MessageRequest(OperationCode.FAULTY_RESP_RECEIVED_NOTIFICATION); // 4. Who to send this to ==> manager    // 2. What type of failure happened
            errorMessage.setOperationHistory(operationHistory); // 1. List of all ClientRequests atm
            errorMessage.setErroneousReplicas(erroneousReplicas); // 3. Which replica is suspected

            socket.send(errorMessage, EntityAddressBook.MANAGER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processesMissingResponses(List<RegisteredReplica> crashedReplicas) {
        MessageRequest errorMessage = null;
        try {
            // 5. Sequence # in every single client Request
            errorMessage = new MessageRequest(OperationCode.NO_RESPONSE_RECEIVED_NOTIFICATION);
            errorMessage.setOperationHistory(operationHistory); // 1. List of all ClientRequests atm
            errorMessage.setErroneousReplicas(crashedReplicas); // 3. Which replica is suspected
            socket.send(errorMessage, EntityAddressBook.MANAGER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        orb.shutdown(false);
    }

    public ORB getOrb() {
        return orb;
    }

    public void setOrb(ORB orb) {
        this.orb = orb;
    }
}