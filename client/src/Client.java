import networkEntities.EntityAddressBook;
import org.omg.CORBA.IFrontend;
import org.omg.CORBA.IFrontendHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client{

    public static String userID = null;

    private static HashMap<String, HashMap<String, String>> commandInterface = new HashMap<>();

    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            IFrontend remote = IFrontendHelper.narrow(ncRef.resolve_str(EntityAddressBook.FRONTEND.getShortHandName()));

            prepareCommandConsoleInterfaces(commandInterface);

            Scanner scanner = new Scanner(System.in);
            System.out.print("\n>>>>>>>>>>> Welcome to your local DSMS <<<<<<<<<<<<\r\n");
            System.out.print("Please Specify what type of user you are. Type 'U' for Customer or 'M' for Manager.\n");

            userID = generateUserType(scanner);
            userID = generateUserStore(scanner) + userID;
            userID = userID + generateUserID(scanner);
            userID = userID.toLowerCase();

            while (true) {
                generateUserAction(scanner, remote);
            }
        } catch (InvalidName invalidName) {
            invalidName.printStackTrace();
        } catch (CannotProceed cannotProceed) {
            cannotProceed.printStackTrace();
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
            invalidName.printStackTrace();
        } catch (NotFound notFound) {
            notFound.printStackTrace();
        }
    }

    private static void prepareCommandConsoleInterfaces(HashMap<String, HashMap<String, String>> commandInterface) {
        HashMap<String, String> managerCommandInterfaces = new HashMap<>();
        HashMap<String, String> userCommandInterfaces = new HashMap<>();

        String addCommandInterface = "\tAdd Item:  User ID  , Item ID, Item name, Quantity, Price\n";
        String removeCommandInterface = "\tRemove Item:  ( User ID  ,  Item ID, Quantity )\n";
        String listCommandInterface = "\tList Items:  ( User ID )\n";


        String purchaseCommandInterface = "\tPurchase Item:  User ID  , Item ID \n";
        String findCommandInterface = "\tFind Item:  ( User ID  ,  Item Name )\n";
        String returnCommandInterface = "\tReturn Item:  ( User ID  ,  Item ID )\n";
        String exchangeItemInterface = "\tExchange Items (Old Item, New Item)\n";

        managerCommandInterfaces.put("add", addCommandInterface);
        managerCommandInterfaces.put("remove", removeCommandInterface);
        managerCommandInterfaces.put("list", listCommandInterface);
        Client.commandInterface.put("M", managerCommandInterfaces);

        userCommandInterfaces.put("purchase", purchaseCommandInterface);
        userCommandInterfaces.put("find", findCommandInterface);
        userCommandInterfaces.put("return", returnCommandInterface);
        userCommandInterfaces.put("exchange", exchangeItemInterface);
        Client.commandInterface.put("U", userCommandInterfaces);
    }

        private static String generateUserType(Scanner scanner) {
        String userType = scanner.nextLine();
        userType = userType.toLowerCase();

        switch (userType) {
            case "u":
            case "m":
                return userType;
            default:
                System.out.println("Error: Invalid user type entry. Please try again entering M/U");
                return generateUserType(scanner);
        }
    }

    private static String generateUserStore(Scanner scanner) {
        System.out.print("Enter the branch that is local to you (BC/ON/QC): ");
        String branchID = scanner.nextLine();
        branchID = branchID.toLowerCase();
        switch (branchID) {
            case "on":
            case "qc":
            case "bc":
                return branchID;
            default:
                System.out.print("Error: Invalid branch ID please try again using (BC/ON/QC):\n\n");
                return generateUserStore(scanner);
        }
    }

    private static int generateUserID(Scanner scanner) {
        System.out.print("Enter a 4 digit number that will be used to identify you as a user:");
        int userNumber = scanner.nextInt();
        if (userNumber >= 1000 && userNumber < 10000)
            return userNumber;
        else
            return generateUserID(scanner);
    }

    private static boolean generateUserAction(Scanner scanner, IFrontend frontend) {
        if (userID.substring(2, 3).equalsIgnoreCase("M")) {
            System.out.print("A Manager can enter the following commands for their own store: \n\n");
            managerTaskList();

            System.out.println("Enter a command: ");

            String command = scanner.nextLine();

            handleManagerRequest(command, scanner, frontend);
        } else if (userID.substring(2, 3).equalsIgnoreCase("U")) {
            System.out.print("A Customer can enter the following commands for their own store: \n\n");
            userTaskList();
            System.out.println("Enter a command: ");

            String command = scanner.nextLine();

            handleUserRequest(command, scanner, frontend);
        }
        return true;
    }

    private static void managerTaskList() {
        for (Map.Entry<String, HashMap<String, String>> entry : commandInterface.entrySet())
            if (entry.getKey().equalsIgnoreCase("M"))
                for (Map.Entry<String, String> command : entry.getValue().entrySet()) {
                    System.out.println(command.getValue());
                }
    }

    private static void userTaskList() {
        for (Map.Entry<String, HashMap<String, String>> entry : commandInterface.entrySet())
            if (entry.getKey().equalsIgnoreCase("U"))
                for (Map.Entry<String, String> command : entry.getValue().entrySet()) {
                    System.out.println(command.getValue());
                }
    }

    private static void handleUserRequest(String command, Scanner scanner, IFrontend frontend) {
        String itemID = null;
        String itemName = null;
        String dateString = null;
        String newItemID = null;
        String oldItemID = null;
        String response = null;
        switch (command.toLowerCase()) {
            case "purchase":
                System.out.println("Enter the required item ID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required date string, format \'dd/mm/yyyy\' hh:mm:ss: ");
                dateString = scanner.nextLine();
                while (!isDateFormatValid(dateString)) {
                    System.out.println("Date string invalid, try again...");
                    System.out.println("Enter the required date string, format \'dd/mm/yyyy\' hh:mm:ss: ");
                    dateString = scanner.nextLine();
                }
                response = frontend.createPurchaseItems(userID,itemID,dateString);
                System.out.println(response);
                break;
            case "find":
                System.out.println("Enter the required item name : ");
                itemName = scanner.nextLine();
                response = frontend.createFindItem(userID,itemName);
                System.out.println(response);
                break;
            case "return":
                System.out.println("Enter the required item ID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required date string, format \'dd/mm/yyyy\' hh:mm:ss: ");
                dateString = scanner.nextLine();
                while (!isDateFormatValid(dateString)) {
                    System.out.println("Date string invalid, try again...");
                    System.out.println("Enter the required date string, format \'dd/mm/yyyy\' hh:mm:ss: ");
                    dateString = scanner.nextLine();
                }
                response = frontend.createReturnItems(userID,itemID,dateString);
                System.out.println(response);
                break;
            case "exchange":
                System.out.println("Enter the old item ID : ");
                oldItemID = scanner.nextLine();
                System.out.println("Enter the new item ID : ");
                newItemID = scanner.nextLine();
                System.out.println("Enter the required date string, format \'dd/mm/yyyy\' hh:mm:ss: ");
                dateString = scanner.nextLine();
                while (!isDateFormatValid(dateString)) {
                    System.out.println("Date string invalid, try again...");
                    System.out.println("Enter the required date string, format \'dd/mm/yyyy\' hh:mm:ss: ");
                    dateString = scanner.nextLine();
                }
                response = frontend.createExchangeItem(userID,newItemID,oldItemID,dateString);
                System.out.println(response);
        }

    }

    private static void handleManagerRequest(String command, Scanner scanner, IFrontend frontend) {
        String itemID = null;
        String itemName = null;
        int quantity = 0;
        double price = 0.00;
        String response = "";
        switch (command.toLowerCase()) {
            case "add":
                System.out.println("Enter the required itemID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required itemName : ");
                itemName = scanner.nextLine();
                System.out.println("Enter the required quantity : ");
                quantity = scanner.nextInt();
                System.out.println("Enter the required price : ");
                price = scanner.nextDouble();
                response = frontend.createAddItem(userID, itemID, itemName, quantity, price);
                System.out.println(response);
                break;
            case "remove":
                System.out.println("Enter the required itemID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required quantity : ");
                quantity = scanner.nextInt();
                response = frontend.createRemoveItem(userID, itemID, quantity);
                System.out.println(response);
                break;
            case "list":
                response = frontend.createListItems(userID);
                System.out.println(response);
                break;
        }

    }

    private static boolean isDateFormatValid(String dateString) { // dd/mm/yyyy
        String strDateRegEx = "([012][0-9]|[3][01])\\/(0[1-9]|1[012])\\/\\d{4}";
        return dateString.matches(strDateRegEx);
    }
}
