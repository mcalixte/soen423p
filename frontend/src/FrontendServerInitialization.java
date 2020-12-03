import java.util.stream.Stream;

import networkEntities.EntityAddressBook;
import org.omg.CORBA.IFrontend;
import org.omg.CORBA.IFrontendHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


public class FrontendServerInitialization {
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            Frontend frontEnd = new Frontend();
            frontEnd.setOrb(orb);

            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(frontEnd);
            IFrontend href = IFrontendHelper.narrow(ref);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent path[] = ncRef.to_name(EntityAddressBook.FRONTEND.getShortHandName());
            ncRef.rebind(path, href);

            System.out.println("The Front-end is now running on port 1050 ...");

            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}