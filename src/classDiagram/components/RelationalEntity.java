package classDiagram.components;

import change.BufferCreationRelAttribute;
import change.BufferCreationTrigger;
import change.Change;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
    private Key lastAddedKey;

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
        lastAddedKey = primaryKey;
        this.primaryKey = primaryKey;
    }

    public LinkedList<Key> getForeignKeys() {
        return foreignKeys;
    }

    public LinkedList<Key> getAlternateKeys() {
        return alternateKeys;
    }

    public void addForeignKey(Key fk) {
        lastAddedKey = fk;
        foreignKeys.add(fk);
        notifyObservers(UpdateMessage.ADD_FK);
    }

    public void removeForeignKey(Key fk) {
        if (foreignKeys.contains(fk)) {
            foreignKeys.remove(fk);
            notifyObservers(UpdateMessage.RM_FK); //FIXME
        }
    }

    public void addAlternateKey(Key ak) {
        lastAddedKey = ak;
        alternateKeys.add(ak);
    }

    public void removeAlternateKey(Key ak) {
        alternateKeys.remove(ak);
    }

    public LinkedList<RelationalAttribute> getAttributes() {
        return attributes;
    }

    public RelationalAttribute getAttributeById(int id) {
        for (RelationalAttribute ra : attributes) {
            if (ra.getId() == id)
                return ra;
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

    public void moveAkPosition(Key ak, int offset) {
        moveComponentPosition(alternateKeys, ak, offset);
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

    public Key getLastAddedKey() {
        return lastAddedKey;
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element entity = doc.createElement(getXmlTagName());

        entity.setAttribute("id", String.valueOf(getId()));
        entity.setAttribute("name", toString());
        entity.setAttribute("entityType", getEntityType());

        for (RelationalAttribute attribute : attributes)
            entity.appendChild(attribute.getXmlElement(doc));

        Element pk = primaryKey.getXmlElement(doc);
        pk.setAttribute("primary", String.valueOf(true));
        entity.appendChild(pk);

        for (Key key : alternateKeys){
            Element ak = key.getXmlElement(doc);
            ak.setAttribute("primary", String.valueOf(false));
            entity.appendChild(ak);
        }

        for (Trigger trigger : triggers)
            entity.appendChild(trigger.getXmlElement(doc));

        return entity;
    }
}
