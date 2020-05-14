package classDiagram.components;

import swing.XMLParser;
import java.util.LinkedList;

public class RelationalEntity extends Entity{
    private Key primaryKey;
    private LinkedList<Key> foreignKeys = new LinkedList<>();
    private LinkedList<Key> alternateKeys = new LinkedList<>();
    private LinkedList<RelationalAttribute> attributes = new LinkedList<>();

    public RelationalEntity(String name) {
        super(name);
        RelationalAttribute defaultAttribute = new RelationalAttribute("ID", PrimitiveType.INTEGER_TYPE);
        attributes.add(defaultAttribute);
        primaryKey = new Key("ID", defaultAttribute);
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

    @Override
    protected String getEntityType() {
        return XMLParser.EntityType.TABLE.toString();
    }
}
