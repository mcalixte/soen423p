package client;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import service.interfaces.StoreInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private static HashMap<String, HashMap<String, String>> completeCommandInterface = new HashMap<>();
    private static String userType;
    private static String provinceID;

    private static StoreInterface quebecStore;
    private static StoreInterface britishColumbiaStore;
    private static StoreInterface ontarioStore;

    ////////////////////////////////////
    ///    User Related Fields       ///
    ////////////////////////////////////
    private static String quebecProvincePrefix = "QC";
    private static String ontarioProvincePrefix = "ON";
    private static String britishColumbiaProvincePrefix = "BC";
    private static String userID;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            URL quebecURL = new URL("http://localhost:8082/quebecStore?wsdl");
            QName quebecQName = new QName("http://service/", "StoreImplService");
            Service quebecService = Service.create(quebecURL, quebecQName);
            quebecStore = quebecService.getPort(StoreInterface.class);

            URL ontarioURL = new URL("http://localhost:8081/ontarioStore?wsdl");
            QName ontarioQName = new QName("http://service/", "StoreImplService");
            Service ontarioService = Service.create(ontarioURL, ontarioQName);
            ontarioStore = ontarioService.getPort(StoreInterface.class);

            URL britishColumbiaURL = new URL("http://localhost:8080/britishColumbiaStore?wsdl");
            QName britishColumbiaQName = new QName("http://service/", "StoreImplService");
            Service britishColumbiaService = Service.create(britishColumbiaURL, britishColumbiaQName);
            britishColumbiaStore = britishColumbiaService.getPort(StoreInterface.class);

            Scanner scanner = new Scanner(System.in);

            prepareCommandConsoleInterfaces(completeCommandInterface);

            while (true) {
                System.out.println("\t\t >>>>>>>>>> Welcome to the DSMS <<<<<<<<<<<<< \n\n");
                System.out.print("Please Specify what type of user you are. Type 'C' for Customer or 'M' for Manager.\n");
                System.out.print("User-Type: ");

                handleUserTypeInput(scanner);
                handleProvinceCodeInput(scanner);
                handleUserIDCreation(scanner);

                switch (provinceID.toLowerCase()) {
                    case "qc":
                        handleUserAction(scanner, quebecStore);
                        break;
                    case "on":
                        handleUserAction(scanner, ontarioStore);
                        break;
                    case "bc":
                        handleUserAction(scanner, britishColumbiaStore);
                        break;

                }
            }
        } catch (Exception e) {
            System.out.println("Hello Client exception: " + e);
           // e.printStackTrace();
        }
    }

    private static void handleProvinceCodeInput(Scanner scanner) {
        System.out.print("Enter your province Identifier: ");
        Client.provinceID = scanner.nextLine();
        if (verifyProvinceID(provinceID)) {
            Client.provinceID = provinceID;
        } else {
            System.out.print("Alert: Invalid province ID  \n\n");
            while (!verifyProvinceID(provinceID)) {
                System.out.print("Enter your province Identifier: ");
                Client.provinceID = scanner.nextLine();
            }
        }
    }

    private static boolean verifyProvinceID(String provinceID) {
        return provinceID.equalsIgnoreCase(quebecProvincePrefix) || provinceID.equalsIgnoreCase(ontarioProvincePrefix) || provinceID.equalsIgnoreCase(britishColumbiaProvincePrefix);
    }

    private static void prepareCommandConsoleInterfaces(HashMap<String, HashMap<String, String>> commandInterface) {
        HashMap<String, String> managerCommandInterfaces = new HashMap<>();
        HashMap<String, String> customerCommandInterfaces = new HashMap<>();

        String addCommandInterface = "\t>>>>>>>>>>> Add <<<<<<<<<<<\n"
                + "\trequired parameters:  Manager ID  , Item ID, Item name, Quantity, Price \n\n";

        String removeCommandInterface = "\t>>>>>>>>>>> Remove <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Manager ID  ,  Item ID, Quantity )\n\n";
        String listCommandInterface = "\t>>>>>>>>>>> List <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Manager ID )\n\n";


        String purchaseCommandInterface = "\t>>>>>>>>>>> Purchase <<<<<<<<<<<\n"
                + "\trequired parameters:  Customer ID  , Item ID \n\n";
        String findCommandInterface = "\t>>>>>>>>>>> Find <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Customer ID  ,  Item Name )\n\n";
        String returnCommandInterface = "\t>>>>>>>>>>> Return <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Customer ID  ,  Item ID )\n\n";
        String exchangeCommandInterface = "\t>>>>>>>>>>> Return <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Customer ID  ,  New Item ID,  Old Item ID )\n\n";

        managerCommandInterfaces.put("add", addCommandInterface);
        managerCommandInterfaces.put("remove", removeCommandInterface);
        managerCommandInterfaces.put("list", listCommandInterface);
        completeCommandInterface.put("M", managerCommandInterfaces);

        customerCommandInterfaces.put("purchase", purchaseCommandInterface);
        customerCommandInterfaces.put("find", findCommandInterface);
        customerCommandInterfaces.put("return", returnCommandInterface);
        customerCommandInterfaces.put("exchange", exchangeCommandInterface);
        completeCommandInterface.put("C", customerCommandInterfaces);
    }

    private static void handleUserAction(Scanner scanner, StoreInterface store) {
        if (userType.toLowerCase().equalsIgnoreCase("M")) {
            System.out.println("Enter a command: ");

            String command = scanner.nextLine();
            while (!validateCommand(command, completeCommandInterface)) {
                System.out.println("Enter a command: ");
                command = scanner.nextLine();
            }
            displayCommandInterface(command);
            handleManagerInputParameters(command, scanner, store);
        } else if (userType.toLowerCase().equalsIgnoreCase("C")) {
            System.out.println("Enter a command: ");

            String command = scanner.nextLine();
            while (!validateCommand(command, completeCommandInterface)) {
                System.out.println("Enter a command: ");
                command = scanner.nextLine();
            }
            displayCommandInterface(command);
            handleCustomerInputParameters(command, scanner, store);
        }

    }

    private static void handleCustomerInputParameters(String command, Scanner scanner, StoreInterface store) {
        System.out.println("Alert: Be sure to enter the parameters in order \n");
        String customerID = "";
        String itemID = "";
        String newItemID = "";
        String oldItemID = "";
        String itemName = "";
        String dateString;
        switch (command.toLowerCase()) {
            case "purchase":
                System.out.println("The current Customer ID : "+userID);
                System.out.println("Enter the required item ID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required date string, format \'dd/mm/yyyy HH:mm\' : ");
                dateString = scanner.nextLine();
                while (!isDateFormatValid(dateString)) {
                    System.out.println("Date string invalid, try again...");
                    System.out.println("Enter the required date string, format \'dd/mm/yyyy HH:mm\' : ");
                    dateString = scanner.nextLine();
                }
                System.out.println("\t\t >>>>>>> Purchase Item result <<<<<<< \n"+ purchaseItem(store, userID, itemID, dateString));
                break;
            case "find":
                System.out.println("The current Customer ID : "+userID);
                System.out.println("Enter the required item name : ");
                itemName = scanner.nextLine();
                System.out.println("\t\t >>>>>>> Find Item result <<<<<<< \n"+ findItem(store, userID, itemName));
                break;
            case "return":
                System.out.println("The current Customer ID : "+userID);
                System.out.println("Enter the required item ID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required date string, format \'dd/mm/yyyy HH:mm\' : ");
                dateString = scanner.nextLine();
                while (!isDateFormatValid(dateString)) {
                    System.out.println("Date string invalid, try again...");
                    System.out.println("Enter the required date string, format \'dd/mm/yyyy HH:mm\' : ");
                    dateString = scanner.nextLine();
                }
                System.out.println("\t\t >>>>>>> Return Item result <<<<<<< \n"+ returnItem(store, userID, itemID, dateString));
                break;
            case "exchange":
                System.out.println("The current Customer ID : "+userID);
                System.out.println("Enter the required NEW item ID : ");
                newItemID = scanner.nextLine();
                System.out.println("Enter the required OLD item ID : ");
                oldItemID = scanner.nextLine();
                System.out.println("Enter the required date string, format \'dd/mm/yyyy HH:mm\' : ");
                dateString = scanner.nextLine();
                while (!isDateFormatValid(dateString)) {
                    System.out.println("Date string invalid, try again...");
                    System.out.println("Enter the required date string, format \'dd/mm/yyyy HH:mm\' : ");
                    dateString = scanner.nextLine();
                }
                System.out.println("\t\t >>>>>>> Exchange Item result <<<<<<< \n"+ exchangeItem(store, userID, newItemID, oldItemID, dateString));
                break;
        }

    }

    private static boolean isDateFormatValid(String dateString) { // mm/dd/yyyy HH:mm
        String strDateRegEx = "(0[1-9]|[12][0-9]|[3][01])\\/(0[1-9]|1[012])\\/\\d{4} (0[1-9]|[1][0-9]|2[0123]):([0-5][0-9])";
        return dateString.matches(strDateRegEx);
    }

    private static void handleManagerInputParameters(String command, Scanner scanner, StoreInterface store) {
        System.out.println("Alert: Be sure to enter the parameters in order \n");
        String managerID = null;
        String itemID = null;
        String itemName = null;
        int quantity = 0;
        double price = 0.00;
        switch (command.toLowerCase()) {
            case "add":
                System.out.println("The current Manager ID : "+userID);
                System.out.println("Enter the required itemID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required itemName : ");
                itemName = scanner.nextLine();
                System.out.println("Enter the required quantity : ");
                quantity = scanner.nextInt();
                System.out.println("Enter the required price : ");
                price = scanner.nextDouble();
                System.out.println("\t\t >>>>>>> ADD Item result <<<<<<< \n"+ addItem(store, userID, itemID, itemName, quantity, price));

                break;
            case "remove":
                System.out.println("The current Manager ID : "+userID);
                System.out.println("Enter the required itemID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required quantity : ");
                quantity = scanner.nextInt();
                System.out.println("\t\t >>>>>>> REMOVE Item result <<<<<<< \n"+ removeItem(store, userID, itemID, quantity));
                break;
            case "list":
                System.out.println("The current Manager ID : "+userID);
                System.out.println("\t\t >>>>>>> List item result <<<<<<< \n"+listItemAvailability(store, userID));
                break;
        }

    }

    private static void displayCommandInterface(String command) {
        for (Map.Entry<String, HashMap<String, String>> entry : completeCommandInterface.entrySet()) {
            for (Map.Entry<String, String> commandEntry : entry.getValue().entrySet())
                if (commandEntry.getKey().equalsIgnoreCase(command)) {
                    System.out.println("Here is the interface for your chosen method: \n");
                    System.out.println(commandEntry.getValue());
                }
        }
    }

    private static boolean validateCommand(String command, HashMap<String, HashMap<String, String>> completeInterface) {
        boolean isValidCommand = false;

        for (Map.Entry<String, HashMap<String, String>> entry : completeInterface.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(userType))
                for (Map.Entry<String, String> commandEntry : entry.getValue().entrySet())
                    if (commandEntry.getKey().equalsIgnoreCase(command))
                        isValidCommand = true;
        }

        return isValidCommand;
    }


    private static void handleUserIDCreation(Scanner scanner) {
        System.out.print("Time to create a user ID. The ID structure is : [Province Prefix][M or C][4 digit number] \n\n");
        System.out.print("Enter your user ID: ");
        userID = scanner.nextLine();

        while (!isUserIDValid(userID)) {
            System.out.print("Enter your user ID: ");
            userID = scanner.nextLine();
        }
    }

    private static boolean isUserIDValid(String userID)  {
        String provinceID = userID.substring(0, 2);
        String userType = userID.substring(2, 3);
        String userNumber = userID.substring(3, userID.length() - 1);

        return verifyProvinceID(provinceID) || validateUserType(userType) || userNumber.length() == 4;
    }

    private static void handleUserTypeInput(Scanner scanner) {
        userType = scanner.nextLine();

        boolean validEntry = false;

        if (validateUserType(userType) && userType.equalsIgnoreCase("C")) {
            System.out.print("A Customer can execute the following actions in their own store: \n\n");
            for (Map.Entry<String, HashMap<String, String>> entry : completeCommandInterface.entrySet())
                if (entry.getKey().equalsIgnoreCase("C"))
                    for (Map.Entry<String, String> command : entry.getValue().entrySet()) {
                        System.out.println(command.getValue());
                    }

        } else if (validateUserType(userType) && userType.equalsIgnoreCase("M")) {
            System.out.print("A Manager can execute the following actions in their own store: \n\n");
            for (Map.Entry<String, HashMap<String, String>> entry : completeCommandInterface.entrySet())
                if (entry.getKey().equalsIgnoreCase("M"))
                    for (Map.Entry<String, String> command : entry.getValue().entrySet()) {
                        System.out.println(command.getValue());
                    }

        } else {
            while (!validEntry) {
                System.out.println("\nAlert: Invalid user type entry. Please try again ...");
                System.out.print("User-Type: ");
                userType = scanner.nextLine();
                validEntry = validateUserType(userType);
            }
        }

    }

    private static boolean validateUserType(String userType) {
        return userType.toLowerCase().replace(" ", "").equalsIgnoreCase("C") || userType.toLowerCase().replace(" ", "").equalsIgnoreCase("M");
    }

    ////////////////////////////////////
    ///     CORBA remote Methods     ///
    ////////////////////////////////////

    public static String addItem(StoreInterface store, String managerID, String itemID, String itemName, int quantity, double price) {
        return store.addItem(managerID.toLowerCase(), itemID.toLowerCase(), itemName.toLowerCase(), quantity, price);
    }

    public static String removeItem(StoreInterface store, String managerID, String itemID, int quantity) {
        return store.removeItem(managerID.toLowerCase(), itemID.toLowerCase(), quantity);
    }

    public static String listItemAvailability(StoreInterface store, String managerID) {
        return store.listItemAvailability(managerID.toLowerCase());
    }

    public static String purchaseItem(StoreInterface store, String customerID, String itemID, String dateOfPurchase) {
        return store.purchaseItem(customerID.toLowerCase(), itemID.toLowerCase(), dateOfPurchase);
    }

    public static String findItem(StoreInterface store, String customerID, String itemID) {
        return store.findItem(customerID.toLowerCase(), itemID.toLowerCase());
    }

    public static String returnItem(StoreInterface store, String customerID, String itemID, String dateOfReturn) {
        String returnResponse = "";
        returnResponse = store.returnItem(customerID.toLowerCase(), itemID.toLowerCase(), dateOfReturn);

        String provinceOfItem = itemID.substring(0, 2);
        if(returnResponse.contains("Alert: Item does not belong to this store...")) {
            switch (provinceOfItem.toLowerCase()) {
                case "qc":
                    returnResponse = quebecStore.returnItem(customerID, itemID, dateOfReturn);
                    break;
                case "on":
                    returnResponse = ontarioStore.returnItem(customerID, itemID, dateOfReturn);
                    break;
                case "bc":
                    returnResponse = britishColumbiaStore.returnItem(customerID, itemID, dateOfReturn);
                    break;
            }
        }
        return returnResponse;
    }

    public static String exchangeItem(StoreInterface store, String customerID, String newItemID, String oldItemID, String dateOfReturn) {
        return store.exchange(customerID.toLowerCase(), newItemID.toLowerCase(), oldItemID.toLowerCase(), dateOfReturn);
    }
}
