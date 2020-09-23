package swing.hierarchicalView;

import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import classDiagram.components.Key;
import swing.PanelClassDiagram;
import swing.Slyum;
import swing.hierarchicalView.HierarchicalView.STree;
import utility.PersonalizedIcon;

public class NodeKey
        extends DefaultMutableTreeNode
        implements ICustomizedIconNode, Observer, IClassDiagramNode {
    private final Key key;
    private final STree tree;
    private final DefaultTreeModel treeModel;
    private final int keyType;

    /**
     * Create a new node associated with a key.
     *
     * @param key
     *          the key associated
     * @param treeModel
     *          the model of the JTree
     * @param tree
     *          the JTree
     * @param keyType
     *          the type of key (0 -> primarykey, 1 -> foreign key, 2 -> alternate key)
     */
    public NodeKey(Key key, DefaultTreeModel treeModel, STree tree, int keyType) {
        super(key.keyName(keyType));

        if (treeModel == null)
            throw new IllegalArgumentException("treeModel is null");

        if (tree == null) throw new IllegalArgumentException("tree is null");

        this.key = key;
        this.treeModel = treeModel;
        this.tree = tree;
        this.keyType = keyType;

        key.addObserver(this);
    }

    @Override
    public IDiagramComponent getAssociedComponent() {
        return key;
    }

    @Override
    public ImageIcon getCustomizedIcon() {
        return PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "key.png");
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
            setUserObject(key.keyName(keyType));
            treeModel.reload(this);
        }
    }

    @Override
    public void remove() {}
}
