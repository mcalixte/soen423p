import infraCommunication.*;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import org.omg.CORBA.IFrontendPOA;
import org.omg.CORBA.ORB;
import replica.ClientRequest;
import replica.ReplicaResponse;
import timer.SocketTimer;

import java.io.*;
import java.net.*;
import java.util.*;

public class Frontend extends IFrontendPOA {

    //Final Fields
    private int requiredAnswersForAgreement = 3;

    private SocketWrapper socket;

    private MulticastSocket socketMulticast;

    private SocketTimer responseTimeLimit;

    private boolean timerState;

    private int responseTimer = 3000;


    private ORB orb;
    private FrontEndHelper frontEndHelper = new FrontEndHelper();

    private HashMap<OperationCode, ClientRequest> requestOperation = new HashMap<>();
    private List<HashMap<OperationCode, ClientRequest>> operationHistory = new ArrayList<>();

    public Frontend() {
        try {
            this.socket = new SocketWrapper(EntityAddressBook.FRONTEND);
            this.socketMulticast = new MulticastSocket(EntityAddressBook.MANAGER.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        System.out.print("Return Message" + returnMessage.toString());
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
        System.out.print("Return Message" + returnMessage.toString());
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
        System.out.print("Return Message" + returnMessage.toString());
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
        System.out.print("Return Message" + returnMessage.toString());
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
        System.out.print("Return Message" + returnMessage.toString());
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
        System.out.print("Return Message" + returnMessage.toString());
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
        System.out.print("Return Message" + returnMessage.toString());
        return returnMessage;
    }


    @Override
    public void softwareFailure(String managerID) {
        requiredAnswersForAgreement--;
    }

    @Override
    public void replicaCrash(String managerID) {
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
     */
    public List<ReplicaResponse> receiveReplicaResponse(SocketWrapper replicaSocket) {
        List<ReplicaResponse> receivedResponses = new ArrayList<>();
        int index = 0;
//        if (!timerState) {
//            responseTimeLimit.start();
//        }
        try {
            while (index < 3) {
                ObjectInputStream is = replicaSocket.receive(8000);
                ReplicaResponse replicaResponse = (ReplicaResponse) is.readObject();
                System.out.print("Collecting Message " + replicaResponse.toString());
                receivedResponses.add(replicaResponse);
                index++;
            }
            //        if (!timerState) {
//            responseTimeLimit.stop();
//            timerState = true;
//            responseTimer = responseTimeLimit.getTimeout();
//        }
        } catch (Exception e) {
            e.printStackTrace();
            int seqID = receivedResponses.get(0).getSequenceNumber();
            System.out.print("Collecting Message " + receivedResponses.size());
            for (ReplicaResponse r : receivedResponses) {
                if (seqID != r.getSequenceNumber()) {
                    receivedResponses.remove(r);
                    System.out.print("Invalid sequence number received");
                }
            }

            return receivedResponses; //TODO Needs to be tested
        }

        System.out.print("Collecting Message line 180" + receivedResponses.size());
        int seqID = receivedResponses.get(0).getSequenceNumber();

//        for (Iterator<ReplicaResponse> it = receivedResponses.iterator(); it.hasNext(); ) {
//            ReplicaResponse r = it. next();
//            if (seqID != r.getSequenceNumber()) {
//                System.out.print("Invalid sequence number received");
//                receivedResponses.remove(r);
//            }
//        }

        return receivedResponses;
    }


    private ReplicaResponse handleReceivedResponses(List<ReplicaResponse> replicaResponseList) {
        System.out.print("MKC1");
        if (replicaResponseList == null || replicaResponseList.size() == 0) {
            HashMap<String, String> stringResponses = new HashMap<>();
            stringResponses.put("", "No replica has answered your request, try again...");
            System.out.print("MKC2");
            return new ReplicaResponse(RegisteredReplica.NONE, false, 0, stringResponses);
        }

        System.out.print("MKC3");
        int seqNumber = replicaResponseList.get(0).getSequenceNumber();
        ReplicaErrorTracker tracker = new ReplicaErrorTracker(seqNumber);

        tracker.trackPotentialErrors(replicaResponseList);

        List<RegisteredReplica> crashedReplicas = tracker.getCrashedReplicas();
        List<RegisteredReplica> erroneousReplicas = tracker.getErroneousReplicas();

        HashMap<String, String> stringResponses = new HashMap<>();
        System.out.print("MKC4");
        if (crashedReplicas.size() == 0 && erroneousReplicas.size() == 0) {
            System.out.print("MKC6");
            return replicaResponseList.get(0);
        } else if (crashedReplicas.size() != 0) {
            processesMissingResponses(crashedReplicas);
            stringResponses.put("", "System needs to be restarted and restored , wait for acknowledgment of restart and restoration...");
            System.out.print("MKC7");
            return new ReplicaResponse(RegisteredReplica.NONE, false, 0, stringResponses);
        } else if (erroneousReplicas.size() != 0) {
            processesFailuresBadResponses(erroneousReplicas);
            System.out.print("MKC8");
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
            System.out.print("MKC11");
            DatagramPacket sendPacket = getPacket(errorMessage, EntityAddressBook.MANAGER.getAddress(), EntityAddressBook.MANAGER.getPort());
            socketMulticast.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("MKC14");
    }

    private void processesMissingResponses(List<RegisteredReplica> crashedReplicas) {
        MessageRequest errorMessage = null;
        try {
            // 5. Sequence # in every single client Request
            errorMessage = new MessageRequest(OperationCode.NO_RESPONSE_RECEIVED_NOTIFICATION);
            errorMessage.setOperationHistory(operationHistory); // 1. List of all ClientRequests atm
            errorMessage.setErroneousReplicas(crashedReplicas); // 3. Which replica is suspected
            System.out.print("MKC10");
            DatagramPacket sendPacket = getPacket(errorMessage, EntityAddressBook.MANAGER.getAddress(), EntityAddressBook.MANAGER.getPort());
            socketMulticast.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("MKC13");
    }

    public static DatagramPacket getPacket(IGenericMessage request, InetAddress group, int port) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(request);

        byte[] data = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, group, port);
        return sendPacket;
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