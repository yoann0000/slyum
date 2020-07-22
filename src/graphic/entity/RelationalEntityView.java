package graphic.entity;

import classDiagram.ClassDiagram.ViewEntity;
import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.*;
import classDiagram.components.Method.ParametersViewStyle;
import graphic.GraphicView;
import graphic.textbox.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import swing.MultiViewManager;
import swing.PanelClassDiagram;
import swing.Slyum;
import utility.PersonalizedIcon;
import utility.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class RelationalEntityView extends EntityView {

    public static List<RelationalEntityView> getAll() {
        LinkedList<RelationalEntityView> rels = new LinkedList<>();

        for (GraphicView gv : MultiViewManager.getAllGraphicViews())
            for (EntityView view : gv.getEntitiesView())
                if (view instanceof RelationalEntityView)
                    rels.add((RelationalEntityView) view);
        return rels;
    }

    public static List<RelationalEntityView> getSelectedRelationalEntityView(GraphicView parent) {
        List<RelationalEntityView> rels = new LinkedList<>();
        for (EntityView view : parent.getSelectedEntities())
            if (view instanceof RelationalEntityView)
                rels.add((RelationalEntityView) view);
        return rels;
    }

    // Attributs et méthodes
    protected LinkedList<TextBoxRelAttribute> attributesView = new LinkedList<>();
    protected boolean displayTriggers = true;

    protected LinkedList<TextBoxTrigger> triggersView = new LinkedList<>();

    private boolean displayAttributes = true;
    // Style de vue
    private boolean displayDefault = true;

    private ButtonGroup groupView, groupViewTriggers;
    private JMenuItem menuItemAbstract;
    private JMenuItem menuItemTriggersAll;
    private JMenuItem menuItemTriggersDefault;
    private JMenuItem menuItemTriggersName;
    private JMenuItem menuItemTriggersNothing;
    private JMenuItem menuItemTriggersType;
    private JMenuItem menuItemStatic;
    private JMenuItem menuItemViewAll;
    private JMenuItem menuItemViewAttributes;
    private JMenuItem menuItemViewDefault;
    private JMenuItem menuItemViewTriggers;
    private JMenuItem menuItemViewNothing;

    public RelationalEntityView(GraphicView parent, RelationalEntity component) {
       super(parent, component);
        initViewType();
        addDefaultKey();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        if ("AddAttribute".equals(e.getActionCommand())) {
            addAttribute();
        } else if ("ViewDefault".equals(e.getActionCommand())) {
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
        else if (Slyum.ACTION_TEXTBOX_UP.equals(e.getActionCommand())
                || Slyum.ACTION_TEXTBOX_DOWN.equals(e.getActionCommand())) {
            int offset = 1;
            if (Slyum.ACTION_TEXTBOX_UP.equals(e.getActionCommand())) offset = -1;
            if (pressedTextBox.getClass() == TextBoxAttribute.class) {
                final RelationalAttribute attribute = (RelationalAttribute) pressedTextBox
                        .getAssociedComponent();
                ((RelationalEntity) component).moveAttributePosition(attribute, offset);
            }else if (Slyum.ACTION_DUPLICATE.equals(e.getActionCommand())) {
                if (pressedTextBox != null) {
                    IDiagramComponent component = pressedTextBox.getAssociedComponent();
                    RelationalEntity entity = (RelationalEntity) getAssociedComponent();
                    if (component instanceof RelationalAttribute) {
                        RelationalAttribute attribute = new RelationalAttribute((RelationalAttribute) component);
                        LinkedList<RelationalAttribute> attributes = entity.getAttributes();
                        entity.addAttribute(attribute);
                        entity.notifyObservers(UpdateMessage.ADD_ATTRIBUTE_NO_EDIT);
                        entity.moveAttributePosition(attribute,
                                attributes.indexOf(component) - attributes.size() + 1);
                        entity.notifyObservers();
                    } else {
                        Trigger trigger = new Trigger((Trigger) component);
                        LinkedList<Trigger> triggers = entity.getTriggers();
                        entity.addTrigger(trigger);
                        entity.notifyObservers(UpdateMessage.ADD_TRIGGER_NO_EDIT);
                        entity.moveTriggerPosition(trigger, triggers.indexOf(component) - triggers.size() + 1);
                        entity.notifyObservers();
                    }
                }
            }
            component.notifyObservers();
        }
    }

    /**
     * Create a new attribute with default type and name.
     */
    public void addAttribute() {
        final RelationalAttribute attribute = new RelationalAttribute("attribute",
                PrimitiveType.VOID_TYPE);
        prepareNewAttribute(attribute);

        ((RelationalEntity) component).addAttribute(attribute);
        component.notifyObservers(UpdateMessage.ADD_ATTRIBUTE);
    }

    private void addDefaultAttribute(){
        final RelationalAttribute attribute = new RelationalAttribute("id", PrimitiveType.INTEGER_TYPE);
        prepareNewAttribute(attribute);

        ((RelationalEntity) component).addAttribute(attribute);
        component.notifyObservers(UpdateMessage.ADD_ATTRIBUTE);
    }

    private void addDefaultKey(){
        RelationalEntity re = (RelationalEntity) component;
        if(re.getPrimaryKey() != null)
            return;
        final Key pk = new Key("ID", re);
        re.setPrimaryKey(pk);
    }

    /**
     * Create a new attribute view with the given attribute. If editing is a true,
     * the new attribute view will be in editing mode while it created.
     *
     * @param attribute
     *          the attribute UML
     * @param editing
     *          true if creating a new attribute view in editing mode; false
     *          otherwise
     */
    public void addAttribute(RelationalAttribute attribute, boolean editing) {
        final TextBoxRelAttribute newTextBox = new TextBoxRelAttribute(parent, attribute);
        attributesView.add(newTextBox);

        updateHeight();

        if (editing) newTextBox.editing();
    }

    /**
     * Create a new trigger view with the given trigger. If editing is a true,
     * the new attribute view will be in editing mode while it created.
     *
     * @param trigger
     *          the trigger
     * @param editing
     *          true if creating a new attribute view in editing mode; false
     *          otherwise
     */
    public void addTrigger(Trigger trigger, boolean editing) {
        final TextBoxTrigger newTextBox = new TextBoxTrigger(parent, trigger);
        triggersView.add(newTextBox);

        updateHeight();

        if (editing) newTextBox.editing();
    }



    @Override
    public RelationalEntityView clone() throws CloneNotSupportedException {

        RelationalEntityView view = (RelationalEntityView) super.clone();
        view.displayDefault = displayDefault;
        view.displayAttributes = displayAttributes;
        view.displayTriggers = displayTriggers;
        return view;
    }

    @Override
    public int computeHeight(int classNameHeight, int stereotypeHeight, int elementsHeight) {
        int height = super.computeHeight(classNameHeight, stereotypeHeight,
                elementsHeight);

        if (displayTriggers) height += 10 + elementsHeight * triggersView.size();
        if (displayAttributes)
            height += 10 + elementsHeight * attributesView.size();
        return height;
    }

    @Override
    public List<TextBox> getAllTextBox() {
        List<TextBox> tb = super.getAllTextBox();
        tb.addAll(triggersView);
        tb.addAll(attributesView);
        return tb;
    }

    @Override
    public RelationalEntity getComponent() {
        return (RelationalEntity) super.getComponent();
    }

    /**
     * Set the display state for attributes.
     *
     * @param display
     *          the new display state for attributes.
     */
    public void setDisplayAttributes(boolean display) {
        displayAttributes = display;
        displayDefault = false;
        updateHeight();
    }

    public void setDisplayDefault(boolean display) {
        displayDefault = display;
        initViewType();
    }

    @Override
    public void setPictureMode(boolean enable) {
        super.setPictureMode(enable);
        for (TextBox t : triggersView)
            t.setPictureMode(enable);
        for (TextBox t : attributesView)
            t.setPictureMode(enable);
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element entityView = super.getXmlElement(doc);
        entityView.setAttribute("displayDefault", String.valueOf(displayDefault));
        entityView.setAttribute("displayAttributes",
                String.valueOf(displayAttributes));
        entityView.setAttribute("displayMethods", String.valueOf(displayTriggers));
        return entityView;
    }

    public final void initViewType() {
        if (displayDefault) {
            ViewEntity view = PanelClassDiagram.getInstance().getClassDiagram().getDefaultViewEntities();
            switch (view) {
                case NOTHING:
                    displayAttributes = false;
                    displayTriggers = false;
                    break;
                case ONLY_ATTRIBUTES:
                    displayAttributes = true;
                    displayTriggers = false;
                    break;
                case ONLY_METHODS:
                    displayAttributes = false;
                    displayTriggers = true;
                    break;
                default:
                    displayAttributes = true;
                    displayTriggers = true;
                    break;
            }
            updateHeight();
        }
    }

    @Override
    public void maybeShowPopup(MouseEvent e, JPopupMenu popupMenu) {
        if (e.isPopupTrigger()) {
            updateMenuItemView();
            updateMenuItemMethodsView();

            menuItemAbstract.setEnabled(false);

            // If context menu is requested on a TextBox, customize popup menu.
            if (pressedTextBox != null) {
                boolean isConstructor = pressedTextBox.getAssociedComponent().getClass()
                        .equals(ConstructorMethod.class);
                menuItemStatic.setEnabled(!isConstructor);

                menuItemMoveUp.setEnabled(attributesView.indexOf(pressedTextBox) != 0
                        && triggersView.indexOf(pressedTextBox) != 0);
                menuItemMoveDown
                        .setEnabled((attributesView.size() == 0 || attributesView
                                .indexOf(pressedTextBox) != attributesView.size() - 1)
                                && (triggersView.size() == 0 || triggersView
                                .indexOf(pressedTextBox) != triggersView.size() - 1));
                if (pressedTextBox instanceof TextBoxMethod)
                    menuItemAbstract.setEnabled(!isConstructor);

            } else {
                menuItemMoveUp.setEnabled(false);
                menuItemMoveDown.setEnabled(false);
                menuItemStatic.setEnabled(false);
                menuItemAbstract.setEnabled(true);
            }
        }
        super.maybeShowPopup(e, popupMenu);
    }

    /**
     * Remove the attribute associated with TextBoxAttribute from model (UML).
     *
     * @param tbAttribute
     *          the attribute to remove.
     * @return true if the attribute has been removed; false otherwise
     */
    public boolean removeAttribute(TextBoxAttribute tbAttribute) {
        if (((RelationalEntity) component).removeAttribute((RelationalAttribute) tbAttribute
                .getAssociedComponent())) {
            component.notifyObservers();
            updateHeight();
            return true;
        }

        return false;
    }

    /**
     * Remove the trigger associated with TextBoxTrigger from model (UML)
     *
     * @param tbTrigger
     *          the trigger to remove.
     * @return true if component has been removed; false otherwise.
     */
    public boolean removeTrigger(TextBoxTrigger tbTrigger) {
        if (((RelationalEntity) component).removeTrigger((Trigger) tbTrigger
                .getAssociedComponent())) {
            component.notifyObservers();

            updateHeight();

            return true;
        }

        return false;
    }

    @Override
    public boolean removeTextBox(TextBox tb) {
        if (tb instanceof TextBoxAttribute)
            return removeAttribute((TextBoxAttribute) tb);
        else if (tb instanceof TextBoxTrigger)
            return removeTrigger((TextBoxTrigger) tb);
        return false;
    }

    @Override
    public void update(Observable observable, Object object) {
        boolean enable = false;
        if (object != null && object.getClass() == UpdateMessage.class)
            switch ((UpdateMessage) object) {
                case ADD_ATTRIBUTE:
                    enable = true;
                case ADD_ATTRIBUTE_NO_EDIT:
                    addAttribute(((RelationalEntity) component).getLastAddedAttribute(), enable);
                    break;
                case ADD_TRIGGER:
                    enable = true;
                case ADD_TRIGGER_NO_EDIT:
                    addTrigger(((RelationalEntity) component).getLastAddedTrigger(), enable);
                default:
                    super.update(observable, object);
                    break;
            }
        else
            regenerateEntity();
    }

    @Override
    protected void initializeMenuItemsAddElements(JPopupMenu popupmenu) {
        popupMenu.add(makeMenuItem("Add attribute", "AddAttribute", "attribute"));
        popupMenu.add(makeMenuItem("Add method", "AddMethod", "method"));
        popupMenu.addSeparator();
    }

    @Override
    protected void initializeMenuItemsPropertiesElements(JPopupMenu popupMenu) {
        popupMenu.add(menuItemAbstract = makeMenuItem("Abstract", "Abstract",
                "abstract"));
        popupMenu.add(menuItemStatic = makeMenuItem("Static", "Static", "static"));
        popupMenu.addSeparator();
    }

    @Override
    protected void initializeMenuViews(JPopupMenu popupMenu) {
        JMenu subMenu;
        subMenu = new JMenu("View");
        subMenu.setIcon(PersonalizedIcon.createImageIcon(Slyum.ICON_PATH
                + "eye.png"));
        groupView = new ButtonGroup();

        // Item Default
        menuItemViewDefault = makeRadioButtonMenuItem("Default", "ViewDefault",
                groupView);
        menuItemViewDefault.setSelected(true);
        subMenu.add(menuItemViewDefault);

        // Item All
        subMenu.add(
                menuItemViewAll = makeRadioButtonMenuItem("All", "ViewAll",
                        groupView), 1);

        // Item Only attributes
        subMenu.add(
                menuItemViewAttributes = makeRadioButtonMenuItem("Only attributes",
                        "ViewAttribute", groupView), 2);

        // Item Only methods
        subMenu.add(
                menuItemViewTriggers = makeRadioButtonMenuItem("Only Methods",
                        "ViewMethods", groupView), 3);

        // Item Nothing
        subMenu.add(menuItemViewNothing = makeRadioButtonMenuItem("Nothing",
                "ViewNothing", groupView));

        popupMenu.add(subMenu);

        // Menu VIEW METHODS
        subMenu = new JMenu("Methods View");
        subMenu.setIcon(PersonalizedIcon.createImageIcon(Slyum.ICON_PATH
                + "eye.png"));
        groupViewTriggers = new ButtonGroup();

        menuItemTriggersDefault = makeRadioButtonMenuItem("Default",
                "ViewMethodsDefault", groupViewTriggers);
        menuItemTriggersDefault.setSelected(true);
        subMenu.add(menuItemTriggersDefault);

        subMenu.add(
                menuItemTriggersAll = makeRadioButtonMenuItem("Type and Name",
                        "ViewTypeAndName", groupViewTriggers), 1);

        subMenu.add(
                menuItemTriggersType = makeRadioButtonMenuItem("Type", "ViewType",
                        groupViewTriggers), 2);

        subMenu.add(
                menuItemTriggersName = makeRadioButtonMenuItem("Name", "ViewName",
                        groupViewTriggers), 3);

        subMenu.add(menuItemTriggersNothing = makeRadioButtonMenuItem("Nothing",
                "ViewMethodNothing", groupViewTriggers));

        popupMenu.add(subMenu);
        popupMenu.addSeparator();
    }

    @Override
    protected void innerRegenerate() {
        triggersView.clear();
        attributesView.clear();
        for (RelationalAttribute a : ((RelationalEntity) component).getAttributes())
            addAttribute(a, false);
    }

    @Override
    protected int paintTextBoxes(Graphics2D g2, Rectangle bounds,
                                 int textboxHeight, int offset) { //TODO add keys here

        if (displayAttributes) {
            // draw attributs separator
            offset += 10;
            g2.setStroke(new BasicStroke(BORDER_WIDTH));
            g2.setColor(DEFAULT_BORDER_COLOR);
            g2.drawLine(bounds.x, offset, bounds.x + bounds.width, offset);

            // draw attributes
            for (TextBoxRelAttribute tb : attributesView) {
                tb.setBounds(new Rectangle(bounds.x + 8, offset + 2, bounds.width - 15,
                        textboxHeight + 2));
                tb.paintComponent(g2);

                offset += textboxHeight;
            }
        }

        if (displayTriggers) {
            // draw methods separator
            offset += 10;
            g2.setStroke(new BasicStroke(BORDER_WIDTH));
            g2.setColor(DEFAULT_BORDER_COLOR);
            g2.drawLine(bounds.x, offset, bounds.x + bounds.width, offset);

            // draw methods
            for (final TextBoxTrigger tb : triggersView) {
                tb.setBounds(new Rectangle(bounds.x + 8, offset + 2, bounds.width - 15,
                        textboxHeight + 2));
                tb.paintComponent(g2);
                offset += textboxHeight;
            }
        }
        return offset;
    }

    /**
     * Method called before creating a new attribute, if modifications on
     * attribute is necessary.
     *
     * @param attribute
     *          the attribute to prepare
     */
    protected void prepareNewAttribute(RelationalAttribute attribute){}

    private void updateMenuItemMethodsView() {
        JMenuItem itemToSelect;
        ParametersViewStyle newView = null;

        if (pressedTextBox == null) {
            // Check si toutes les méthodes des entités sélectionnées ont la même vue.
            List<RelationalEntityView> selected = getSelectedRelationalEntityView(parent);
            List<TextBoxTrigger> textbox = new LinkedList<>();
            for (RelationalEntityView view : selected)
                textbox.addAll(view.triggersView);

            for (int i = 0; i < textbox.size() - 1; i++) {
                Trigger current = (Trigger) textbox.get(i).getAssociedComponent();
                Trigger next = (Trigger) textbox
                        .get(i + 1).getAssociedComponent();
                /*if (!current.getConcretParametersViewStyle().equals(
                        next.getConcretParametersViewStyle())) {
                    groupViewTriggers.clearSelection();
                    return;
                }*/
            }

            if (textbox.size() > 0)
                newView = ((Method) textbox.get(0).getAssociedComponent())
                        .getConcretParametersViewStyle();
        } else if (pressedTextBox instanceof TextBoxMethod) {
            newView = ((Method) pressedTextBox.getAssociedComponent())
                    .getConcretParametersViewStyle();
        }

        if (newView != null) {
            switch (newView) {
                case DEFAULT:
                    itemToSelect = menuItemTriggersDefault;
                    break;
                case NAME:
                    itemToSelect = menuItemTriggersName;
                    break;

                case NOTHING:
                    itemToSelect = menuItemTriggersNothing;
                    break;

                case TYPE:
                    itemToSelect = menuItemTriggersType;
                    break;

                case TYPE_AND_NAME:
                    itemToSelect = menuItemTriggersAll;
                    break;

                default:
                    itemToSelect = menuItemTriggersAll;
                    break;
            }

            groupViewTriggers.setSelected(itemToSelect.getModel(), true);
        }
    }

    private void updateMenuItemView() {
        JMenuItem menuItemToSelect;

        // Check si toutes les entités sélectionnées ont le même type de vue.
        List<RelationalEntityView> selected = getSelectedRelationalEntityView(parent);
        for (int i = 0; i < selected.size() - 1; i++) {
            RelationalEntityView view = selected.get(i), next = selected.get(i + 1);
            if (view.displayDefault != next.displayDefault
                    || view.displayAttributes != next.displayAttributes
                    || view.displayTriggers != next.displayTriggers) {
                groupView.clearSelection();
                return;
            }
        }

        if (displayDefault)
            menuItemToSelect = menuItemViewDefault;
        else if (displayAttributes && displayTriggers)
            menuItemToSelect = menuItemViewAll;
        else if (displayAttributes)
            menuItemToSelect = menuItemViewAttributes;
        else if (displayTriggers)
            menuItemToSelect = menuItemViewTriggers;
        else
            menuItemToSelect = menuItemViewNothing;

        groupView.setSelected(menuItemToSelect.getModel(), true);
    }

    @Override
    public void restore() {
        super.restore();
        parent.addEntityWithRelations(this, this.middleBounds());
        restoreEntity();
        repaint();
    }

    protected void restoreEntity() {
        if (parent.getClassDiagram().searchComponentById(getAssociedComponent().getId()) == null)
            parent.getClassDiagram().addTableEntity((RelationalEntity) getAssociedComponent());
    }

}
