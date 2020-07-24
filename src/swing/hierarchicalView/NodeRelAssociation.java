package swing.hierarchicalView;

import classDiagram.components.Entity;
import classDiagram.relationships.Association;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

public class NodeRelAssociation extends NodeAssociation{
    /**
     * Create a new node association with an association.
     *
     * @param association the associated association
     * @param treeModel   the model of the JTree
     * @param icon        the customized icon
     * @param tree
     */
    public NodeRelAssociation(Association association, DefaultTreeModel treeModel, ImageIcon icon, HierarchicalView.STree tree) {
        super(association, treeModel, icon, tree);
    }

    public Entity getSource() {
        return association.getSource();
    }

    public Entity getTarget() {
        return association.getTarget();
    }
}
