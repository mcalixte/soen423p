package services.item;

public class Items {

    private String itemID;
    private String itemName;
    private int quantity;
    private double price;

    public Items(String itemID, String itemName, int quantity, double price) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return  "itemID= " + itemID  +
                ", itemName= " + itemName  +
                ", quantity= " + quantity +
                ", price= " + price + ";";
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
