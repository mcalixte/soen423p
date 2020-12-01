package udp;

import networkEntities.EntityAddressBook;
import networkEntities.RegisteredReplica;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RequestListener implements Runnable {

    public interface Processor {

        public String handleRequestMessage(udp.MessageRequest msg) throws Exception;
    }

    private EntityAddressBook m_Address;
    private RegisteredReplica m_InstanceId;

    final private Processor m_Handler;

    private boolean m_ShouldContinueWorking;
    private boolean m_ProcessingHasBegun;

    private MulticastSocket m_Socket;

    public RequestListener(Processor handler, EntityAddressBook address) {
        m_Handler = handler;
        m_Address = address;
        m_InstanceId = RegisteredReplica.EVERYONE;
        m_ShouldContinueWorking = false;
        m_ProcessingHasBegun = false;
    }

    public RequestListener(Processor handler, EntityAddressBook address, RegisteredReplica instanceId) {
        m_Handler = handler;
        m_Address = address;
        m_InstanceId = instanceId;
        m_ShouldContinueWorking = false;
        m_ProcessingHasBegun = false;
    }

    public void Stop() {
        m_ShouldContinueWorking = false;
        m_ProcessingHasBegun = false;
        m_Socket.close();
    }

    public void Wait() {
        while (m_ProcessingHasBegun == false) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Wait();
            }
        }
    }

    @Override
    public void run() {
        createSocket();

        if (m_ShouldContinueWorking) {
            System.out.println("Ready...");
            m_ProcessingHasBegun = true;
        }

        while (m_ShouldContinueWorking) {
            MessageRequest request = waitForIncommingMessage();

            if (request == null) {
                break;
            }

            MessageRequest response = null;

            try {
                response = processRequest(request);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (response == null) {
                continue;
            }

            try {
                System.out.println("Replying... " + response);
                m_Socket.send(response.getPacket());
            } catch (IOException ex) {
                System.out.println("Failed to send message: " + ex.getMessage());
            }
        }

        m_Socket.close();
    }

    private void createSocket() {
        try {
            m_Socket = new MulticastSocket(m_Address.getPort());

            m_Socket.joinGroup(m_Address.getAddr());
            m_ShouldContinueWorking = true;
        } catch (IOException ex) {
            m_ShouldContinueWorking = false;
            System.out.println("Failed to create socket due to: " + ex.getMessage());
        }
    }

    private MessageRequest waitForIncommingMessage() {
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            m_Socket.receive(packet);
            System.out.println("UDP.RequestListener.waitForIncommingMessage()");
        } catch (IOException ex) {
            System.out.println("Failed to receive message due to: " + ex.getMessage());
            return null;
        }

        return new MessageRequest(packet);
    }

    private MessageRequest processRequest(MessageRequest request) throws Exception {
        boolean isLocationImportant = (m_InstanceId != RegisteredReplica.EVERYONE);
        boolean isForSomeone = (request.getLocation() != RegisteredReplica.EVERYONE);
        boolean isForNotMe = (request.getLocation() != m_InstanceId);
        if (isLocationImportant && isForSomeone && isForNotMe) {
            System.out.println("Dropping request for [ " + request.getLocation().toString() + " ]");
            return null;
        }

        System.out.println("Processing new request...");
        String responsePayload;
        OperationCode responseCode = request.getOpCode().toAck();

        try {
            responsePayload = m_Handler.handleRequestMessage(request);
        } catch (Exception ex) {
            System.out.println("Handler failed to process request!");
            responseCode = OperationCode.INVALID;
            responsePayload = ex.getMessage();
        }

        InetAddress address = request.getAddress();
        int port = request.getPort();

        return new MessageRequest(responseCode, request.getSeqNum(), responsePayload, address, port);
    }
}
