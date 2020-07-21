package classDiagram.components;

import change.BufferCreationRelAttribute;
import change.BufferCreationTrigger;
import change.Change;
import swing.XMLParser;

import java.util.LinkedList;

public class RelationalEntity extends Entity{
    private Key primaryKey = new Key("ID", this);
    private final LinkedList<Key> foreignKeys = new LinkedList<>();
    private final LinkedList<Key> alternateKeys = new LinkedList<>();
    private LinkedList<RelationalAttribute> attributes = new LinkedList<>();
    private LinkedList<Trigger> triggers = new LinkedList<>();
    private RelationalAttribute lastAddedAttribute;
    private Trigger lastAddedTrigger;

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

    public RelationalAttribute getAttributeByName(String name) {
        for (RelationalAttribute a : attributes){
            if (a.getName().equals(name)){
                return a;
            }
        }
        return null;
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

    public LinkedList<Trigger> getTriggers() {
        return triggers;
    }

    /**
     * Add a new trigger.
     *
     * @param trigger
     *          the new trigger.
     */
    public void addTrigger(Trigger trigger) {
        addTrigger(trigger, triggers.size());
    }

    /**
     * Add a new trigger.
     *
     * @param trigger
     *          the new trigger.
     * @param index the position of the new trigger in the list.
     */
    public void addTrigger(Trigger trigger, int index) {
        if (trigger == null)
            throw new IllegalArgumentException("trigger is null");

        triggers.add(index, trigger);
        lastAddedTrigger = trigger;
        int i = triggers.indexOf(trigger);
        Change.push(new BufferCreationTrigger(this, trigger, true, i));
        Change.push(new BufferCreationTrigger(this, trigger, false, i));

        setChanged();
    }

    public boolean removeTrigger(Trigger trigger) {
        if (trigger == null)
            throw new IllegalArgumentException("attribute is null");

        int i = triggers.indexOf(trigger);

        if (triggers.remove(trigger)) {
            Change.push(new BufferCreationTrigger(this, trigger, false, i));
            Change.push(new BufferCreationTrigger(this, trigger, true, i));

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

    public void moveTriggerPosition(Trigger trigger, int offset) {
        moveComponentPosition(triggers, trigger, offset);
    }

    @Override
    protected String getEntityType() {
        return XMLParser.EntityType.TABLE.toString();
    }

    public RelationalAttribute getLastAddedAttribute() {
        return lastAddedAttribute;
    }

    public Trigger getLastAddedTrigger() {
        return lastAddedTrigger;
    }
}
