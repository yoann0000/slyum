package classDiagram.components;

import change.BufferCreationAttribute;
import change.BufferCreationRelAttribute;
import change.Change;
import swing.XMLParser;
import java.util.LinkedList;

public class RelationalEntity extends Entity{
    private Key primaryKey;
    private final LinkedList<Key> foreignKeys = new LinkedList<>();
    private final LinkedList<Key> alternateKeys = new LinkedList<>();
    private LinkedList<RelationalAttribute> attributes = new LinkedList<>();
    private RelationalAttribute lastAddedAttribute;

    public RelationalEntity(String name) {
        super(name);
    }

    public RelationalEntity(String name, int id) {
        super(name, id);
    }

    public RelationalEntity(String name, LinkedList<RelationalAttribute> attributes) {
        super(name);
        this.attributes = attributes;
    }

    public Key getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Key primaryKey){
        this.primaryKey = primaryKey;
    }

    public LinkedList<Key> getForeignKeys() {
        return foreignKeys;
    }

    public LinkedList<Key> getAlternateKeys() {
        return alternateKeys;
    }

    public void addForeignKey(Key fk) {
        foreignKeys.add(fk);
    }

    public void removeForeignKey(Key fk) {
        foreignKeys.remove(fk);
    }

    public void addAlternateKey(Key ak) {
        alternateKeys.add(ak);
    }

    public void removeAlternateKey(Key ak) {
        alternateKeys.remove(ak);
    }

    public LinkedList<RelationalAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Add a new attribute.
     *
     * @param attribute
     *          the new attribute.
     */
    public void addAttribute(RelationalAttribute attribute) {
        addAttribute(attribute, attributes.size());
    }

    /**
     * Add a new attribute.
     *
     * @param attribute
     *          the new attribute.
     * @param index the position of the new attribute in the list.
     */
    public void addAttribute(RelationalAttribute attribute, int index) {
        if (attribute == null)
            throw new IllegalArgumentException("attribute is null");

        attributes.add(index, attribute);
        lastAddedAttribute = attribute;
        int i = attributes.indexOf(attribute);
        Change.push(new BufferCreationRelAttribute(this, attribute, true, i));
        Change.push(new BufferCreationRelAttribute(this, attribute, false, i));

        setChanged();
    }

    public boolean removeAttribute(RelationalAttribute attribute) {
        if (attribute == null)
            throw new IllegalArgumentException("attribute is null");

        int i = attributes.indexOf(attribute);

        if (attributes.remove(attribute)) {
            Change.push(new BufferCreationRelAttribute(this, attribute, false, i));
            Change.push(new BufferCreationRelAttribute(this, attribute, true, i));

            setChanged();
            return true;
        } else
            return false;
    }

    public LinkedList<Key> getAllKeys() {
        LinkedList<Key> keyList = new LinkedList<>();
        keyList.add(primaryKey);
        keyList.addAll(alternateKeys);
        keyList.addAll(foreignKeys);
        return(keyList);
    }

    public void moveAttributePosition(RelationalAttribute attribute, int offset) {
        moveComponentPosition(attributes, attribute, offset);
    }

    @Override
    protected String getEntityType() {
        return XMLParser.EntityType.TABLE.toString();
    }

    public RelationalAttribute getLastAddedAttribute() {
        return lastAddedAttribute;
    }
}
