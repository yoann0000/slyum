package graphic.relations;

import classDiagram.IDiagramComponent;
import classDiagram.components.Entity;
import classDiagram.components.RelationalEntity;
import classDiagram.relationships.Association;
import classDiagram.relationships.RelAssociation;
import classDiagram.relationships.RelationChanger;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.EntityView;

import javax.swing.*;
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
    public RelAssociationView(GraphicView parent, EntityView source, EntityView target, RelAssociation ra,
                              Point posSource, Point posTarget, boolean checkRecursivity) throws IllegalArgumentException{
        super(parent, source, target, ra, posSource, posTarget, checkRecursivity);
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
                sourceEntity.addForeignKey(sourceEntity.getPrimaryKey());
            } else if (direction == Association.NavigateDirection.SECOND_TO_FIRST) {
                targetEntity.addForeignKey(targetEntity.getPrimaryKey());
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
        deleteFKs();
        super.delete();
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
        } else if(e.getActionCommand().equals("Delete")){
            GraphicView.deleteComponent(this);
            deleteFKs();
        } else
            super.actionPerformed(e);

        association.notifyObservers();
    }

    @Override
    public void changeOrientation() {
        super.changeOrientation();
        deleteFKs();
        addFk();
    }

    private void deleteFKs() {
        if (relation.getSource() instanceof RelationalEntity && relation.getTarget() instanceof RelationalEntity){
            RelationalEntity sourceEntity = (RelationalEntity) relation.getSource();
            RelationalEntity targetEntity = (RelationalEntity) relation.getTarget();
            sourceEntity.removeForeignKey(targetEntity.getPrimaryKey());
            targetEntity.removeForeignKey(sourceEntity.getPrimaryKey());
        }
    }

    @Override
    protected void setMenuItemText() {
        String sourceName = association.getSource().getName(), targetName = association
                .getTarget().getName();
        navFirstToSecond.setText(String.format("%s -> %s", sourceName, targetName));
        navSecondToFirst.setText(String.format("%s -> %s", targetName, sourceName));
    }

    @Override
    public void popupmenuInit() {
        JMenu menuNavigation = new JMenu("Navigability");
        popupMenu.addSeparator();
        popupMenu.add(menuNavigation);
        btnGrpNavigation = new ButtonGroup();
        menuNavigation.add(navFirstToSecond = makeRadioButtonMenuItem("",
                Association.NavigateDirection.FIRST_TO_SECOND.toString(), btnGrpNavigation));
        menuNavigation.add(navSecondToFirst = makeRadioButtonMenuItem("",
                Association.NavigateDirection.SECOND_TO_FIRST.toString(), btnGrpNavigation));
    }
}
