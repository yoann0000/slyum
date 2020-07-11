package classDiagram.components;

import classDiagram.ClassDiagram;
import classDiagram.IDiagramComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedList;

public class Key extends Type implements IDiagramComponent {
    protected final int id = ClassDiagram.getNextId();
    private final LinkedList<RelationalAttribute> keyComponents = new LinkedList<>();

    public Key(String name) {
        super(name);
    }

    public Key(String name, RelationalAttribute attribute){
        super(name);
        keyComponents.add(attribute);
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
