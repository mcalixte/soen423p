package tests.threads;
import service.StoreImpl;

public class ManagerThread extends Thread {
    private StoreImpl quebecStore;
    private StoreImpl ontarioStore;
    private StoreImpl britishColumbiaStore;

    public ManagerThread(StoreImpl quebecStore, StoreImpl britishColumbiaStore, StoreImpl ontarioStore) {
        this.quebecStore = quebecStore;
        this.ontarioStore = ontarioStore;
        this.britishColumbiaStore = britishColumbiaStore;
    }

    @Override
    public void run() {
        StringBuilder response = new StringBuilder();
        response.append(britishColumbiaStore.addItem("bcm1111", "bc1111", "TEA", 2, 980.00));
        //System.out.println(response.toString());
        response.append(britishColumbiaStore.addItem("bcm1111", "bc1234", "LAPTOP", 2, 30.00));
        //System.out.println(response.toString());

        response.append(quebecStore.addItem("qcm1111", "qc1234", "TEA", 2, 30.00));
        //System.out.println(response.toString());
        response.append(quebecStore.addItem("qcm1111", "qc1111", "XYZ", 2, 40.00)+"\n");
        response.append(quebecStore.listItemAvailability("qcm1111"));

        //System.out.println(response.toString());
        System.out.println(response.toString());
    }
}
