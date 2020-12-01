package tests.main;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import service.StoreImpl;
import tests.threads.BCClientThread;
import tests.threads.ManagerThread;
import tests.threads.ONClientThread;
import tests.threads.QCClientThread;

public class ConcurrencyTest {
    private static QCClientThread qcClientThread;
    private static ONClientThread onClientThread;
    private static BCClientThread bcClientThread;
    private static ManagerThread genericManager;

    private static StoreImpl quebecStore;
    private static StoreImpl britishColumbiaStore;
    private static StoreImpl ontarioStore;

    public static void main(String[] args) {
        initializeStores(args);
        runTest();
    }

    private static void runTest() {
        genericManager.run();
        onClientThread.start();
        qcClientThread.start();
        bcClientThread.start();
    }

    private static void initializeStores(String[] args) {
        try {
//            quebecStore = StoreHelper.narrow(ncRef.resolve_str("quebecStore"));
//            britishColumbiaStore = StoreHelper.narrow(ncRef.resolve_str("britishColumbiaStore"));
//            ontarioStore = StoreHelper.narrow(ncRef.resolve_str("ontarioStore"));
//
//            initializeClientThreads(quebecStore, britishColumbiaStore, ontarioStore);
        }
        catch(Exception e) {
            System.out.println("Hello Client exception: " + e);
        }
    }

    private static void initializeClientThreads(StoreImpl quebecStore, StoreImpl britishColumbiaStore, StoreImpl ontarioStore) {
        genericManager = new ManagerThread(quebecStore, britishColumbiaStore, ontarioStore);
        qcClientThread = new QCClientThread(quebecStore, ontarioStore, britishColumbiaStore);
        onClientThread = new ONClientThread(quebecStore, ontarioStore, britishColumbiaStore);
        bcClientThread = new BCClientThread(quebecStore, ontarioStore, britishColumbiaStore);
    }

    private static void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
           // e.printStackTrace();
        }
    }
}
