package graphic.relations;

import classDiagram.components.Entity;
import classDiagram.components.RelationalEntity;
import classDiagram.relationships.Association;
import classDiagram.relationships.RelAssociation;
import classDiagram.relationships.RelationChanger;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.EntityView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Observable;

public class RelAssociationView extends AssociationView {
    /**
     * Create a new BinaryView between source and target.
     *
     * @param parent           the graphic view
     * @param source           the entity source
     * @param target           the entity target
     * @param ra               the relational association
     * @param posSource        the position for put the first MagneticGrip
     * @param posTarget        the position for put the last MagneticGrip
     * @param checkRecursivity check if the relation points to itself
     */
    public RelAssociationView(GraphicView parent, EntityView source, EntityView target, RelAssociation ra, Point posSource, Point posTarget, boolean checkRecursivity) throws IllegalArgumentException{
        super(parent, source, target, ra, posSource, posTarget, checkRecursivity);
        addFk();
    }

    @Override
    public boolean relationChanged(MagneticGrip gripSource, GraphicComponent target) {
        if (!(target instanceof EntityView)) return false;
        deleteFKs();

        // Update model
        RelationChanger.changeRelation(
                relation,
                gripSource.equals(getFirstPoint()),
                (Entity)target.getAssociedComponent());

        // Update views
        adaptRelationsToComponent(relation);

        addFk();

        return true;
    }

    public void addFk() {
        if (relation.getSource() instanceof RelationalEntity && relation.getTarget() instanceof RelationalEntity){
            RelationalEntity sourceEntity = (RelationalEntity) relation.getSource();
            RelationalEntity targetEntity = (RelationalEntity) relation.getTarget();
            Association.NavigateDirection direction = ((RelAssociation) relation).getDirected();
            if (direction == Association.NavigateDirection.FIRST_TO_SECOND) {
                sourceEntity.addForeignKey(targetEntity.getPrimaryKey());
            } else if (direction == Association.NavigateDirection.SECOND_TO_FIRST) {
                targetEntity.addForeignKey(sourceEntity.getPrimaryKey());
            }
        }
    }

    @Override
    public void restore() {
        super.restore();
        if (this.getClass().equals(RelAssociationView.class))
            parent.getClassDiagram().addRelAssociation((RelAssociation) getAssociedComponent(), false);

        repaint();
    }

    @Override
    public void delete() {
        super.delete();
        deleteFKs();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(Association.NavigateDirection.FIRST_TO_SECOND.toString())){
            association.setDirected(Association.NavigateDirection.FIRST_TO_SECOND);
            deleteFKs();
            addFk();
        } else if (e.getActionCommand().equals(Association.NavigateDirection.SECOND_TO_FIRST.toString())) {
            association.setDirected(Association.NavigateDirection.SECOND_TO_FIRST);
            deleteFKs();
            addFk();
        } else
            super.actionPerformed(e);

        association.notifyObservers();
    }

    @Override
    public void update(Observable observable, Object o) { //TODO check if call is a change of direction
        deleteFKs();
        addFk();
        super.update(observable, o);
    }

    private void deleteFKs() {
        if (relation.getSource() instanceof RelationalEntity && relation.getTarget() instanceof RelationalEntity){
            RelationalEntity sourceEntity = (RelationalEntity) relation.getSource();
            RelationalEntity targetEntity = (RelationalEntity) relation.getTarget();
            sourceEntity.getForeignKeys().remove(targetEntity.getPrimaryKey());
            targetEntity.getForeignKeys().remove(sourceEntity.getPrimaryKey());
        }
    }
}
