package swing.hierarchicalView;

import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import classDiagram.components.RelationalAttribute;
import swing.PanelClassDiagram;
import swing.Slyum;
import swing.hierarchicalView.HierarchicalView.STree;
import utility.PersonalizedIcon;

public class NodeRelationalAttribute
        extends DefaultMutableTreeNode
        implements ICustomizedIconNode, Observer, IClassDiagramNode {
    private final RelationalAttribute attribute;
    private final STree tree;
    private final DefaultTreeModel treeModel;

    /**
     * Create a new node associated with a relational attribute.
     *
     * @param attribute
     *          the relational attribute associated
     * @param treeModel
     *          the model of the JTree
     * @param tree
     *          the JTree
     */
    public NodeRelationalAttribute(RelationalAttribute attribute, DefaultTreeModel treeModel,
                                   STree tree) {
        super(attribute.getName());

        if (treeModel == null)
            throw new IllegalArgumentException("treeModel is null");

        if (tree == null) throw new IllegalArgumentException("tree is null");

        this.attribute = attribute;
        this.treeModel = treeModel;
        this.tree = tree;

        attribute.addObserver(this);
    }

    @Override
    public IDiagramComponent getAssociatedComponent() {
        return attribute;
    }

    @Override
    public ImageIcon getCustomizedIcon() {
        return PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "attribute.png");
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof UpdateMessage) {
            final TreePath path = new TreePath(getPath());

            switch ((UpdateMessage) o) {
                case SELECT:
                    if (!PanelClassDiagram.getInstance().isDisabledUpdate())
                        tree.addSelectionPathNoFire(path);
                    break;
                case UNSELECT:
                    tree.removeSelectionPathNoFire(path);
                    break;
                default:
                    break;
            }
        } else {
            setUserObject(attribute.getName());
            treeModel.reload(this);
        }
    }

    @Override
    public void remove() {}
}
