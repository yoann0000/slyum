package graphic.textbox;

import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.RelationalAttribute;
import graphic.GraphicView;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Observable;
import java.util.Observer;
import swing.PanelClassDiagram;
import utility.Utility;


public class TextBoxRelAttribute extends TextBox implements Observer {
    /**
     * Get a String representing the Attribute.
     *
     * @param attribute
     *          the attribute to convert to String
     * @return a String representing the Attribute.
     */
    public static String getStringFromAttribute(RelationalAttribute attribute) {
        StringBuilder attr = new StringBuilder(attribute.getName() + getFullStringType(attribute));
        if (attribute.isUnique())
            attr.append(" UNIQUE");
        if (attribute.isNotNull())
            attr.append(" NOT NULL");
        return attr.toString();
    }

    public static String getFullStringType(RelationalAttribute attribute) {
        return " : " + attribute.getType();
    }

    private final RelationalAttribute attribute;

    /**
     * Create a new TextBoxAttribute with the given Attribute.
     *
     * @param parent
     *          the graphic view
     * @param attribute
     *          the attribute
     */
    public TextBoxRelAttribute(GraphicView parent, RelationalAttribute attribute) {
        super(parent, getStringFromAttribute(attribute));

        if (attribute == null)
            throw new IllegalArgumentException("attribute is null");

        this.attribute = attribute;
        attribute.addObserver(this);
    }

    @Override
    public void createEffectivFont() {
        effectivFont = getFont();
    }

    @Override
    public IDiagramComponent getAssociedComponent() {
        return attribute;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        if (bounds == null) throw new IllegalArgumentException("bounds is null");

        this.bounds = new Rectangle(bounds);
    }

    @Override
    public String getText() {
        String text = getStringFromAttribute(attribute);
        if (!PanelClassDiagram.getInstance().getClassDiagram().getDefaultVisibleTypes())
            text = text.replace(getFullStrungType(), "");
        return text;
    }

    @Override
    public String getEditingText() {
        return getStringFromAttribute(attribute);
    }

    public String getFullStrungType() {
        return getFullStringType(attribute);
    }

    @Override
    public void setSelected(boolean select) {
        if (isSelected() != select) {
            super.setSelected(select);
            attribute.select();
            if (select)
                attribute.notifyObservers(UpdateMessage.SELECT);
            else
                attribute.notifyObservers(UpdateMessage.UNSELECT);
        }
    }

    @Override
    public void setText(String text) {
        attribute.setText(text);
        super.setText(getStringFromAttribute(attribute));
    }

    @Override
    public String getFullString() {
        return getStringFromAttribute(attribute);
    }

    @Override
    protected String truncate(Graphics2D g2, String text, int width) {
        return Utility.truncate(g2, text, width);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof UpdateMessage)
            switch ((UpdateMessage) o) {
                case SELECT:
                    setSelected(true);
                    break;
                case UNSELECT:
                    setSelected(false);
                    break;
                default:
                    break;
            }
        else
            super.setText(getStringFromAttribute(attribute));

        repaint();
    }

    @Override
    protected boolean mustPaintSelectedStyle() {
        return mouseHover;
    }
}
