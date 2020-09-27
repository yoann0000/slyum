package classDiagram.components;

import change.BufferIndex;
import change.Change;
import classDiagram.IDiagramComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

public class Key extends Type implements IDiagramComponent {
    private final LinkedList<RelationalAttribute> keyComponents = new LinkedList<>();
    private final RelationalEntity table;

    public Key(Key key) {
        super(key.getName(), key.getId());
        this.table = key.getTable();
    }

    public Key(String name, RelationalEntity table) {
        super(name);
        this.table = table;
    }

    public Key(String name, RelationalAttribute attribute, RelationalEntity table){
        super(name);
        keyComponents.add(attribute);
        this.table = table;
    }

    /**
     * Get all of the key's relational attributes.
     * @return A list containing the relational attributes.
     */
    public LinkedList<RelationalAttribute> getKeyComponents() {
        return keyComponents;
    }

    /**
     * Add a relational attribute to the key.
     * @param attribute the attribute to add.
     */
    public void addKeyComponent(RelationalAttribute attribute) {
        keyComponents.add(attribute);
    }

    /**
     * Remove a relational attribute from the key.
     * @param attribute the attribute to remove.
     */
    public void removeKeyComponent(RelationalAttribute attribute) {
        keyComponents.remove(attribute);
    }

    /**
     * Get the table associated with this key.
     * @return the table associated with this key.
     */
    public RelationalEntity getTable() {
        return table;
    }

    /**
     * Get a string that describes the key depending on it's type.
     * @param keyType the type of the key (primary, foreign, alternate).
     * @return the string representation of the key.
     */
    public String keyName(int keyType) {
        switch (keyType){
            case 0:
                return name + " <PK>";
            case 1:
                return name + " <FK " + table.getName() + ">";
            case 2:
                return name + " <AK>";
            default:
                return name;
        }
    }

    @Override
    public void select() {
        setChanged();
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element key = doc.createElement(getXmlTagName());
        key.setAttribute("name", name);

        key.setAttribute("id", String.valueOf(table.getId()));

        for (RelationalAttribute attribute : keyComponents) {
            Element compId = doc.createElement("raID");
            compId.setAttribute("id", String.valueOf(attribute.id));
            key.appendChild(compId);
        }

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
    protected <T> void moveComponentPosition(List<T> list, T o, int offset) {
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
