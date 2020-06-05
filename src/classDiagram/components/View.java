package classDiagram.components;

import change.BufferView;
import change.Change;
import classDiagram.ClassDiagram;
import classDiagram.IDiagramComponent;
import java.util.Observable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class View extends Observable
        implements IDiagramComponent, Cloneable {

    protected final int id = ClassDiagram.getNextId();
    private String name;
    private String procedure;

    public View(String name, String procedure) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("View name cannot be null or empty.");

        if (!name.matches(Attribute.REGEX_SEMANTIC_ATTRIBUTE))
            throw new IllegalArgumentException("Semantic name doesn't matche.");

        if (procedure == null)
            throw new IllegalArgumentException("Procedure cannot be null");

        this.name = name;
        this.procedure = procedure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!name.matches(Attribute.REGEX_SEMANTIC_ATTRIBUTE))
            throw new IllegalArgumentException("Semantic name doesn't matche.");
        if (this.name.equals(name)) return;
        Change.push(new BufferView(this));
        this.name = name;
        Change.push(new BufferView(this));
        setChanged();
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        if (this.procedure.equals(procedure)) return;
        Change.push(new BufferView(this));
        this.procedure = procedure;
        Change.push(new BufferView(this));
        setChanged();
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element enumValue = doc.createElement(getXmlTagName());
        enumValue.setTextContent(getProcedure());
        return enumValue;
    }

    @Override
    public String getXmlTagName() {
        return "View";
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
    public String toString() {
        return getName();
    }

    @Override
    public View clone() throws CloneNotSupportedException {
        return new View(getName(), getProcedure());
    }

}
