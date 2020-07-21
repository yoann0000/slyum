package classDiagram.components;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import swing.Slyum;
import utility.PersonalizedIcon;

import javax.swing.*;

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


    public ImageIcon getImageIcon() {
        return PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "method.png");
    }

    @Override
    public Element getXmlElement(Document doc) {

        Element variable = doc.createElement(getXmlTagName());
        variable.setAttribute("name", name);
        variable.setAttribute("procedure", getProcedure());
        variable.setAttribute("activationTime", activationTime.getName());
        variable.setAttribute("triggerType", triggerType.getName());

        return variable;
    }

    @Override
    public String getXmlTagName() {
        return "trigger";
    }
}

