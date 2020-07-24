package graphic.textbox;

import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.Attribute;
import classDiagram.components.Key;
import graphic.GraphicView;
import utility.Utility;

import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class TextBoxKey extends TextBox implements Observer {
    private final Key key;

    /**
     * Create a new TextBoxAttribute with the given Attribute.
     *
     * @param parent
     *          the graphic view
     * @param key
     *          the key
     */
    public TextBoxKey(GraphicView parent, Key key) {
        super(parent, getStringFromKey(key));

        this.key = key;
        key.addObserver(this);
    }

    /**
     * Get a String representing the Key.
     *
     * @param key
     *          the key to convert to String
     * @return a String representing the key.
     */
    public static String getStringFromKey(Key key) {
        StringBuilder sb = new StringBuilder();
        sb.append(key.getName());
        if(key.getTable().getPrimaryKey() == key)
            sb.append(" ").append("<PK>");
        if(key.getTable().getForeignKeys().contains(key))
            sb.append(" ").append("<FK : ").append(key.getTable().getName()).append(">");
        if (key.getTable().getAlternateKeys().contains(key))
            sb.append(" ").append("<AK>");
        return sb.toString();
    }

    @Override
    public void createEffectivFont() {
        effectivFont = getFont();
    }

    @Override
    public IDiagramComponent getAssociedComponent() {
        return key;
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
        return getStringFromKey(key);
    }

    @Override
    public String getEditingText() {
        return getStringFromKey(key);
    }

    @Override
    public void setSelected(boolean select) {
        if (isSelected() != select) {
            super.setSelected(select);
            key.select();
            if (select)
                key.notifyObservers(UpdateMessage.SELECT);
            else
                key.notifyObservers(UpdateMessage.UNSELECT);
        }
    }

    @Override
    public void setText(String text) {
        super.setText(getStringFromKey(key));
    }

    @Override
    public String getFullString() {
        return getStringFromKey(key);
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
            super.setText(getStringFromKey(key));

        repaint();
    }

    @Override
    protected boolean mustPaintSelectedStyle() {
        return mouseHover;
    }
}
