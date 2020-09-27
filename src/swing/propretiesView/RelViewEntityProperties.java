package swing.propretiesView;

import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.RelViewEntity;
import swing.slyumCustomizedComponents.FlatPanel;
import swing.slyumCustomizedComponents.TextFieldWithPrompt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RelViewEntityProperties extends GlobalPropreties {

    private static final RelViewEntityProperties instance = new RelViewEntityProperties();

    /**
     * Get the unique instance of this View.
     *
     * @return the unique instance of RelViewEntityProperties
     */
    public static RelViewEntityProperties getInstance() {
        return instance;
    }

    private final JTextField textName = new TextFieldWithPrompt("", "Enter the entity's name");
    private final JTextArea procedure = new JTextArea();

    private RelViewEntityProperties() {
        JPanel p = new FlatPanel();
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setMaximumSize(new Dimension(0, Integer.MAX_VALUE));

        {
            final GridBagLayout gbl_panel = new GridBagLayout();
            gbl_panel.columnWidths = new int[] { 0, 0 };
            gbl_panel.rowHeights = new int[] { 0, 0 };
            gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
            gbl_panel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
            p.setLayout(gbl_panel);
        }

        {
            final GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
            gbc_btnNewButton.anchor = GridBagConstraints.NORTH;
            gbc_btnNewButton.gridx = 0;
            gbc_btnNewButton.gridy = 0;
            p.add(createRelViewEntityProperties(), gbc_btnNewButton);
        }

        add(p);

        p = new FlatPanel();
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setLayout(new BorderLayout());
        JPanel panel = createWhitePanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));


        procedure.setLineWrap(true);
        procedure.setWrapStyleWord(true);
        procedure.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                change.Change.setHasChange(true);
                final RelViewEntity relViewEntity = (RelViewEntity) currentObject;
                if(relViewEntity != null)
                    relViewEntity.setProcedure(procedure.getText());
            }



        });

        JScrollPane textAreaPane = new JScrollPane(procedure);
        textAreaPane.setPreferredSize(new Dimension(250, 0));
        textAreaPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textAreaPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel proc = new JLabel();

        proc.setText("View Procedure");
        panel.add(proc);
        panel.add(Box.createVerticalStrut(5));
        panel.add(textAreaPane);

        p.add(panel, BorderLayout.CENTER);
        add(p);
    }

    public JPanel createRelViewEntityProperties() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentY(TOP_ALIGNMENT);

        // Entity's name
        textName.setAlignmentX(LEFT_ALIGNMENT);
        textName.setPreferredSize(new Dimension(230, 25));
        textName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    final RelViewEntity relViewEntity = (RelViewEntity) currentObject;

                    if (!relViewEntity.setName(textName.getText()))
                        textName.setText(relViewEntity.getName());
                    else
                        relViewEntity.notifyObservers();
                }
            }
        });

        panel.add(new JLabel("Relational View Name"));

        panel.add(textName);
        panel.add(Box.createVerticalStrut(5));
        return panel;
    }

    public JPanel createWhitePanel() {
        final JPanel panel = new JPanel();
        panel.setBackground(SystemColor.control);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setAlignmentY(TOP_ALIGNMENT);
        panel.setOpaque(false);
        return panel;
    }

    @Override
    public void updateComponentInformation(UpdateMessage msg) {
        if (currentObject == null) return;

        final RelViewEntity relViewEntity = (RelViewEntity) currentObject;

        final String procedureTxt = relViewEntity.getProcedure();

        if (msg != null && msg.equals(UpdateMessage.UNSELECT))
            if (!relViewEntity.getName().equals(textName.getText()))
                if (!relViewEntity.setName(textName.getText()))
                    textName.setText(relViewEntity.getName());
                else
                    relViewEntity.notifyObservers();

        textName.setText(relViewEntity.getName());
        procedure.setText(procedureTxt);
        validate();
    }

    @Override
    public Component add(Component comp) {
        Component c = super.add(comp);
        super.add(Box.createHorizontalStrut(5));
        return c;
    }
}
