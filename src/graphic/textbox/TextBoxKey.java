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
    private final boolean foreign;

    /**
     * Create a new TextBoxAttribute with the given Attribute.
     *
     * @param parent
     *          the graphic view
     * @param key
     *          the key
     * @param foreign
     *          if the key is foreign
     */
    public TextBoxKey(GraphicView parent, Key key, boolean foreign) {
        super(parent, key.getFullKeyString(foreign));

        this.key = key;
        this.foreign = foreign;
        key.addObserver(this);
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
        return key.getFullKeyString(foreign);
    }

    @Override
    public String getEditingText() {
        return key.getFullKeyString(foreign);
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

    }

    @Override
    public String getFullString() {
        return key.getFullKeyString(foreign);
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
            super.setText(key.getFullKeyString(foreign));

        repaint();
    }

    @Override
    protected boolean mustPaintSelectedStyle() {
        return mouseHover;
    }
}
