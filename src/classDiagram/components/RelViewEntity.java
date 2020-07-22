package classDiagram.components;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RelViewEntity extends Entity {
    private String procedure;

    public RelViewEntity(String name) {
        super(name);
        this.procedure = "";
    }

    public RelViewEntity(String name, String procedure) {
        super(name);
        this.procedure = procedure;
    }

    public RelViewEntity(String name, int id, String procedure) {
        super(name, id);
        this.procedure = procedure;
    }

    @Override
    protected String getEntityType() {
        return "View";
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element viewValue = doc.createElement(getXmlTagName());
        viewValue.setTextContent(getProcedure());
        return viewValue;
    }

    @Override
    public String getXmlTagName() {
        return "View";
    }

    @Override
    public String toString() {
        return getName();
    }
}
