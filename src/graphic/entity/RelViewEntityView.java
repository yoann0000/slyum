package graphic.entity;

import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.Attribute;
import classDiagram.components.Method;
import classDiagram.components.RelViewEntity;
import classDiagram.components.SimpleEntity;
import graphic.GraphicView;
import graphic.textbox.TextBox;
import graphic.textbox.TextBoxAttribute;
import graphic.textbox.TextBoxMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import swing.MultiViewManager;
import swing.Slyum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class RelViewEntityView extends EntityView {

    public static List<RelViewEntityView> getAll() {
        LinkedList<RelViewEntityView> simples = new LinkedList<>();

        for (GraphicView gv : MultiViewManager.getAllGraphicViews())
            for (EntityView view : gv.getEntitiesView())
                if (view instanceof RelViewEntityView)
                    simples.add((RelViewEntityView) view);
        return simples;
    }

    // Style de vue
    private boolean displayDefault = true;

    public RelViewEntityView(GraphicView parent, RelViewEntity component) {
        super(parent, component);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        if ("ViewDefault".equals(e.getActionCommand())) {
            parent.setDefaultForSelectedEntities(true);
        } else if ("ViewAttribute".equals(e.getActionCommand())) {
            parent.showAttributsForSelectedEntity(true);
            parent.showMethodsForSelectedEntity(false);
        } else if ("ViewMethods".equals(e.getActionCommand())) {
            parent.showAttributsForSelectedEntity(false);
            parent.showMethodsForSelectedEntity(true);
        } else if ("ViewAll".equals(e.getActionCommand())) {
            parent.showAttributsForSelectedEntity(true);
            parent.showMethodsForSelectedEntity(true);
        } else if ("ViewNothing".equals(e.getActionCommand())) {
            parent.showAttributsForSelectedEntity(false);
            parent.showMethodsForSelectedEntity(false);
        }
        component.notifyObservers();
    }

    @Override
    public RelViewEntityView clone() throws CloneNotSupportedException {

        RelViewEntityView view = (RelViewEntityView) super.clone();
        view.displayDefault = displayDefault;
        return view;
    }

    @Override
    public int computeHeight(int classNameHeight, int stereotypeHeight, int elementsHeight) {
        return super.computeHeight(classNameHeight, stereotypeHeight, elementsHeight);
    }

    @Override
    public List<TextBox> getAllTextBox() {
        return super.getAllTextBox();
    }

    @Override
    public RelViewEntity getComponent() {
        return (RelViewEntity) super.getComponent();
    }



    @Override
    public void setPictureMode(boolean enable) {
        super.setPictureMode(enable);
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element entityView = super.getXmlElement(doc);
        entityView.setAttribute("displayDefault", String.valueOf(displayDefault));
        return entityView;
    }

    @Override
    public boolean removeTextBox(TextBox tb) {
        return false;
    }

    @Override
    public void update(Observable observable, Object object) {
        if (object != null && object.getClass() == UpdateMessage.class)
            super.update(observable, object);
        else
            regenerateEntity();
    }

    @Override
    protected void initializeMenuItemsAddElements(JPopupMenu popupmenu) {

    }

    @Override
    protected void initializeMenuItemsPropertiesElements(JPopupMenu popupMenu) {

    }

    @Override
    protected void initializeMenuViews(JPopupMenu popupMenu) {

    }

    @Override
    protected void innerRegenerate() {

    }

    @Override
    protected int paintTextBoxes(Graphics2D g2, Rectangle bounds, int textboxHeight, int offset) {
        return offset;
    }
}
