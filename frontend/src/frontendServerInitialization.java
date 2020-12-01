import java.net.SocketException;
import java.util.stream.Stream;

import networkEntities.EntityAddressBook;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.CORBA.Frontend;
import org.omg.CORBA.FrontendHelper;

public class frontendServerInitialization {
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);

            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            frontend frontEnd = new frontend();

            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(frontEnd);
            Frontend href = FrontendHelper.narrow(ref);

            NameComponent path[] = ncRef.to_name(EntityAddressBook.FRONTEND.getShortHandName());
            ncRef.rebind(path, href);

            System.out.println("The Front-end is now running on port 1050 ...");

            orb.run();

            frontEnd.shutdown();

        } catch (Exception e) {
            System.out.println("Failed to start the Front-End");
            e.printStackTrace();
        }
    }
}