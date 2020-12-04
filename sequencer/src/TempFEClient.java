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

        ClientRequest req = new ClientRequest(OperationCode.FIND_ITEM, Location.QUEBEC, UserType.CUSTOMER);
        req.addRequestDataEntry(ParameterType.CLIENTID,"QCU1212");
        req.addRequestDataEntry(ParameterType.ITEMNAME,"RAM");

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
