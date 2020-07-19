package graphic.factory;

import change.BufferCreation;
import change.BufferDeepCreation;
import change.Change;
import classDiagram.components.RelationalEntity;
import classDiagram.relationships.Association;
import classDiagram.relationships.Multi;
import classDiagram.relationships.RelAssociation;
import classDiagram.relationships.Role;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.EntityView;
import graphic.entity.RelationalEntityView;
import graphic.relations.MultiLineView;
import graphic.relations.MultiView;
import graphic.relations.RelAssociationView;
import swing.SPanelDiagramComponent;

import java.awt.*;

public class RelAssociationFactory extends BinaryFactory {
    /**
     * Create a new factory allowing the creation of a relational association.
     *
     * @param parent
     *          the graphic view
     */
    public RelAssociationFactory(GraphicView parent) {
        super(parent);
        GraphicView.setButtonFactory(SPanelDiagramComponent.getInstance().getBtnAssocRel());
    }

    @Override
    public GraphicComponent create() {
        if (componentMousePressed instanceof EntityView
                && componentMouseReleased instanceof EntityView) {
            final EntityView source = (EntityView) componentMousePressed;
            final EntityView target = (EntityView) componentMouseReleased;

            final RelAssociation relAssoc = new RelAssociation(source.getComponent(), target.getComponent(), Association.NavigateDirection.FIRST_TO_SECOND);

            final RelAssociationView b = new RelAssociationView(parent, source, target, relAssoc,
                    mousePressed, mouseReleased, true);

            parent.addLineView(b);
            classDiagram.addRelAssociation(relAssoc);

            Change.push(new BufferDeepCreation(false, relAssoc));
            Change.push(new BufferDeepCreation(true, relAssoc));

            parent.unselectAll();
            b.setSelected(true);

            return b;
        } else {
            final MultiView multiView;
            final RelationalEntityView relationalEntityView;

            if (componentMousePressed.getClass() == MultiView.class
                    && componentMouseReleased instanceof RelationalEntityView) {
                multiView = (MultiView) componentMousePressed;
                relationalEntityView = (RelationalEntityView) componentMouseReleased;
            } else if (componentMouseReleased.getClass() == MultiView.class
                    && componentMousePressed instanceof RelationalEntityView) {
                multiView = (MultiView) componentMouseReleased;
                relationalEntityView = (RelationalEntityView) componentMousePressed;
            } else {
                repaint();
                return null;
            }

            boolean isRecord = Change.isRecord();
            Change.record();

            final Multi multi = (Multi) multiView.getAssociedComponent();
            final Role role = new Role(multi,
                    (RelationalEntity) relationalEntityView.getAssociedComponent(), "");

            Rectangle bounds = multiView.getBounds();
            final Point multiPos = new Point((int) bounds.getCenterX(),
                    (int) bounds.getCenterY());
            bounds = relationalEntityView.getBounds();
            final Point classPos = new Point((int) bounds.getCenterX(),
                    (int) bounds.getCenterY());

            for (MultiLineView mLineView : multiView.getMultiLinesView())
                if (mLineView.getLastPoint().getAssociedComponentView().equals(relationalEntityView)) {
                    mLineView.getTextBoxRole().stream().forEach(tbr -> tbr.delete());
                    multiView.removeMultiLineView(mLineView);
                }

            final MultiLineView mlv = new MultiLineView(parent, multiView, relationalEntityView,
                    role, multiPos, classPos, false);
            multiView.addMultiLineView(mlv);

            Change.push(new BufferCreation(false, mlv));
            Change.push(new BufferCreation(true, mlv));

            if (!isRecord)
                Change.stopRecord();

            repaint();
            return mlv;
        }
    }
}
