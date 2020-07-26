package classDiagram.relationships;

import classDiagram.components.Entity;
import classDiagram.components.RelationalEntity;
import swing.XMLParser;

public class RelAssociation extends Binary{
    public RelAssociation(Entity source, Entity target) {
        super(source, target, NavigateDirection.FIRST_TO_SECOND);
        addForeignKeys();
    }

    public RelAssociation(Entity source, Entity target, NavigateDirection directed) {
        super(source, target, directed);
        addForeignKeys();
    }

    public RelAssociation(Entity source, Entity target, NavigateDirection directed, int id) {
        super(source, target, directed, id);
        addForeignKeys();
    }

    @Override
    public String getAssociationType() {
        return XMLParser.Aggregation.REL.toString();
    }

    public void addForeignKeys(){
        if(getSource() instanceof RelationalEntity && getTarget() instanceof RelationalEntity)
            ((RelationalEntity)getSource()).addForeignKey(((RelationalEntity)getTarget()).getPrimaryKey());
    }

    public void removeForeignKeys() {
        if(getSource() instanceof RelationalEntity && getTarget() instanceof RelationalEntity)
            ((RelationalEntity)getSource()).removeForeignKey(((RelationalEntity)getTarget()).getPrimaryKey());
    }
}
