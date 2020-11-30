package tests.threads;

import service.StoreImpl;

public class BCClientThread extends Thread  {
    private StoreImpl quebecStore;
    private StoreImpl ontarioStore;
    private StoreImpl britishColumbiaStore;

    public BCClientThread(StoreImpl quebecStore, StoreImpl ontarioStore, StoreImpl britishColumbiaStore){
        this.quebecStore = quebecStore;
        this.ontarioStore = ontarioStore;
        this.britishColumbiaStore = britishColumbiaStore;
    }

    @Override
    public void run() {
        pause(2000);
        String result = britishColumbiaStore.purchaseItem("bcc1111", "qc1234", "25/10/2020 20:00");
        System.out.println(result);
    }


    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
           // e.printStackTrace();
        }
    }
}
