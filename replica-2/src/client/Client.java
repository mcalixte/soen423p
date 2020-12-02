package client;

import service.interfaces.StoreInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Scanner;

public class Client {
    static StoreInterface store;

    public static void main(String[] args){
        try{

            String name = "";
            Scanner in = new Scanner(System.in);

            while(true) {
                System.out.println("Please enter your ID to gain access >");
                String id = in.nextLine();

                if(validID(id)) {
                    if(id.substring(0,2).equalsIgnoreCase("qc")) {
                        URL qcURL = new URL("http://localhost:8080/qcStore?wsdl");
                        QName qcQName = new QName("http://implementation.server/", "StoreServerImplService");
                        Service qcService = Service.create(qcURL, qcQName);
                        store = qcService.getPort(StoreInterface.class);

                        if (id.toLowerCase().charAt(2) == 'm')
                            runManager(id);
                        else
                            runUser(id);
                    }
                    else if(id.substring(0,2).equalsIgnoreCase("on")) {
                        URL onURL = new URL("http://localhost:8081/onStore?wsdl");
                        QName onQName = new QName("http://implementation.server/", "StoreServerImplService");
                        Service onService = Service.create(onURL, onQName);
                        store = onService.getPort(StoreInterface.class);

                        if(id.toLowerCase().charAt(2) == 'm')
                            runManager(id);
                        else
                            runUser(id);
                    }
                    else {
                        URL bcURL = new URL("http://localhost:8082/bcStore?wsdl");
                        QName bcQName = new QName("http://implementation.server/", "StoreServerImplService");
                        Service bcService = Service.create(bcURL, bcQName);
                        store = bcService.getPort(StoreInterface.class);

                        if(id.toLowerCase().charAt(2) == 'm')
                            runManager(id);
                        else
                            runUser(id);
                    }
                }
                else
                    System.out.println("Invalid ID");

                if (id.equalsIgnoreCase("exit")) break;


            }


        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }

    public static void runUser(String id) {

        double budget = 1000.00;

        Scanner in = new Scanner(System.in);
        while(true) {
            System.out.println("What do you want to do? ( Buy - Find - Return - Exchange) > ");
            String request = in.nextLine();

            if(request.equalsIgnoreCase("exit")) break;

            try {
                if(request.equalsIgnoreCase("buy")) {

                    System.out.println("Please enter the following information.");

                    System.out.println("Item ID >");
                    String itemID = in.nextLine();
                    System.out.println("Date >");
                    String date = in.nextLine();

                    double price = store.getRemotePrice(itemID);
                    if (budget >= price) {
                        String response = store.runRemotePurchase(id, itemID, date).getResponse().get("response");
                        System.out.println(response + "\n");

                        if (response.contains("stock")) {
                            System.out.println("Would you like to be added to the waitlist for this item? (Yes/No) >");
                            if (in.nextLine().equalsIgnoreCase("yes"))
                                System.out.println(store.addToWaitlist(id, itemID, date));
                        }

                        budget = budget - price;
                    } else
                        System.out.println("You do not have the funds to make this purchase.");



                }

                else if(request.equalsIgnoreCase("return")) {

                    System.out.println("Please enter the following information.");

                    System.out.println("Item ID >");
                    String itemID = in.nextLine();
                    System.out.println("Date >");
                    String date = in.nextLine();
                    String response = store.returnItem(id, itemID, date).getResponse().get("response");
                    System.out.println(response);

                    if(response.contains("successful"))
                        budget += store.getItemPrice(id);

                }

                else if (request.equalsIgnoreCase("find")){

                    System.out.println("Please enter the name of the item >");
                    String itemName = in.nextLine();
                    String response = store.findItemRequest(id,itemName).getResponse().get("response");

                    if(response.contains("QC") || response.contains("ON") || response.contains("BC")) {
                        response = response.replace(",","\n");
                        System.out.println(response);
                    }
                    else
                        System.out.println("No such item available\n");

                }

                else if(request.equalsIgnoreCase("exchange")){
                    System.out.println("Please enter the following information.");

                    System.out.println("Old Item's ID >");
                    String oldItemID = in.nextLine();
                    System.out.println("New Item's ID >");
                    String newItemID = in.nextLine();
                    System.out.println("Date >");
                    String date = in.nextLine();
                    String response = store.exchangeItem(id, newItemID, oldItemID, date).getResponse().get("response");
                    System.out.println(response);

                    if(response.contains("successful"))
                        budget = (budget + store.getItemPrice(oldItemID)) - store.getItemPrice(newItemID);

                }

                else if(request.equalsIgnoreCase("balance")){
                    System.out.println("Your current ballance is: " + budget + "\n");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static void runManager(String id){

        Scanner in = new Scanner(System.in);
        while(true) {
            System.out.println("What do you want to do? ( Add - List - Remove) >");
            String request = in.nextLine();

            if(request.equalsIgnoreCase("exit")) break;

            try {
                if(request.equalsIgnoreCase("add")) {

                    System.out.println("Please enter the following information.");

                    if(store.isManager(id).equals("true")) {
                        System.out.println("Item ID >");
                        String itemID = in.nextLine();
                        System.out.println("Item Name >");
                        String itemName = in.nextLine();
                        System.out.println("Item Quantity >");
                        int quantity = in.nextInt();
                        System.out.println("Item Price >");
                        double price = in.nextDouble();

                        System.out.println(store.addItem(id, itemID, itemName, quantity, price).getResponse().get("response"));
                    }
                    else
                        System.out.println("You do not have the authorization to perform this task");
                }

                else if(request.equalsIgnoreCase("remove")) {

                    System.out.println("Please enter the following information.");
                    if(store.isManager(id).equals("true")) {
                        System.out.println("Item ID >");
                        String itemID = in.nextLine();
                        System.out.println("Item Quantity >");
                        int quantity = in.nextInt();

                        System.out.println(store.removeItem(id, itemID, quantity).getResponse().get("response") + "\n");
                    }
                    else
                        System.out.println("You do not have the authorization to perform this task");
                }

                else if (request.equalsIgnoreCase("list")){
                    if(store.isManager(id).equals("true")) {
                        String [] tempInv;
                        System.out.println("The following is the store inventory");
                        System.out.println("-------------------------------------");
                        String tempCleanUp = store.listItemAvailability(id).getResponse().get("response").substring(1,store.listItemAvailability(id).getResponse().get("response").length() -2 );
                        tempInv = tempCleanUp.split(";,");
                        for (String item: tempInv) {
                            System.out.println(item);
                        }
                        System.out.println("-------------------------------------");
                    }
                    else
                        System.out.println("You do not have the authorization to perform this task");
                }
                else if (request.equalsIgnoreCase("wait")){
                    if(store.isManager(id).equals("true")) {
                        String [] tempInv;
                        System.out.println("The following is the store's waitlist");
                        System.out.println("-------------------------------------");
                        String tempCleanUp = store.showWaitlist();
                        tempInv = tempCleanUp.split("/");
                        for (String item: tempInv) {
                            System.out.println(item);
                        }
                        System.out.println("-------------------------------------");
                    }
                    else
                        System.out.println("You do not have the authorization to perform this task");
                }
                else if (request.equalsIgnoreCase("log")){
                    if(store.isManager(id).equals("true")) {
                        String [] tempInv;
                        System.out.println("The following is the store's customer log");
                        System.out.println("-------------------------------------");
                        String tempCleanUp = store.showCustomerLog();
                        tempInv = tempCleanUp.split("/");
                        for (String item: tempInv) {
                            System.out.println(item);
                        }
                        System.out.println("-------------------------------------");
                    }
                    else
                        System.out.println("You do not have the authorization to perform this task");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static boolean validID(String id){
        String stores = "qc bc on";
        id = id.toLowerCase();

        if(id.length() == 7) {
            if (stores.contains(id.substring(0, 2))) {
                if (id.charAt(2) == 'm') {
                    if (isInt(id.substring(3))) {
                        System.out.println("Gaining manager access...\n");
                        return true;
                    }
                }
                else if (id.charAt(2) == 'u') {
                    if (isInt(id.substring(3))) {
                        System.out.println("Gaining user access...\n");
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public static boolean isInt(String word){
        try {
            Integer.parseInt(word);
        }catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }
}
