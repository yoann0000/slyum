package classDiagram.components;

public class Trigger extends Type {
    private TriggerType triggerType;
    private ActivationTime activationTime;
    private String procedure;

    public Trigger(String name) {
        super(name);
        triggerType = TriggerType.FOR_EACH_ROW;
        activationTime = ActivationTime.AFTER_CREATION;
        procedure = "";
    }

    public Trigger(String name, int id) {
        super(name, id);
        triggerType = TriggerType.FOR_EACH_ROW;
        activationTime = ActivationTime.AFTER_CREATION;
        procedure = "";
    }

    public Trigger(String name, int id, TriggerType triggerType, ActivationTime activationTime, String procedure) {
        super(name, id);
        this.triggerType = triggerType;
        this.activationTime = activationTime;
        this.procedure = procedure;
    }

    public Trigger(Trigger component) {
        super(component.name);
        this.triggerType = component.triggerType;
        this.activationTime = component.activationTime;
        this.procedure = component.procedure;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public ActivationTime getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(ActivationTime activationTime) {
        this.activationTime = activationTime;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }
}

