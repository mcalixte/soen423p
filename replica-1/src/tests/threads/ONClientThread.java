package tests.threads;


import service.StoreImpl;

public class ONClientThread extends Thread  {
    private StoreImpl ontarioStore;

    public ONClientThread(StoreImpl quebecStore, StoreImpl ontarioStore, StoreImpl britishColumbiaStore){
        this.ontarioStore = ontarioStore;
    }

    @Override
    public void run() {
        String response = "";
        response = ontarioStore.purchaseItem("onc1111", "qc1234", "25/10/2020 20:00")+"\n";
        System.out.println(response);
        pause(1000);
        response = ontarioStore.exchange("onc1111", "qc1111", "qc1234", "25/10/2020 20:00")+"\n";
        System.out.println(response);
        pause(1000);
        response = ontarioStore.exchange("onc1111", "bc1111", "qc1234", "25/10/2020 20:00")+"\n";
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
