package swing.propretiesView;

import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.Entity;
import classDiagram.relationships.*;
import classDiagram.relationships.Association.NavigateDirection;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import swing.MultiViewManager;
import swing.slyumCustomizedComponents.FlatPanel;
import swing.slyumCustomizedComponents.SRadioButton;
import swing.slyumCustomizedComponents.TextFieldWithPrompt;

/**
 * Show the propreties of an association and its roles with Swing components.
 *
 * @author David Miserez
 * @version 1.0 - 28.07.2011
 */
public class RelationalRelationProperties extends GlobalPropreties {
    private static final RelationalRelationProperties instance = new RelationalRelationProperties();

    /**
     * Get the unique instance of this class.
     *
     * @return the unique instance of RelationPropreties
     */
    public static RelationalRelationProperties getInstance() {
        return instance;
    }

    private final ButtonGroup btnGrpNavigation;
    private final JRadioButton radFirstToSecond, radSecondToFirst;
    private final JPanel pnlRoles;
    private final JTextField textFieldLabel;

    /**
     * Create the panel.
     */
    public RelationalRelationProperties() {
        JPanel pnlGeneral = new FlatPanel();

        // Initialization
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        // Panel général
        textFieldLabel = new TextFieldWithPrompt("", "Enter the relation's name");
        textFieldLabel.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
        textFieldLabel.addActionListener(e -> {
            if (currentObject != null) if (currentObject instanceof Association) {
                ((Association) currentObject).setLabel(textFieldLabel.getText());
                ((Association) currentObject).notifyObservers();
            } else if (currentObject instanceof Dependency) {
                ((Dependency) currentObject).setLabel(textFieldLabel.getText());
                ((Dependency) currentObject).notifyObservers();
            }
        });

        radFirstToSecond = new SRadioButton();
        radFirstToSecond.setBackground(null);
        radFirstToSecond.addActionListener(evt -> setNavDir(NavigateDirection.FIRST_TO_SECOND));

        radSecondToFirst = new SRadioButton();
        radSecondToFirst.setBackground(null);
        radSecondToFirst.addActionListener(evt -> setNavDir(NavigateDirection.SECOND_TO_FIRST));

        btnGrpNavigation = new ButtonGroup();
        btnGrpNavigation.add(radFirstToSecond);
        btnGrpNavigation.add(radSecondToFirst);

        pnlGeneral.setLayout(new BoxLayout(pnlGeneral, BoxLayout.PAGE_AXIS));
        pnlGeneral.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));
        pnlGeneral.add(textFieldLabel);
        pnlGeneral.add(Box.createVerticalGlue());
        pnlGeneral.add(radFirstToSecond);
        pnlGeneral.add(radSecondToFirst);
        pnlGeneral.add(Box.createVerticalGlue());

        // Panel roles & ScrollPane
        pnlRoles = new JPanel();
        pnlRoles.setLayout(new BoxLayout(pnlRoles, BoxLayout.LINE_AXIS));
        pnlRoles.setBackground(null);
        pnlRoles.setBorder(null);

        add(pnlGeneral);
        add(Box.createHorizontalStrut(5));
        add(pnlRoles);
    }

    private void setCurrentObjectDirected(NavigateDirection direction) {
        if (currentObject != null && currentObject instanceof Association) {
            ((Association) currentObject).setDirected(direction);
        }
    }

    @Override
    public void updateComponentInformation(UpdateMessage msg) {
        if (currentObject != null) {
            if (currentObject instanceof RelAssociation) {
                final RelAssociation association = (RelAssociation) currentObject;

                if (msg != null && msg.equals(UpdateMessage.UNSELECT)) {
                    association.setName(textFieldLabel.getText());
                    association.notifyObservers();

                    for (final Component c : pnlRoles.getComponents())
                        if (c instanceof RelationalFlatPanel) ((RelationalFlatPanel) c).confirm();
                }

                switch (association.getDirected()) {
                    case FIRST_TO_SECOND:
                        btnGrpNavigation.setSelected(radFirstToSecond.getModel(), true);
                        break;
                    case SECOND_TO_FIRST:
                        btnGrpNavigation.setSelected(radSecondToFirst.getModel(), true);
                        break;
                    default:
                        break;
                }
                setMenuItemText();

                textFieldLabel.setText(association.getLabel());

                if (pnlRoles.getComponentCount() == 0 || msg == UpdateMessage.SELECT) {
                    for (final Component c : pnlRoles.getComponents()) {
                        if (c instanceof RelationalFlatPanel)
                            ((RelationalFlatPanel) c).stopObserving();
                        pnlRoles.removeAll();
                    }

                    for (final Role role : association.getRoles()) {
                        pnlRoles.add(new RelationalFlatPanel(role));
                        pnlRoles.add(Box.createHorizontalStrut(5));
                    }
                }
            } else if (currentObject instanceof Dependency) {
                for (final Component c : pnlRoles.getComponents()) {
                    if (c instanceof RelationalFlatPanel)
                        ((RelationalFlatPanel) c).stopObserving();
                    pnlRoles.removeAll();
                }

                Dependency dependency = (Dependency) currentObject;

                if (msg != null && msg.equals(UpdateMessage.UNSELECT)) {
                    dependency.setLabel(textFieldLabel.getText());
                    dependency.notifyObservers();
                }

                textFieldLabel.setText(dependency.getLabel());
            }
            setVisibleNavigationBtn(currentObject instanceof Binary);
        }
    }

    private void setMenuItemText() {
        if (currentObject != null && currentObject instanceof RelAssociation) {
            Entity source = ((RelAssociation) currentObject).getSource(),
                    target = ((RelAssociation) currentObject).getTarget();
            if (source != null && target != null) {
                String sourceName = source.getName(),
                        targetName = target.getName();
                radFirstToSecond.setText(String
                        .format("%s -> %s", sourceName, targetName));
                radSecondToFirst.setText(String
                        .format("%s -> %s", targetName, sourceName));
            }
        }
    }

    private void setVisibleNavigationBtn(boolean visible) {
        radFirstToSecond.setVisible(visible);
        radSecondToFirst.setVisible(visible);
    }

    private void setNavDir(NavigateDirection navDir) {
        setCurrentObjectDirected(navDir);
        ((RelAssociation)currentObject).resetKeys();
    }
}
