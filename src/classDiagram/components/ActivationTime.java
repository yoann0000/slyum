package classDiagram.components;

/**
 * Trigger activation times
 */
public enum ActivationTime {
    AFTER_CREATION("After Creation"),
    BEFORE_ALTER("Before Alter"),
    AFTER_ALTER("After Alter"),
    BEFORE_DROP("Before Drop"),
    AFTER_DROP("After Drop"),
    BEFORE_INSERT("Before Insert");

    private final String name;

    ActivationTime(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ActivationTime getFromName(String name) throws IllegalArgumentException{
        for (ActivationTime t : ActivationTime.values()) {
            if (name.equals(t.name))
                return t;
        }
        throw new IllegalArgumentException();
    }
}
