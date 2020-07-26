package swing;

import graphic.GraphicView;
import graphic.factory.*;
import swing.slyumCustomizedComponents.SButton;
import swing.slyumCustomizedComponents.SSeparator;
import swing.slyumCustomizedComponents.SToolBar;
import swing.slyumCustomizedComponents.SToolBarButton;
import utility.PersonalizedIcon;
import utility.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SPanelDiagramComponent extends SToolBar implements ActionListener{

  public enum Mode {
    CURSOR(getInstance().btnCursorMode), GRIP(getInstance().btnGripMode);

    private final SButton btnMode;

    Mode(SButton btn) {
      btnMode = btn;
    }

    public SButton getBtnMode() {
      return btnMode;
    }
  }

  private static final String TT_MODE_CURSOR = "Default cursor "
          + Utility.keystrokeToString(Slyum.KEY_DEFAULT_MODE);

  private static final String TT_MODE_GRIP = "Add grips "
          + Utility.keystrokeToString(Slyum.KEY_GRIPS_MODE);

  private static final String TT_CLASS = "Class "
          + Utility.keystrokeToString(Slyum.KEY_CLASS);

  private static final String TT_ENUM = "Enum "
          + Utility.keystrokeToString(Slyum.KEY_ENUM);

  private static final String TT_INTERFACE = "Interface "
          + Utility.keystrokeToString(Slyum.KEY_INTERFACE);

  private static final String TT_CLASS_ASSOC = "Association class "
          + Utility.keystrokeToString(Slyum.KEY_ASSOCIATION_CLASS);

  private static final String TT_GENERALIZE = "Generalize & Realize "
          + Utility.keystrokeToString(Slyum.KEY_INHERITANCE);

  private static final String TT_DEPENDENCY = "Dependency "
          + Utility.keystrokeToString(Slyum.KEY_DEPENDENCY);

  private static final String TT_INNER_CLASS = "Inner class "
          + Utility.keystrokeToString(Slyum.KEY_INNER_CLASS);

  private static final String TT_ASSOCIATION = "Association "
          + Utility.keystrokeToString(Slyum.KEY_ASSOCIATION);

  private static final String TT_AGGREGATION = "Aggregation "
          + Utility.keystrokeToString(Slyum.KEY_AGGREGATION);

  private static final String TT_COMPOSITION = "Composition "
          + Utility.keystrokeToString(Slyum.KEY_COMPOSITION);

  private static final String TT_MULTI = "Multi-association "
          + Utility.keystrokeToString(Slyum.KEY_MULTI_ASSOCIATION);

  private static final String TT_NOTE = "Note "
          + Utility.keystrokeToString(Slyum.KEY_NOTE);

  private static final String TT_LINK_NOTE = "Link note "
          + Utility.keystrokeToString(Slyum.KEY_LINK_NOTE);

  private static final String TT_REL_TABLE = "Table "
          + Utility.keystrokeToString(Slyum.KEY_TABLE);

  private static final String TT_REL_ASSOCIATION = "Relational association "
          + Utility.keystrokeToString(Slyum.KEY_REL_ASSOCIATION);

  private static final String TT_VIEW = "View "
          + Utility.keystrokeToString(Slyum.KEY_REL_VIEW);

  private static final String TT_CONVERT = "Convert "
          + Utility.keystrokeToString(Slyum.KEY_CONVERT);

  private static final String TT_SQL = "Sql "
          + Utility.keystrokeToString(Slyum.KEY_SQL);


  private final SButton btnCursorMode, btnGripMode, btnClass, btnEnum, btnInterface,
          btnClassAssociation, btnGeneralize, btnDependeny, btnInnerClass,
          btnAssociation, btnAggregation, btnComposition, btnMulti, btnNote,
          btnLinkNote, btnTable, btnAssocRel, btnView, btnUmlRel, btnRelSql;

  private Mode currentMode;
  private static SPanelDiagramComponent instance;

  public static SPanelDiagramComponent getInstance() {
    if (instance == null) instance = new SPanelDiagramComponent();
    return instance;
  }

  private SPanelDiagramComponent() {

    add(btnCursorMode = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH
                    + "pointer-arrow.png"), Slyum.ACTION_MODE_CURSOR,
            Color.RED, TT_MODE_CURSOR));

    add(btnGripMode = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH
                    + "pointer-grip.png"), Slyum.ACTION_MODE_GRIP, Color.RED,
            TT_MODE_GRIP));

    add(new SSeparator());

    add(btnClass = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "class.png"),
            Slyum.ACTION_NEW_CLASS, Color.RED, TT_CLASS));

    add(btnInterface = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "interface.png"),
            Slyum.ACTION_NEW_INTERFACE, Color.RED, TT_INTERFACE));

    add(btnEnum = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "enum.png"),
            Slyum.ACTION_NEW_ENUM, Color.RED, TT_ENUM));

    add(btnClassAssociation = createSButton(
            PersonalizedIcon
                    .createImageIcon(Slyum.ICON_PATH + "classAssoc.png"),
            Slyum.ACTION_NEW_CLASS_ASSOCIATION, Color.RED, TT_CLASS_ASSOC));

    add(new SSeparator());

    add(btnGeneralize = createSButton(
            PersonalizedIcon
                    .createImageIcon(Slyum.ICON_PATH + "generalize.png"),
            Slyum.ACTION_NEW_GENERALIZE, Color.RED, TT_GENERALIZE));

    add(btnDependeny = createSButton(
            PersonalizedIcon
                    .createImageIcon(Slyum.ICON_PATH + "dependency.png"),
            Slyum.ACTION_NEW_DEPENDENCY, Color.RED, TT_DEPENDENCY));

    add(btnInnerClass = createSButton(
            PersonalizedIcon
                    .createImageIcon(Slyum.ICON_PATH + "innerClass.png"),
            Slyum.ACTION_NEW_INNER_CLASS, Color.RED, TT_INNER_CLASS));

    add(new SSeparator());
    
    add(btnTable = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "tableRel.png"),
            Slyum.ACTION_NEW_TABLE, Color.RED, TT_REL_TABLE));

    add(btnView = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "view.png"),
            Slyum.ACTION_NEW_REL_VIEW, Color.RED, TT_VIEW));

    add(btnAssocRel = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "relAssociation.png"),
            Slyum.ACTION_NEW_REL_ASSOCIATION, Color.RED, TT_REL_ASSOCIATION));

    btnTable.setEnabled(false);
    btnView.setEnabled(false);
    btnAssocRel.setEnabled(false);

    add(new SSeparator());

    add(btnAssociation = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH
                    + "association.png"), Slyum.ACTION_NEW_ASSOCIATION,
            Color.RED, TT_ASSOCIATION));

    add(btnAggregation = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH
                    + "aggregation.png"), Slyum.ACTION_NEW_AGGREGATION,
            Color.RED, TT_AGGREGATION));

    add(btnComposition = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH
                    + "composition.png"), Slyum.ACTION_NEW_COMPOSITION,
            Color.RED, TT_COMPOSITION));

    add(btnMulti = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "multi.png"),
            Slyum.ACTION_NEW_MULTI, Color.RED, TT_MULTI));

    add(new SSeparator());

    add(btnNote = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "note.png"),
            Slyum.ACTION_NEW_NOTE, Color.RED, TT_NOTE));

    add(btnLinkNote = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "linkNote.png"),
            Slyum.ACTION_NEW_LINK_NOTE, Color.RED, TT_LINK_NOTE));

    add(new SSeparator());

    add(btnUmlRel = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "newRel.png"),
            Slyum.ACTION_CONVERT_UML, Color.RED, TT_CONVERT));

    add(btnRelSql = createSButton(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "newSql.png"),
            Slyum.ACTION_CONVERT_SQL, Color.RED, TT_SQL));
  }

  public void setButtonModeStyle(SButton button) {
    // Réinitialise le style de tous les boutons.
    synchronized (getTreeLock()) {
      for (Component c : getComponents())
        if (c instanceof SButton) ((SButton) c).resetBackground();
    }

    // Attribut le nouveau bouton définissant le mode.
    button.setBackground(Slyum.THEME_COLOR.brighter());
    button.setContentAreaFilled(true);
  }

  private SButton createSButton(ImageIcon ii, String a, Color c, String tt) {
    return new SToolBarButton(ii, a, c, tt, this);
  }

  public Mode getMode() {
    return currentMode;
  }

  public void setMode(Mode newMode) {
    currentMode = newMode;
    PropertyLoader.getInstance().getProperties()
            .put(PropertyLoader.MODE_CURSOR, currentMode.toString());
    PropertyLoader.getInstance().push();
    MultiViewManager.getSelectedGraphicView().deleteCurrentFactory();
    setButtonModeStyle(newMode.getBtnMode());
  }

  public void applyMode() {
    setButtonModeStyle(currentMode.getBtnMode());
  }

  public void updateButtons(){
    GraphicView gv = MultiViewManager.getSelectedGraphicView();
    boolean isRel = gv.isRelational();
    btnTable.setEnabled(isRel);
    btnView.setEnabled(isRel);
    btnAssocRel.setEnabled(isRel);
    btnClass.setEnabled(!isRel);
    btnInterface.setEnabled(!isRel);
    btnEnum.setEnabled(!isRel);
    btnClassAssociation.setEnabled(!isRel);
    btnGeneralize.setEnabled(!isRel);
    btnDependeny.setEnabled(!isRel);
    btnInnerClass.setEnabled(!isRel);
    btnAssociation.setEnabled(!isRel);
    btnAggregation.setEnabled(!isRel);
    btnComposition.setEnabled(!isRel);
    btnMulti.setEnabled(!isRel);
    btnUmlRel.setEnabled(!isRel);
    btnRelSql.setEnabled(isRel);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    
    // Disable shortcut if user is currently editing text.
    if (Slyum.getInstance().getFocusOwner() instanceof JTextField ||
        Slyum.getInstance().getFocusOwner() instanceof JTextArea)
      return;
    
    GraphicView gv = MultiViewManager.getSelectedGraphicView();
    switch (e.getActionCommand()) {
      case Slyum.ACTION_NEW_TABLE:
        gv.initNewComponent(new TableFactory(gv));
        break;
      case Slyum.ACTION_NEW_REL_ASSOCIATION:
        gv.initNewComponent(new RelAssociationFactory(gv));
        break;
      case Slyum.ACTION_NEW_REL_VIEW:
        gv.initNewComponent(new RelViewFactory(gv));
        break;
      case Slyum.ACTION_NEW_CLASS:
        gv.initNewComponent(new ClassFactory(gv));
        break;
      case Slyum.ACTION_NEW_INTERFACE:
        gv.initNewComponent(new InterfaceFactory(gv));
        break;
      case Slyum.ACTION_NEW_ENUM:
        gv.initNewComponent(new EnumFactory(gv));
        break;
      case Slyum.ACTION_NEW_GENERALIZE:
        gv.initNewComponent(new InheritanceFactory(gv));
        break;
      case Slyum.ACTION_NEW_INNER_CLASS:
        gv.initNewComponent(new InnerClassFactory(gv));
        break;
      case Slyum.ACTION_NEW_DEPENDENCY:
        gv.initNewComponent(new DependencyFactory(gv));
        break;
      case Slyum.ACTION_NEW_ASSOCIATION:
        gv.initNewComponent(new BinaryFactory(gv));
        break;
      case Slyum.ACTION_NEW_AGGREGATION:
        gv.initNewComponent(new AggregationFactory(gv));
        break;
      case Slyum.ACTION_NEW_COMPOSITION:
        gv.initNewComponent(new CompositionFactory(gv));
        break;
      case Slyum.ACTION_NEW_CLASS_ASSOCIATION:
        gv.initNewComponent(new AssociationClassFactory(gv));
        break;
      case Slyum.ACTION_NEW_MULTI:
        gv.initNewComponent(new MultiFactory(gv));
        break;
      case Slyum.ACTION_NEW_NOTE:
        gv.initNewComponent(new NoteFactory(gv));
        break;
      case Slyum.ACTION_NEW_LINK_NOTE:
        gv.initNewComponent(new LineCommentaryFactory(gv));
        break;
      case Slyum.ACTION_MODE_CURSOR:
        setMode(Mode.CURSOR);
        break;
      case Slyum.ACTION_MODE_GRIP:
        setMode(Mode.GRIP);
        break;
      case Slyum.ACTION_CONVERT_UML:
        MultiViewManager.ConvertSelectedView();
        break;
      case Slyum.ACTION_CONVERT_SQL:
        MultiViewManager.SQLSelectedView();
        break;
    }
  }

  public SButton getBtnClass() {
    return btnClass;
  }

  public SButton getBtnInterface() {
    return btnInterface;
  }

  public SButton getBtnClassAssociation() {
    return btnClassAssociation;
  }

  public SButton getBtnGeneralize() {
    return btnGeneralize;
  }

  public SButton getBtnDependency() {
    return btnDependeny;
  }

  public SButton getBtnInnerClass() {
    return btnInnerClass;
  }

  public SButton getBtnAssociation() {
    return btnAssociation;
  }

  public SButton getBtnAggregation() {
    return btnAggregation;
  }

  public SButton getBtnComposition() {
    return btnComposition;
  }

  public SButton getBtnMulti() {
    return btnMulti;
  }

  public SButton getBtnNote() {
    return btnNote;
  }

  public SButton getBtnLinkNote() {
    return btnLinkNote;
  }

  public SButton getBtnEnum() {
    return btnEnum;
  }

  public SButton getBtnTable() {
    return btnTable;
  }

  public SButton getBtnAssocRel() {
    return btnAssocRel;
  }

  public SButton getBtnView() {
    return btnView;
  }
}
