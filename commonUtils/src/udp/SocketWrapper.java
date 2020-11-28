package udp;

import networkEntities.RegisteredReplica;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class SocketWrapper {

        private DatagramSocket socket;
        private MessageRequest response;

        public SocketWrapper() throws SocketException {
            this.socket = new DatagramSocket();
        }

        public MessageRequest getResponse() {
            return response;
        }

        public boolean send(MessageRequest msg, int tries, int timeout) throws IOException {
            System.out.println("Trying to send Sending... " + msg);
            sendRaw(msg); // Dont catch this exception, likely to be the internal socket is bad

            try {
                MessageRequest hopefulAck = receiveRaw(timeout);
                System.out.println("Obtained the response... " + hopefulAck);

                if (hopefulAck.getOpCode() != msg.getOpCode().toAck()) {
                    throw new Exception("RUDP: Rx a message but wasnt the correct ACK OpCode");
                }

                if (hopefulAck.getLocation() != msg.getLocation()) {
                    throw new Exception("RUDP: Rx a message but Location did not match");
                }

                response = hopefulAck;

            } catch (Exception ex) {
                if (--tries > 0) {
                    System.out.println(" Attempt #" + tries + " failed due to: " + ex.getMessage());
                    return send(msg, tries, timeout);
                } else {
                    System.out.println("Failed to communicate after 10 successive tries ...");
                    return false;
                }
            }
            return true;
        }


        //For when you need to communicate with a location in particular
        public boolean sendTo(RegisteredReplica[] locations, MessageRequest msg, int retryCounter, int timeout) throws Exception {
            boolean retval = true;

            for (RegisteredReplica loc : locations) {
                if (loc == RegisteredReplica.EVERYONE) {
                    continue;
                }

                msg.setLocation(loc);
                if (!send(msg, retryCounter, timeout)) {
                    retval = false;
                }
            }

            return retval;
        }

        private void sendRaw(MessageRequest request) throws IOException {
            socket.send(request.getPacket());
        }

        private MessageRequest receiveRaw(int timeout) throws IOException {
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            socket.setSoTimeout(timeout); // Set timeout in case packet is dropped by network, which shouldnt happen locally
            socket.receive(packet);

            return new MessageRequest(packet);
        }
}
