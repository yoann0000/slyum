package classDiagram.components;

import java.util.LinkedList;

public class Key {
    private LinkedList<RelationalAttribute> keyComponents = new LinkedList<>();
    private String name;

    public Key(String name, RelationalAttribute attribute) {
        this.name = name;
        keyComponents.add(attribute);
    }

    public Key(LinkedList<RelationalAttribute> keyComponents, String name) {
        this.keyComponents = keyComponents;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<RelationalAttribute> getKeyComponents() {
        return keyComponents;
    }

    public void addKeyComponent(RelationalAttribute attribute) {
        keyComponents.add(attribute);
    }

    public void removeKeyComponent(RelationalAttribute attribute) {
        keyComponents.remove(attribute);
    }
}
