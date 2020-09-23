package graphic.textbox;

import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.Key;
import graphic.GraphicView;
import utility.Utility;

import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class TextBoxKey extends TextBox implements Observer {
    private final Key key;
    int keyType;

    /**
     * Create a new TextBoxAttribute with the given Attribute.
     *
     * @param parent
     *          the graphic view
     * @param key
     *          the key
     */
    public TextBoxKey(GraphicView parent, Key key, int keyType) {
        super(parent, getStringFromKey(key, keyType));
        this.keyType = keyType;
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
    public static String getStringFromKey(Key key, int keyType) {
        return key.keyName(keyType);
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
        return getStringFromKey(key, keyType);
    }

    @Override
    public String getEditingText() {
        return getStringFromKey(key, keyType);
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
        super.setText(getStringFromKey(key, keyType));
    }

    @Override
    public String getFullString() {
        return getStringFromKey(key, keyType);
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
            super.setText(getStringFromKey(key, keyType));
        repaint();
    }

    @Override
    protected boolean mustPaintSelectedStyle() {
        return mouseHover;
    }
}
