package swing.propretiesView;

import classDiagram.ClassDiagram;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.Method;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

import graphic.relations.RelationView;
import swing.MultiViewManager;
import swing.PanelClassDiagram;
import swing.Slyum;
import swing.slyumCustomizedComponents.FlatButton;
import swing.slyumCustomizedComponents.SCheckBox;
import swing.slyumCustomizedComponents.SComboBox;
import utility.PersonalizedIcon;
import utility.RelValidation.RelValidator;

public class DiagramPropreties 
    extends GlobalPropreties 
    implements ActionListener {

  private static DiagramPropreties instance;

  public static String getDiagramsInformations() {
    return getInstance().txaDiagramsInformations.getText();
  }
  
  public static void setDiagramsInformations(String informations) {
    getInstance().txaDiagramsInformations.setText(informations);
  }
  
  public static void clearDiagramsInformation() {
    getInstance().txaDiagramsInformations.setText("");
  }
  
  public static DiagramPropreties getInstance() {
    if (instance == null) instance = new DiagramPropreties();
    return instance;
  }

  public static void updateComponentInformations() {
    instance.updateComponentInformations(null);
  }

  JPanel west = createJPanelInformations(),
         panelInformations = createJPanelInformations(),
          relValidator = createJPanelInformations();
  
  private final String ACTION_ENTITY_VIEW = "1",
                       ACTION_METHODS_VIEW = "2",
                       ACTION_VISIBLE_TYPE = "3",
                       ACTION_VISIBLE_ENUM = "4",
                       ACTION_VALIDATE = "5";
  
  private final SComboBox<ClassDiagram.ViewEntity> cbbEntityView;
  private final SComboBox<Method.ParametersViewStyle> cbbParametersView;
  private final SCheckBox chkDisplayTypes;
  private final SCheckBox chkViewEnum;
  private final JTextArea txaDiagramsInformations, txtRelVal;
  
  private boolean raiseEvent;

  private final RelValidator rv = RelValidator.getInstance();

  private DiagramPropreties() {
    
    final Dimension CCB_DIMENSION = new Dimension(130, 25);
    final int HEIGHT_STRUT = 5;
    
    PanelClassDiagram.getInstance().getClassDiagram().addObserver(this);
    
    GroupLayout layout = new GroupLayout(this);
    setLayout(layout);

    // Informations générales
    west.add(new JLabel("Entities view types"));
    cbbEntityView = 
        new SComboBox<>(ClassDiagram.ViewEntity.values());
    cbbEntityView.setMaximumSize(CCB_DIMENSION);
    cbbEntityView.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbbEntityView.setActionCommand(ACTION_ENTITY_VIEW);
    cbbEntityView.addActionListener(this);
    west.add(cbbEntityView);
    west.add(Box.createVerticalStrut(HEIGHT_STRUT));
    
    west.add(new JLabel("Methods view type"));
    cbbParametersView = 
        new SComboBox<>(Method.ParametersViewStyle.values());
    cbbParametersView.removeItemAt(0);
    cbbParametersView.setMaximumSize(CCB_DIMENSION);
    cbbParametersView.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbbParametersView.setActionCommand(ACTION_METHODS_VIEW);
    cbbParametersView.addActionListener(this);
    west.add(cbbParametersView);
    west.add(Box.createVerticalStrut(HEIGHT_STRUT));
    
    chkViewEnum = new SCheckBox("View enum values");
    chkViewEnum.setAlignmentX(Component.LEFT_ALIGNMENT);
    chkViewEnum.setBackground(Color.WHITE);
    chkViewEnum.setActionCommand(ACTION_VISIBLE_ENUM);
    chkViewEnum.addActionListener(this);
    west.add(chkViewEnum);
    west.add(Box.createVerticalStrut(HEIGHT_STRUT-5));
    
    chkDisplayTypes = new SCheckBox("Display types");
    chkDisplayTypes.setAlignmentX(Component.LEFT_ALIGNMENT);
    chkDisplayTypes.setBackground(Color.WHITE);
    chkDisplayTypes.setActionCommand(ACTION_VISIBLE_TYPE);
    chkDisplayTypes.addActionListener(this);
    west.add(chkDisplayTypes);
    
    JPanel pnlDiagramProperties = new JPanel();
    pnlDiagramProperties.setLayout(
        new BoxLayout(pnlDiagramProperties, BoxLayout.Y_AXIS));
    pnlDiagramProperties.setMaximumSize(new Dimension(140, Short.MAX_VALUE));
    
    JLabel lblTitle = new JLabel("Project's properties");
    lblTitle.setHorizontalTextPosition(JLabel.LEFT);
    lblTitle.setVerticalTextPosition(JLabel.BOTTOM);
    lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 0));
    
    pnlDiagramProperties.add(lblTitle);
    pnlDiagramProperties.add(west);
    
    JPanel pnlDiagramInformations = new JPanel();
    pnlDiagramInformations.setLayout(
        new BoxLayout(pnlDiagramInformations, BoxLayout.Y_AXIS));
    pnlDiagramInformations.setMaximumSize(new Dimension(140, Short.MAX_VALUE));
    
    JLabel lblInformationsTitle = new JLabel("Project's informations");
    lblInformationsTitle.setHorizontalTextPosition(JLabel.LEFT);
    lblInformationsTitle.setVerticalTextPosition(JLabel.BOTTOM);
    lblInformationsTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 0));

    JLabel lblRelValidator = new JLabel("Relational Diagram Validator");
    lblInformationsTitle.setHorizontalTextPosition(JLabel.LEFT);
    lblInformationsTitle.setVerticalTextPosition(JLabel.BOTTOM);
    lblInformationsTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 0));
    
    panelInformations.setBorder(null);
    
    txaDiagramsInformations = new JTextArea();
    txaDiagramsInformations.setLineWrap(true);
    txaDiagramsInformations.setWrapStyleWord(true);
    txaDiagramsInformations.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        
        change.Change.setHasChange(true);
        PanelClassDiagram.getInstance().getClassDiagram().setInformation(txaDiagramsInformations.getText());
      }
      
    });

    relValidator.add(lblRelValidator);

    txtRelVal = new JTextArea();
    txtRelVal.setLineWrap(true);
    txtRelVal.setWrapStyleWord(true);
    txtRelVal.setEditable(false);

    JScrollPane textAreaPaneRel = new JScrollPane(txtRelVal);
    textAreaPaneRel.setPreferredSize(new Dimension(250, 0));
    textAreaPaneRel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    textAreaPaneRel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    relValidator.setBackground(null);
    relValidator.add(textAreaPaneRel);

    class ButtonValidate extends FlatButton {
      public ButtonValidate() {
        super("Validate Diagram", PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "check-mark.png"));
        setActionCommand(ACTION_VALIDATE);
        setMaximumSize(new Dimension(250, 100));
        setHorizontalAlignment(SwingUtilities.LEFT);
      }
    }

    ButtonValidate validateBtn = new ButtonValidate();
    validateBtn.addActionListener(this);
    relValidator.add(validateBtn);
    
    JScrollPane textAreaPane = new JScrollPane(txaDiagramsInformations);
    textAreaPane.setPreferredSize(new Dimension(250, 0));
    textAreaPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    textAreaPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    panelInformations.add(textAreaPane);
    
    pnlDiagramInformations.add(lblInformationsTitle);
    pnlDiagramInformations.add(panelInformations);

    //TODO create validator here

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(pnlDiagramProperties);
    add(Box.createHorizontalStrut(10));
    add(pnlDiagramInformations);
    add(Box.createHorizontalGlue());
    add(new JLabel("Select a component to see it's members"));
    add(Box.createHorizontalGlue());
    add(relValidator);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!raiseEvent)
      return;
    
    PanelClassDiagram p = PanelClassDiagram.getInstance();
    if (p == null) return;
    ClassDiagram cd = p.getClassDiagram();
    if (cd == null) return;
    switch (e.getActionCommand()) {
      case ACTION_ENTITY_VIEW:
        cd.setViewEntity(
            (ClassDiagram.ViewEntity) cbbEntityView.getSelectedItem());
        cd.notifyObservers(true);
        break;
      case ACTION_METHODS_VIEW:
        cd.setDefaultViewMethods(
            (Method.ParametersViewStyle) cbbParametersView.getSelectedItem());
        cd.notifyObservers();
        break;
      case ACTION_VISIBLE_ENUM:
        cd.setDefaultViewEnum(chkViewEnum.isSelected());
        cd.notifyObservers();
        break;
      case ACTION_VISIBLE_TYPE:
        cd.setVisibleType(chkDisplayTypes.isSelected());
        cd.notifyObservers();
        break;
      case ACTION_VALIDATE:
        if (MultiViewManager.getSelectedGraphicView().isRelational()){
          rv.setClassDiagram(cd);
          rv.validate();
          if (rv.getErrors() == 0) {
            txtRelVal.setForeground(Color.GREEN);
            txtRelVal.setText("No Errors");
          } else {
            txtRelVal.setForeground(Color.RED);
            txtRelVal.setText(rv.getErrorString());
          }
        } else {
          txtRelVal.setForeground(Color.RED);
          txtRelVal.setText("Select a relational view before validating");
        }
    }
  }

  private JPanel createJPanelInformations() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.setBackground(Color.WHITE);

    return panel;
  }
 
 @Override
  public void updateComponentInformations(UpdateMessage msg) {
    PanelClassDiagram panel = PanelClassDiagram.getInstance();
    if (panel == null) {
      west.setVisible(false);
      return;
    }
    
    ClassDiagram classDiagram = panel.getClassDiagram();
    if (classDiagram == null) return;
    
    west.setVisible(true);
    
    raiseEvent = false;
    cbbEntityView.setSelectedItem(classDiagram.getDefaultViewEntities());
    cbbParametersView.setSelectedItem(classDiagram.getDefaultViewMethods());
    chkDisplayTypes.setSelected(classDiagram.getDefaultVisibleTypes());
    chkViewEnum.setSelected(classDiagram.getDefaultViewEnum());
    raiseEvent = true;
  }
}
