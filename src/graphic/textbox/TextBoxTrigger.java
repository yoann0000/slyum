package graphic.textbox;

import classDiagram.IDiagramComponent;
import classDiagram.components.Trigger;
import graphic.GraphicView;
import utility.Utility;

import java.awt.*;
import java.text.AttributedString;
import java.util.Observable;
import java.util.Observer;

public class TextBoxTrigger extends TextBox implements Observer {

    private final Trigger trigger;

    /**
     * Create a new TextBoxMethod with the given Method.
     *
     * @param parent the graphic view
     * @param trigger the method
     */
    public TextBoxTrigger(GraphicView parent, Trigger trigger) {
        super(parent, trigger.getName());
        this.trigger = trigger;
        trigger.addObserver(this);
    }

    @Override
    public void createEffectivFont() {
        effectivFont = getFont();
    }

    @Override
    public IDiagramComponent getAssociedComponent() {
        return trigger;
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
    public String getEditingText() {
        return trigger.getName();
    }

    @Override
    public void setSelected(boolean select) {
        if (isSelected() != select) {
            super.setSelected(select);

            trigger.select();

            if (select)
                trigger.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            else
                trigger.notifyObservers(IDiagramComponent.UpdateMessage.UNSELECT);
        }
    }

    @Override
    public String getText() {
        return trigger.getName();
    }

    @Override
    public void setText(String text) {
        trigger.setName(text);
        super.setText(trigger.getName());
    }

    @Override
    public void initAttributeString(AttributedString ats) {}

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof IDiagramComponent.UpdateMessage) {
            switch ((IDiagramComponent.UpdateMessage) o) {
                case SELECT:
                    setSelected(true);
                    break;
                case UNSELECT:
                    setSelected(false);
                    break;
                default:
                    break;
            }
        } else {
            String text = trigger.getName();
            super.setText(text);
        }

        repaint();
    }

    @Override
    protected boolean mustPaintSelectedStyle() {
        return mouseHover;
    }

    @Override
    protected String truncate(Graphics2D g2, String text, int width) {
        return Utility.truncate(g2, text, width);
    }
}
