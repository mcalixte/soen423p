package replica.enums;

public enum Location {
    QUEBEC("qc"),
    BRITISHCOLUMBIA("bc"),
    ONTARIO("on");

    private Location(String shorthand) {
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
