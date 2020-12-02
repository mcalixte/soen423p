package replica.enums;

public enum UserType {
    CUSTOMER("c"),
    MANAGER("m");

    private UserType(String shorthand) {
        this.shorthandName = shorthand;
    }

    private String shorthandName;

    public String getShorthandName() {
        return shorthandName;
    }

    public void setShorthandName(String shorthandName) {
        this.shorthandName = shorthandName;
    }
}
