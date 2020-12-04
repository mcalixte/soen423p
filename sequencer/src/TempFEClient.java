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

        ClientRequest req = new ClientRequest(OperationCode.PURCHASE_ITEM, Location.BRITISHCOLUMBIA, UserType.CUSTOMER);
        req.addRequestDataEntry(ParameterType.CLIENTID,"BCU1212");
        req.addRequestDataEntry(ParameterType.ITEMID,"QC1100");
        req.addRequestDataEntry(ParameterType.DATEOFPURCHASE,"12/3/2020");

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
