package classDiagram.components;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import swing.XMLParser;

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

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    @Override
    protected String getEntityType() {
        return XMLParser.EntityType.VIEW.toString();
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element entity = doc.createElement(getXmlTagName());
        entity.setAttribute("id", String.valueOf(getId()));
        entity.setAttribute("name", toString());
        entity.setAttribute("entityType", getEntityType());
        entity.setAttribute("procedure", getProcedure());
        return entity;
    }

    @Override
    public String toString() {
        return getName();
    }
}
