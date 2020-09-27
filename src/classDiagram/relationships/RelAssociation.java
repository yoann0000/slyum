package classDiagram.relationships;

import classDiagram.components.Entity;
import classDiagram.components.RelationalEntity;
import swing.XMLParser;

public class RelAssociation extends Binary{
    public RelAssociation(Entity source, Entity target) {
        super(source, target, NavigateDirection.FIRST_TO_SECOND);
        addForeignKey();
    }

    public RelAssociation(Entity source, Entity target, NavigateDirection directed) {
        super(source, target, directed);
        addForeignKey();
    }

    public RelAssociation(Entity source, Entity target, NavigateDirection directed, int id) {
        super(source, target, directed, id);
        addForeignKey();
    }

    @Override
    public String getAssociationType() {
        return XMLParser.Aggregation.REL.toString();
    }

    public void addForeignKey(){
        if (getSource() instanceof RelationalEntity && getTarget() instanceof RelationalEntity)
            ((RelationalEntity)getSource()).addForeignKey(((RelationalEntity)getTarget()).getPrimaryKey());
    }

    public void removeForeignKey() {
        if (getSource() instanceof RelationalEntity && getTarget() instanceof RelationalEntity)
            ((RelationalEntity)getSource()).removeForeignKey(((RelationalEntity)getTarget()).getPrimaryKey());
    }

    /**
     * Reset the foreign keys.
     * Used when the relation's direction is changed.
     */
    public void resetKeys() {
        if (getSource() instanceof RelationalEntity && getTarget() instanceof RelationalEntity){
            ((RelationalEntity)getSource()).removeForeignKey(((RelationalEntity)getTarget()).getPrimaryKey());
            ((RelationalEntity)getTarget()).removeForeignKey(((RelationalEntity)getSource()).getPrimaryKey());
            if (directed == NavigateDirection.FIRST_TO_SECOND) {
                ((RelationalEntity)getSource()).addForeignKey(((RelationalEntity)getTarget()).getPrimaryKey());
            } else if (directed == NavigateDirection.SECOND_TO_FIRST){
                ((RelationalEntity)getTarget()).addForeignKey(((RelationalEntity)getSource()).getPrimaryKey());
            }
        }
    }
}
