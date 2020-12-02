import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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
        ClientRequest req = new ClientRequest(OperationCode.FIND_ITEM, Location.QUEBEC, UserType.CUSTOMER);
        req.addRequestDataEntry(ParameterType.CLIENTID,"QCU1212");
        req.addRequestDataEntry(ParameterType.ITEMNAME,"RAM");
        req.setSequenceNumber(++sequenceID);

        try{
            InetAddress group = EntityAddressBook.ALLREPLICAS.getAddress();
            MulticastSocket multicastSock = new MulticastSocket();
            String msg = "Hello World\n";
            DatagramPacket packet = getPacket(req, group, 5000);
            multicastSock.send(packet);
            multicastSock.close();
        } catch (IOException e) {
            e.printStackTrace();
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
}
