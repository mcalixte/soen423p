package tests.threads;

import service.StoreImpl;

public class QCClientThread extends Thread  {
    private StoreImpl quebecStore;
    private StoreImpl ontarioStore;
    private StoreImpl britishColumbiaStore;

    public QCClientThread(StoreImpl quebecStore, StoreImpl ontarioStore, StoreImpl britishColumbiaStore){
        this.quebecStore = quebecStore;
        this.ontarioStore = ontarioStore;
        this.britishColumbiaStore = britishColumbiaStore;
    }

    @Override
    public void run() {
        pause(1000);
        String response = "";
        response = quebecStore.purchaseItem("qcc1111", "qc1234", "25/10/2020 20:00")+"\n";
        System.out.println(response);
        pause(2000);
        response = quebecStore.exchange("qcc1111", "qc1111", "qc1234","25/10/2020 20:00")+"\n";
        System.out.println(response);
    }

    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
           // e.printStackTrace();
        }
    }
}
