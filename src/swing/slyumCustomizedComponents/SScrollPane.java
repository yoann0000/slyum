package swing.slyumCustomizedComponents;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 *
 * @author David Miserez <david.miserez@heig-vd.ch>
 */
public class SScrollPane extends JScrollPane {

  public SScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
    super(view, vsbPolicy, hsbPolicy);
    initialize();
  }

  public SScrollPane(Component view) {
    super(view);
    initialize();
  }

  public SScrollPane(int vsbPolicy, int hsbPolicy) {
    super(vsbPolicy, hsbPolicy);
    initialize();
  }

  public SScrollPane() {
    initialize();
  }
  
  private void initialize() {
    setBackground(Color.white);
  }

  @Override
  public JScrollBar createHorizontalScrollBar() {
    return new SScrollBar(JScrollPane.ScrollBar.HORIZONTAL);
  }

  @Override
  public JScrollBar createVerticalScrollBar() {
    return new SScrollBar(JScrollPane.ScrollBar.VERTICAL);
  }
}
