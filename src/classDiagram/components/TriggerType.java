package classDiagram.components;

public enum TriggerType {
    FOR_EACH_ROW("For Each Row"),
    FOR_EACH_STATEMENT("For Each Statement");

    private final String name;

    TriggerType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TriggerType getFromName(String name) throws IllegalArgumentException{
        for (TriggerType t : TriggerType.values()) {
            if (name.equals(t.name))
                return t;
        }
        throw new IllegalArgumentException();
    }
}
