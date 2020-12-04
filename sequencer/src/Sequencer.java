import java.io.*;
import java.net.*;
import java.util.HashMap;

import infraCommunication.OperationCode;
import networkEntities.EntityAddressBook;
import replica.ClientRequest;
import replica.enums.Location;
import replica.enums.ParameterType;
import replica.enums.UserType;


public class Sequencer {

    private static int sequenceID = 0;

    public static void main(String[] args) {
        while(true) {
            ClientRequest request = awaitClientRequest();
            System.out.print(request);
            if(request != null) {
                try {
                    InetAddress group = EntityAddressBook.ALLREPLICAS.getAddress();
                    MulticastSocket multicastSock = new MulticastSocket();
                    DatagramPacket packet = getPacket(request, group, EntityAddressBook.ALLREPLICAS.getPort());
                    multicastSock.send(packet);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static DatagramPacket getPacket(ClientRequest request, InetAddress group, int port) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(request);

        byte[] data = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, group, port);
        return sendPacket;
    }

    public static ClientRequest awaitClientRequest(){
        DatagramSocket aSocket;

        try {
            //TODO update address
            aSocket = new DatagramSocket();

            byte[] buffer = new byte[1024];
            System.out.println("UDP Server started.....");

            DatagramPacket request = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("127.0.0.1"), 10000);
            aSocket.receive(request);

            byte[] data = request.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);

            ClientRequest clientRequest = (ClientRequest) is.readObject();
            clientRequest.setSequenceNumber(++sequenceID);
            is.close();
            return clientRequest;

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
