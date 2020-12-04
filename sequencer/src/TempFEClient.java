import infraCommunication.OperationCode;
import networkEntities.EntityAddressBook;
import replica.ClientRequest;
import replica.enums.Location;
import replica.enums.ParameterType;
import replica.enums.UserType;

import java.io.*;
import java.net.*;

public class TempFEClient {
    public static void main(String[] args) {

        ClientRequest req = new ClientRequest(OperationCode.ADD_ITEM, Location.BRITISHCOLUMBIA, UserType.MANAGER);
        req.addRequestDataEntry(ParameterType.ITEMID,"BC1100");
        req.addRequestDataEntry(ParameterType.ITEMNAME,"Finn");
        req.addRequestDataEntry(ParameterType.QUANTITY,69);
        req.addRequestDataEntry(ParameterType.PRICE,69.00);
        req.addRequestDataEntry(ParameterType.MANAGERID,"BCM1100");

        DatagramSocket aSocket;

        try {
            aSocket = new DatagramSocket();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(req);
            byte[] data = outputStream.toByteArray();

            //TODO update address
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, EntityAddressBook.SEQUENCER.getAddress(), EntityAddressBook.SEQUENCER.getPort());
            aSocket.send(sendPacket);

            System.out.println("UDP Server started.....");

            aSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
