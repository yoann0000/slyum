package graphic.entity;

import classDiagram.components.Entity;
import graphic.GraphicView;
import graphic.textbox.TextBox;

import javax.swing.*;
import java.awt.*;

public class RelationalEntityView extends EntityView{

    public RelationalEntityView(GraphicView parent, Entity component) {
        super(parent, component);
    }

    @Override
    public boolean removeTextBox(TextBox tb) {
        return false;
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
        return 0;
    }
}
