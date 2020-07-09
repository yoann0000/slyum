package swing.hierarchicalView;

import classDiagram.components.Entity;
import classDiagram.components.RelationalEntity;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

public class NodeRelationalEntity extends NodeEntity {
    public NodeRelationalEntity(Entity entity, DefaultTreeModel treeModel,
                                HierarchicalView.STree tree, ImageIcon icon) {
        super(entity, treeModel, tree, icon);
    }

    @Override
    protected void reloadChildsNodes() {
        RelationalEntity relationalEntity = (RelationalEntity)super.entity;

        setUserObject(relationalEntity.getName());
        removeAllChildren();

        relationalEntity.getAttributes().forEach((a) -> {
            add(new NodeRelationalAttribute(a, treeModel, tree));
        });

        relationalEntity.getAllKeys().forEach((k) -> {
            add(new NodeKey(k, treeModel, tree));
        });

        HierarchicalView.sortAlphabetically(this, treeModel, tree);
        treeModel.reload(this);
    }
}
