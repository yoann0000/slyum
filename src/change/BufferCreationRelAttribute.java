package change;

import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.RelationalAttribute;
import classDiagram.components.RelationalEntity;

public class BufferCreationRelAttribute implements Changeable {
    private RelationalEntity entity;
    private RelationalAttribute attribute;
    private boolean isCreated;
    private int index;

    public BufferCreationRelAttribute(RelationalEntity e, RelationalAttribute a,
                                   Boolean isCreated, int index) {
        entity = e;
        attribute = a;
        this.isCreated = isCreated;
        this.index = index;
    }

    @Override
    public void restore() {

        if (!isCreated) {
            entity.addAttribute(attribute);
            entity.notifyObservers(UpdateMessage.ADD_ATTRIBUTE_NO_EDIT);

            entity.moveAttributePosition(attribute, index
                    - entity.getAttributes().size() + 1);

            entity.notifyObservers();
        } else {
            entity.removeAttribute(attribute);
            entity.notifyObservers();
        }

    }

    @Override
    public Object getAssociedComponent() {
        return attribute;
    }

}
