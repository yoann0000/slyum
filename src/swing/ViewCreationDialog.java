package swing;

import swing.slyumCustomizedComponents.TextFieldWithPrompt;
import utility.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ViewCreationDialog extends JDialog{
    private static final long serialVersionUID = 1182377279614623151L;
    private boolean accepted = false;
    private boolean isRel = false;
    private final JPanel contentPanel = new JPanel();
    private JTextField textField;
    private JRadioButton uml;
    private JRadioButton rel;


    /**
     * Create the dialog.
     *
     * @param defaultText TODO
     * @param title       TODO
     * @param invite      TODO
     */
    public ViewCreationDialog(String defaultText, String title, String invite) {
        super(Slyum.getInstance(), title, ModalityType.APPLICATION_MODAL);
        setResizable(true);
        setBounds(100, 100, 513, 120);
        Utility.setRootPaneActionOnEsc(getRootPane(), new AbstractAction() {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        {
            final JLabel lblEditCommentary = new JLabel(invite);
            contentPanel.add(lblEditCommentary);
        }
        {
            textField = new TextFieldWithPrompt(defaultText);
            contentPanel.add(textField);
            textField.setColumns(10);
        }
        {
            final JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            uml = new JRadioButton("UML");
            rel = new JRadioButton("REL");
            uml.setSelected(true);
            ButtonGroup bg = new ButtonGroup();
            bg.add(uml);
            bg.add(rel);
            buttonPane.add(uml);
            buttonPane.add(rel);
            {
                final JButton okButton = new JButton("OK");
                getRootPane().setDefaultButton(okButton);
                okButton.addActionListener(arg0 -> {
                    accepted = true;
                    if (rel.isSelected()){
                        isRel = true;
                    }
                    setVisible(false);
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                final JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(e -> setVisible(false));
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }

        setLocationRelativeTo(PanelClassDiagram.getInstance());
    }

    public String getText() {
        return textField.getText();
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isRel() {
        return isRel;
    }
}
