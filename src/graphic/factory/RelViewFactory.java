package graphic.factory;

import classDiagram.components.RelViewEntity;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.EntityView;
import graphic.entity.RelViewEntityView;
import swing.SPanelDiagramComponent;

public class RelViewFactory extends EntityFactory {
    /**
     * Create a new factory allowing the creation of a relational view.
     *
     * @param parent
     *          the graphic view
     */
    public RelViewFactory(GraphicView parent) {
        super(parent);
        GraphicView.setButtonFactory(SPanelDiagramComponent.getInstance().getBtnView());
    }

    @Override
    public GraphicComponent create() {
        final RelViewEntity relViewEntity = new RelViewEntity("View");
        final EntityView c = new RelViewEntityView(parent, relViewEntity);

        initializeBounds(c);
        parent.addEntity(c);
        classDiagram.addRelViewEntity(relViewEntity);

        return c;
    }
}
