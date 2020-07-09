package graphic.factory;

import classDiagram.components.RelationalEntity;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.EntityView;
import graphic.entity.RelationalEntityView;
import swing.SPanelDiagramComponent;

public class TableFactory extends EntityFactory {

    /**
     * Create a new factory allowing the creation of a class.
     *
     * @param parent
     *          the graphic view
     */
    public TableFactory(GraphicView parent) {
        super(parent);
        GraphicView.setButtonFactory(SPanelDiagramComponent.getInstance().getBtnClass());
    }

    @Override
    public GraphicComponent create() {
        final RelationalEntity relationalEntity = new RelationalEntity("Table");
        final EntityView c = new RelationalEntityView(parent, relationalEntity);

        initializeBounds(c);
        parent.addEntity(c);
        classDiagram.addTableEntity(relationalEntity);

        return c;
    }

}