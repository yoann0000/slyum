package swing.hierarchicalView;

import classDiagram.components.Entity;
import classDiagram.components.Key;
import classDiagram.components.RelationalEntity;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

public class NodeRelationalEntity extends NodeEntity {
    private final int PRIMARY_KEY = 0;
    private final int FOREIGN_KEY = 1;
    private final int ALTERNATE_KEY = 2;

    public NodeRelationalEntity(Entity entity, DefaultTreeModel treeModel, HierarchicalView.STree tree, ImageIcon icon) {
        super(entity, treeModel, tree, icon);
    }

    @Override
    protected void reloadChildsNodes() {
        RelationalEntity relationalEntity = (RelationalEntity)super.entity;

        setUserObject(relationalEntity.getName());
        removeAllChildren();

        relationalEntity.getAttributes().forEach(a -> add(new NodeRelationalAttribute(a, treeModel, tree)));

        relationalEntity.getTriggers().forEach((trigger -> add(new NodeTrigger(trigger, treeModel, tree))));

        Key pk = relationalEntity.getPrimaryKey();
        if(pk != null) {
            add(new NodeKey(pk, treeModel, tree, PRIMARY_KEY));
        }

        relationalEntity.getForeignKeys().forEach(fk -> add(new NodeKey(fk, treeModel, tree, FOREIGN_KEY)));

        relationalEntity.getAlternateKeys().forEach(ak -> add(new NodeKey(ak, treeModel, tree, ALTERNATE_KEY)));

        HierarchicalView.sortAlphabetically(this, treeModel, tree);
        treeModel.reload(this);
    }
}
