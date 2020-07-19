package swing.hierarchicalView;

import classDiagram.components.Entity;
import classDiagram.components.RelViewEntity;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

public class NodeViewEntity extends NodeEntity {

    public NodeViewEntity(Entity entity, DefaultTreeModel treeModel, HierarchicalView.STree tree, ImageIcon icon) {
        super(entity, treeModel, tree, icon);
    }

    @Override
    protected void reloadChildsNodes() {
        RelViewEntity relViewEntity = (RelViewEntity) super.entity;

        setUserObject(relViewEntity.getName());
        removeAllChildren();
        treeModel.reload(this);
    }
}
