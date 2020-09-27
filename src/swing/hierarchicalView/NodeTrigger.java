package swing.hierarchicalView;

import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.Method;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import classDiagram.components.Trigger;
import swing.PanelClassDiagram;
import swing.hierarchicalView.HierarchicalView.STree;

@SuppressWarnings("serial")
public class NodeTrigger extends DefaultMutableTreeNode implements Observer, IClassDiagramNode, ICustomizedIconNode {
    private final Trigger trigger;
    private final STree tree;
    private final DefaultTreeModel treeModel;

    /**
     * Create a new node associated with a trigger.
     *
     * @param trigger
     *          the attribute trigger
     * @param treeModel
     *          the model of the JTree
     * @param tree
     *          the JTree
     */
    public NodeTrigger(Trigger trigger, DefaultTreeModel treeModel, STree tree) {
        super(trigger.getName());

        if (treeModel == null)
            throw new IllegalArgumentException("treeModel is null");

        if (tree == null) throw new IllegalArgumentException("tree is null");

        this.trigger = trigger;
        this.treeModel = treeModel;
        this.tree = tree;

        trigger.addObserver(this);
    }

    @Override
    public IDiagramComponent getAssociatedComponent() {
        return trigger;
    }

    @Override
    public ImageIcon getCustomizedIcon() {
        return trigger.getImageIcon();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof UpdateMessage) {
            final TreePath path = new TreePath(getPath());

            switch ((UpdateMessage) o) {
                case SELECT:
                    if (!PanelClassDiagram.getInstance().isDisabledUpdate()) {
                        tree.addSelectionPathNoFire(path);
                    }
                    break;
                case UNSELECT:
                    tree.removeSelectionPathNoFire(path);
                    break;
                default:
                    break;
            }
        } else {
            setUserObject(trigger.getName());
            treeModel.reload(this);
        }
    }

    @Override
    public void removeAllChildren() {}

    @Override
    public void remove() {}
}
