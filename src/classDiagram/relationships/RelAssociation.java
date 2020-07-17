package classDiagram.relationships;

import classDiagram.components.Entity;
import classDiagram.components.RelationalEntity;
import swing.XMLParser;

public class RelAssociation extends Binary{
    public RelAssociation(Entity source, Entity target) {
        super(source, target, NavigateDirection.FIRST_TO_SECOND);
    }

    public RelAssociation(Entity source, Entity target, NavigateDirection directed) {
        super(source, target, directed);
    }

    public RelAssociation(Entity source, Entity target, NavigateDirection directed, int id) {
        super(source, target, directed, id);
    }

    @Override
    public String getAssociationType() {
        return XMLParser.Aggregation.REL.toString();
    }

    public void addForeignKeys(){
        ((RelationalEntity)getTarget()).addAlternateKey(((RelationalEntity)getSource()).getPrimaryKey());
    }

    public void removeForeignKeys() {
        ((RelationalEntity)getTarget()).removeAlternateKey(((RelationalEntity)getSource()).getPrimaryKey());
    }
}
