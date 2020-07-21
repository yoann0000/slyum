package swing;

import utility.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ViewConversionDialog extends JDialog{
    private boolean accepted = false;
    private final JPanel contentPanel = new JPanel();

    public ViewConversionDialog(String title, String invite, boolean isRel) {
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
            final JLabel label = new JLabel();
            if(isRel)
                label.setText("A UML view must be open to convert");
            else
                label.setText(invite);
            contentPanel.add(label);
        }

        {
            final JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            {
                final JButton okButton = new JButton("OK");
                getRootPane().setDefaultButton(okButton);
                okButton.addActionListener(arg0 -> {
                    accepted = true;
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

    public boolean isAccepted() {
        return accepted;
    }
}
