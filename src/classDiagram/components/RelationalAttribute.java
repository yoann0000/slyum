package classDiagram.components;

import change.BufferRelationalAttribute;
import change.Change;
import classDiagram.verifyName.TypeName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RelationalAttribute extends Variable {
    private boolean unique = false;
    private boolean notNull = false;
    private String defaultValue;

    public RelationalAttribute(String name, Type type) {
        super(name, type);

        boolean isBlocked = Change.isBlocked();
        Change.setBlocked(true);

        setDefaultValue("");

        Change.setBlocked(isBlocked);
    }

    public RelationalAttribute(String name, Type type, int id) {
        super(name, type, id);

        boolean isBlocked = Change.isBlocked();
        Change.setBlocked(true);

        setDefaultValue("");

        Change.setBlocked(isBlocked);
    }

    public RelationalAttribute(RelationalAttribute relationalAttribute) {
        super(relationalAttribute.getName(), new Type(relationalAttribute.getType().getName()));

        boolean isBlocked = Change.isBlocked();
        Change.setBlocked(true);

        name = relationalAttribute.name;
        type = new Type(relationalAttribute.getType().getName());
        defaultValue = relationalAttribute.defaultValue;
        unique = relationalAttribute.unique;
        notNull = relationalAttribute.notNull;
        Change.setBlocked(isBlocked);
    }

    public void setRelationalAttribute(RelationalAttribute relationalAttribute) {
        boolean isRecord = Change.isRecord();
        Change.record();

        setName(relationalAttribute.getName());
        setType(new Type(relationalAttribute.getType().getName()));
        setDefaultValue(relationalAttribute.getDefaultValue());
        setUnique(relationalAttribute.isUnique());
        setNotNull(relationalAttribute.isNotNull());

        if (!isRecord) Change.stopRecord();

        notifyObservers();
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        Change.push(new BufferRelationalAttribute(this));
        this.unique = unique;
        Change.push(new BufferRelationalAttribute(this));
        setChanged();
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        Change.push(new BufferRelationalAttribute(this));
        this.notNull = notNull;
        Change.push(new BufferRelationalAttribute(this));
        setChanged();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        Change.push(new BufferRelationalAttribute(this));
        this.defaultValue = defaultValue;
        Change.push(new BufferRelationalAttribute(this));
        setChanged();
    }

    public void setText(String text) {
        if (text.length() == 0) return;

        String newName;
        Type type = getType();
        text = text.trim();
        boolean unique = false;
        boolean notNull = false;

        final String[] subString = text.split(" : | ");

        newName = subString[0].trim();

        if (subString.length >= 2) {
            subString[1] = subString[1].trim();

            if (!TypeName.getInstance().verifyName(subString[1])) return;

            type = new Type(subString[1]);
        }

        if (subString.length >= 3) {
            subString[2] = subString[2].trim();

            if(subString[2].equals("UNIQUE")) {
                unique = true;
            }else if (subString[2].equals("NOTNULL")) {
                notNull = true;
            }else {
                return;
            }
        }

        if (subString.length >= 4) {
            subString[2] = subString[2].trim();

            if (subString[2].equals("NOTNULL")) {
                notNull = true;
            }else {
                return;
            }
        }

        boolean isRecord = Change.isRecord();
        Change.record();

        setType(type);
        setName(newName);
        setUnique(unique);
        setNotNull(notNull);

        if (!isRecord) Change.stopRecord();

        notifyObservers();
    }

    @Override
    public String getXmlTagName() {
        return "relationalAttribute";
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element attribute = super.getXmlElement(doc);
        if (defaultValue != null)
            attribute.setAttribute("defaultValue", defaultValue);
        attribute.setAttribute("unique", String.valueOf(unique));
        attribute.setAttribute("notNull", String.valueOf(notNull));
        attribute.setAttribute("id", String.valueOf(getId()));
        return attribute;
    }
}
