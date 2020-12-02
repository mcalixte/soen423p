import infraCommunication.*;
import javafx.util.Pair;
import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;
import org.omg.CORBA.IFrontendPOA;
import org.omg.CORBA.ORB;
import replica.ClientRequest;
import replica.enums.Location;
import replica.enums.ParameterType;
import replica.enums.UserType;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Frontend extends IFrontendPOA {

    private ORB orb;
    final private SocketWrapper socket;
    private int requiredAnswersForAgreement;
    frontendListener frontendListener = new frontendListener();
    Pair<OperationCode,ClientRequest> requestOperation;
    List<Pair> operationHistory;

    public Frontend() throws SocketException {
        this.socket = new SocketWrapper();
        requiredAnswersForAgreement = 3;
        frontendListener.start();
    }

    @Override
    public String createAddItem(String managerID, String itemID, String itemName, int quantity, double price) {
        String managerPrefix = managerID.substring(0, 2);
        ClientRequest request = null;
        if(managerPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.ADD_ITEM, Location.QUEBEC, UserType.MANAGER);
        }
        if(managerPrefix.equalsIgnoreCase("bc")){
            request = new ClientRequest(OperationCode.ADD_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if(managerPrefix.equalsIgnoreCase("on")){
            request = new ClientRequest(OperationCode.ADD_ITEM, Location.ONTARIO, UserType.MANAGER);
        }


        request.addRequestDataEntry(ParameterType.MANAGERID, managerID);
        request.addRequestDataEntry(ParameterType.ITEMID, itemID);
        request.addRequestDataEntry(ParameterType.ITEMNAME, itemName);
        request.addRequestDataEntry(ParameterType.QUANTITY, quantity);
        request.addRequestDataEntry(ParameterType.PRICE, price);

        try {
            sendRequestToSequencer(OperationCode.ADD_ITEM, request);
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return getResults();
    }

    public String createRemoveItem(String managerID, String itemID, int quantity) {
        String managerPrefix = managerID.substring(0, 2);
        ClientRequest request = null;
        if(managerPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.REMOVE_ITEM, Location.QUEBEC, UserType.MANAGER);
        }
        if(managerPrefix.equalsIgnoreCase("bc")){
            request = new ClientRequest(OperationCode.REMOVE_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if(managerPrefix.equalsIgnoreCase("on")){
            request = new ClientRequest(OperationCode.REMOVE_ITEM, Location.ONTARIO, UserType.MANAGER);
        }

        request.addRequestDataEntry(ParameterType.MANAGERID, managerID);
        request.addRequestDataEntry(ParameterType.ITEMID, itemID);
        request.addRequestDataEntry(ParameterType.QUANTITY, quantity);

        try {
            sendRequestToSequencer(OperationCode.REMOVE_ITEM, request);
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return getResults();
    }
    public String createListItems(String managerID) {
        String managerPrefix = managerID.substring(0, 2);
        ClientRequest request = null;
        if(managerPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.LIST_ITEMS, Location.QUEBEC, UserType.MANAGER);
        }
        if(managerPrefix.equalsIgnoreCase("bc")){
            request = new ClientRequest(OperationCode.LIST_ITEMS, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if(managerPrefix.equalsIgnoreCase("on")){
            request = new ClientRequest(OperationCode.LIST_ITEMS, Location.ONTARIO, UserType.MANAGER);
        }

        request.addRequestDataEntry(ParameterType.MANAGERID, managerID);

        try {
            sendRequestToSequencer(OperationCode.LIST_ITEMS, request);
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return getResults();
    }
    public String createPurchaseItems(String customerID, String itemID, String dateOfPurchase) {
        String userPrefix = customerID.substring(0, 2);
        ClientRequest request = null;
        if(userPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.PURCHASE_ITEM, Location.QUEBEC, UserType.MANAGER);
        }
        if(userPrefix.equalsIgnoreCase("bc")){
            request = new ClientRequest(OperationCode.PURCHASE_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if(userPrefix.equalsIgnoreCase("on")){
            request = new ClientRequest(OperationCode.PURCHASE_ITEM, Location.ONTARIO, UserType.MANAGER);
        }

        request.addRequestDataEntry(ParameterType.CLIENTID, customerID);
        request.addRequestDataEntry(ParameterType.ITEMID, itemID);
        request.addRequestDataEntry(ParameterType.DATEOFPURCHASE, dateOfPurchase);

        try {
            sendRequestToSequencer(OperationCode.PURCHASE_ITEM, request);
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return getResults();
    }
    public String createReturnItems(String customerID, String itemID, String dateOfReturn) {
        String userPrefix = customerID.substring(0, 2);
        ClientRequest request = null;
        if(userPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.RETURN_ITEM, Location.QUEBEC, UserType.MANAGER);
        }
        if(userPrefix.equalsIgnoreCase("bc")){
            request = new ClientRequest(OperationCode.RETURN_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if(userPrefix.equalsIgnoreCase("on")){
            request = new ClientRequest(OperationCode.RETURN_ITEM, Location.ONTARIO, UserType.MANAGER);
        }

        request.addRequestDataEntry(ParameterType.CLIENTID, customerID);
        request.addRequestDataEntry(ParameterType.ITEMID, itemID);
        request.addRequestDataEntry(ParameterType.DATEOFRETURN, dateOfReturn);

        try {
            sendRequestToSequencer(OperationCode.RETURN_ITEM, request);
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return getResults();
    }
    public String createFindItem(String customerID, String itemName) {
        String userPrefix = customerID.substring(0, 2);
        ClientRequest request = null;
        if(userPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.FIND_ITEM, Location.QUEBEC, UserType.MANAGER);
        }
        if(userPrefix.equalsIgnoreCase("bc")){
            request = new ClientRequest(OperationCode.FIND_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if(userPrefix.equalsIgnoreCase("on")){
            request = new ClientRequest(OperationCode.FIND_ITEM, Location.ONTARIO, UserType.MANAGER);
        }

        request.addRequestDataEntry(ParameterType.CLIENTID, customerID);
        request.addRequestDataEntry(ParameterType.ITEMNAME, itemName);

        try {
            sendRequestToSequencer(OperationCode.FIND_ITEM, request);
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return getResults();
    }

    public String createExchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        String userPrefix = customerID.substring(0, 2);
        ClientRequest request = null;
        if(userPrefix.equalsIgnoreCase("qc")) {
            request = new ClientRequest(OperationCode.EXCHANGE_ITEM, Location.QUEBEC, UserType.MANAGER);
        }
        if(userPrefix.equalsIgnoreCase("bc")){
            request = new ClientRequest(OperationCode.EXCHANGE_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        }
        if(userPrefix.equalsIgnoreCase("on")){
            request = new ClientRequest(OperationCode.EXCHANGE_ITEM, Location.ONTARIO, UserType.MANAGER);
        }

        request.addRequestDataEntry(ParameterType.CLIENTID, customerID);
        request.addRequestDataEntry(ParameterType.NEWITEMID, newItemID);
        request.addRequestDataEntry(ParameterType.OLDITEMID, oldItemID);
        request.addRequestDataEntry(ParameterType.DATEOFEXCHANGE, dateOfExchange);

        try {
            sendRequestToSequencer(OperationCode.EXCHANGE_ITEM, request);
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return getResults();
    }


    @Override
    public void softwareFailure(String managerID) {
        requiredAnswersForAgreement--;
    }

    @Override
    public void replicaCrash(String managerID) {
        requiredAnswersForAgreement--;
    }


    private void sendRequestToSequencer(OperationCode method, ClientRequest clientRequest) throws Exception {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream);
        oo.writeObject(clientRequest);
        oo.close();
        oo.flush();

//        byte[] serializedClientRequest = bStream.toByteArray();
//        String payload = new String(serializedClientRequest);
//
//        MessageRequest messageToSend = null;
//        try {
//            messageToSend = new MessageRequest(method, 0, payload, EntityAddressBook.SEQUENCER);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        socket.send(messageToSend, 5, 1000);
    }

    private String getResults(){
        int seqNumber = Integer.valueOf(socket.getResponse().getOperationParameters().substring("SEQ=".length()));

        ConsensusTracker tracker = new ConsensusTracker(requiredAnswersForAgreement, seqNumber);

        frontendListener.setTracker(tracker);

        try {
            tracker.Wait();
        } catch (InterruptedException e) {
           e.printStackTrace();
        }

        frontendListener.setTracker(null);

        processesFailures(tracker);

        return tracker.getAnswer();
    }


    private void processesFailures(ConsensusTracker tracker) {
        processesFailuresBadResponses(tracker);
        processesMissingResponses(tracker);
    }

    private void processesFailuresBadResponses(ConsensusTracker tracker) {
        LinkedList<RegisteredReplica> inError = tracker.getFailures();

        RegisteredReplica errors[] = new RegisteredReplica[inError.size()];
        errors = inError.toArray(errors);

        MessageRequest notice = null;
        try {
            notice = new MessageRequest(OperationCode.FAULTY_RESP_RECEIVED_NOTIFICATION, 0, "Your replica Errored!", EntityAddressBook.MANAGER);
            socket.sendTo(errors, notice, 10, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processesMissingResponses(ConsensusTracker tracker) {
        LinkedList<RegisteredReplica> inError = tracker.getMissingAnswers();

        RegisteredReplica errors[] = new RegisteredReplica[inError.size()];
        errors = inError.toArray(errors);

        MessageRequest notice = null;
        try {
            notice = new MessageRequest(OperationCode.NO_RESPONSE_RECEIVED_NOTIFICATION, 0, "Your replica Errored!", EntityAddressBook.MANAGER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            socket.sendTo(errors, notice, 10, 1000);
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