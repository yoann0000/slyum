package change;

import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.RelationalEntity;
import classDiagram.components.Trigger;

public class BufferCreationTrigger implements Changeable {

    private RelationalEntity entity;
    private Trigger trigger;
    private boolean isCreated;
    private int index;

    public BufferCreationTrigger(RelationalEntity e, Trigger t, Boolean isCreated,
                                int index) {
        entity = e;
        trigger = t;
        this.isCreated = isCreated;
        this.index = index;
    }

    @Override
    public void restore() {
        if (!isCreated) {
            entity.addTrigger(trigger);
            entity.notifyObservers(UpdateMessage.ADD_METHOD_NO_EDIT);

            entity.moveTriggerPosition(trigger, index - entity.getTriggers().size() + 1);
            entity.notifyObservers();
        } else {
            entity.removeTrigger(trigger);
            entity.notifyObservers();
        }
    }

    @Override
    public Object getAssociedComponent() {
        return trigger;
    }

}
