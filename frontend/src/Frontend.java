import infraCommunication.*;
import javafx.util.Pair;
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

    private ORB orb;
    final private SocketWrapper socket;
    private FrontEndHelper frontEndHelper;
    private int requiredAnswersForAgreement;
    private frontendListener frontendListener = new frontendListener();
    Pair<OperationCode, ClientRequest> requestOperation;
    List<Pair> operationHistory;
    DatagramSocket sequencerSocket;

    public Frontend() throws SocketException {
        frontEndHelper = new FrontEndHelper();
        this.socket = new SocketWrapper();
        requiredAnswersForAgreement = 3;
        sequencerSocket = new DatagramSocket();
    }

    @Override
    public String createAddItem(String managerID, String itemID, String itemName, int quantity, double price) {
        ClientRequest request = frontEndHelper.createAddItem(managerID, itemID, itemName, quantity, price);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.ADD_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createRemoveItem(String managerID, String itemID, int quantity) {
        ClientRequest request = frontEndHelper.createRemoveItem(managerID,itemID,quantity);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.REMOVE_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createListItems(String managerID) {
        ClientRequest request = frontEndHelper.createListItems(managerID);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.LIST_ITEMS, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createPurchaseItems(String customerID, String itemID, String dateOfPurchase) {
        ClientRequest request = frontEndHelper.createPurchaseItems(customerID,itemID,dateOfPurchase);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.PURCHASE_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createReturnItems(String customerID, String itemID, String dateOfReturn) {
        ClientRequest request = frontEndHelper.createReturnItems(customerID, itemID, dateOfReturn);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.RETURN_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createFindItem(String customerID, String itemName) {
        ClientRequest request = frontEndHelper.createFindItem(customerID,itemName);
        ReplicaResponse response = handleReceivedResponses(sendRequestToSequencer(OperationCode.FIND_ITEM, request));
        String returnMessage = "";
        for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
            returnMessage = entry.getValue();
        }
        return returnMessage;
    }

    public String createExchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        ClientRequest request = frontEndHelper.createExchangeItem(customerID, newItemID, oldItemID, dateOfExchange);
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
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutput os = new ObjectOutputStream(outputStream);
            os.writeObject(clientRequest);
            os.close();
            os.flush();

            byte[] data = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, EntityAddressBook.SEQUENCER.getAddress(), EntityAddressBook.SEQUENCER.getPort());
            sequencerSocket.send(sendPacket);

            //TODO Create a method that listens for all responses and collects them BEFORE doing consensus handling
            //METHOD wil handle grouping the responses by sequence number
            List<ReplicaResponse> list = receiveReplicaResponse(sequencerSocket, sendPacket);

            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<ReplicaResponse>();
    }

    public List<ReplicaResponse> receiveReplicaResponse(DatagramSocket replicaSocket, DatagramPacket receivePacket) {
        List<ReplicaResponse> receivedResponses = new ArrayList<>();
        int index = 3;
        while (index > 0) {
            try {
                byte[] incomingData = new byte[1024];
                replicaSocket.setSoTimeout(3000);
                replicaSocket.receive(receivePacket);
                byte[] data = receivePacket.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream is = new ObjectInputStream(in);

                ReplicaResponse replicaResponse = (ReplicaResponse) is.readObject();
                receivedResponses.add(replicaResponse);
                index--;
            } catch (Exception e) {
                int seqID = receivedResponses.get(0).getSequenceNumber();
                 for(ReplicaResponse r : receivedResponses) {
                     if(seqID != r.getSequenceNumber()) {
                      receivedResponses.remove(r);
                      System.out.print("Invalid sequence number received");
                    }
                 }
                return receivedResponses; //TODO Needs to be tested
            }
        }

        int seqID = receivedResponses.get(0).getSequenceNumber();
        for(ReplicaResponse r : receivedResponses)
        {
            if(seqID != r.getSequenceNumber()) {
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

        if(crashedReplicas.size() == 0 && erroneousReplicas.size() == 0)
            return replicaResponseList.get(0);
        else if(crashedReplicas.size() != 0) {
            processesMissingResponses(crashedReplicas);
            stringResponses.put("", "System needs to be restarted and restored , wait for acknowledgment of restart and restoration...");
            return new ReplicaResponse(RegisteredReplica.NONE, false, 0, stringResponses);
        }
        else if(erroneousReplicas.size() != 0) {
            processesFailuresBadResponses(erroneousReplicas);
            stringResponses.put("", "System needs to be  restored due to erroneous answers, wait for acknowledgment of restoration...");
            return new ReplicaResponse(RegisteredReplica.NONE, false, 0, stringResponses);
        }
        return new ReplicaResponse();
    }


    private void processesFailuresBadResponses(List<RegisteredReplica> erroneousReplicas) {
        MessageRequest notice = null;
        try {
            notice = new MessageRequest(OperationCode.FAULTY_RESP_RECEIVED_NOTIFICATION, 0, EntityAddressBook.MANAGER);
            socket.sendTo(erroneousReplicas, notice, 10, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processesMissingResponses(List<RegisteredReplica> crashedReplicas) {

        MessageRequest notice = null;
        try {
            notice = new MessageRequest(OperationCode.NO_RESPONSE_RECEIVED_NOTIFICATION, 0, EntityAddressBook.MANAGER);
            socket.sendTo(crashedReplicas, notice, 10, 1000);
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