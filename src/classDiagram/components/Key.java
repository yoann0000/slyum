package classDiagram.components;

import change.BufferIndex;
import change.Change;
import classDiagram.ClassDiagram;
import classDiagram.IDiagramComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

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

    public void moveAttributePosition(RelationalAttribute attribute, int offset) {
        moveComponentPosition(keyComponents, attribute, offset);
    }

    /**
     * Move the object's position in the given array by the given offset. Offset
     * is added to the current index to compute the new index. The offset can be
     * positive or negative.
     *
     * @param list
     *          the list containing the object to move
     * @param o
     *          the object to move
     * @param offset
     *          the offset for compute the new index
     */
    protected <T extends Object> void moveComponentPosition(List<T> list, T o, int offset) {
        int index = list.indexOf(o);

        if (index != -1) {
            Change.push(new BufferIndex<>(this, list, o));

            list.remove(o);
            list.add(index + offset, o);

            Change.push(new BufferIndex<>(this, list, o));

            setChanged();
        }
    }
}
