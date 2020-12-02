import networkEntities.EntityAddressBook;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastReceiver {
   // public void receiveMulticastRequest(){
   public static void main(String[] args) {
        try{
            InetAddress group = EntityAddressBook.REPLICA2.getAddress();
            MulticastSocket multicastSock = new MulticastSocket(5000);
            multicastSock.joinGroup(group);

            byte[] buffer = new byte[100];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            multicastSock.receive(packet);

            System.out.print(new String(buffer));

            multicastSock.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
