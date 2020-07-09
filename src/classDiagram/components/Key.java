package classDiagram.components;

import classDiagram.ClassDiagram;
import classDiagram.IDiagramComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.Observable;

public class Key extends Observable implements IDiagramComponent {
    protected final int id = ClassDiagram.getNextId();
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

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void select() {
        setChanged();
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element key = doc.createElement(getXmlTagName());
        key.setAttribute("name", name);

        for (RelationalAttribute attribute : keyComponents)
            key.appendChild(attribute.getXmlElement(doc));

        return key;
    }

    @Override
    public String getXmlTagName() {
        return "key";
    }
}
