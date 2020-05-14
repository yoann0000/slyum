package change;

import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.RelationalAttribute;

public class BufferRelationalAttribute extends BufferVariable {
    private RelationalAttribute attribute, copy;

    public BufferRelationalAttribute(RelationalAttribute attribute) {
        super(attribute);
        this.attribute = attribute;
        copy = new RelationalAttribute(attribute);
    }

    @Override
    public void restore() {
        super.restore();
        attribute.setRelationalAttribute(copy);

        attribute.select();
        attribute.notifyObservers(UpdateMessage.SELECT);
    }
}
