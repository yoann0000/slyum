package swing.propretiesView;

import classDiagram.IDiagramComponent;
import classDiagram.components.*;
import classDiagram.verifyName.TypeName;
import swing.Slyum;
import swing.slyumCustomizedComponents.FlatPanel;
import swing.slyumCustomizedComponents.SButton;
import swing.slyumCustomizedComponents.STable;
import swing.slyumCustomizedComponents.TextFieldWithPrompt;
import utility.PersonalizedIcon;
import utility.Utility;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class RelationalEntityProperties extends GlobalPropreties{

    private static RelationalEntityProperties instance = new RelationalEntityProperties();

    public static RelationalEntityProperties getInstance() {
        return instance;
    }

    private class AttributeTableModel extends AbstractTableModel implements Observer, TableModelListener, MouseListener {
        private final String[] columnNames = { "Attribute", "Type", "Unique", "Not Null" };

        private final LinkedList<Object[]> data = new LinkedList<>();

        private final HashMap<RelationalAttribute, Integer> mapIndex = new HashMap<>();

        public void addAttribute(RelationalAttribute attribute) {
            data.add(new Object[] { attribute.getName(),
                    attribute.getType().getName(),
                    attribute.isUnique(),
                    attribute.isNotNull()
            });

            attribute.addObserver(this);
            mapIndex.put(attribute, data.size() - 1);

            fireTableRowsInserted(0, data.size());
        }

        public void clearAll() {
            data.clear();
            mapIndex.clear();
            fireTableDataChanged();
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @SuppressWarnings("unchecked")
        public HashMap<RelationalAttribute, Integer> getMapIndex() {
            return (HashMap<RelationalAttribute, Integer>) mapIndex.clone();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data.get(row)[col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return !(currentObject.getClass() == InterfaceEntity.class && col == 4);
        }

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            if (currentObject == null || !(currentObject instanceof SimpleEntity))
                return;

            // Get the selected attribute
            final int index = attributesTable.getSelectionModel().getLeadSelectionIndex();
            final RelationalAttribute attribute = Utility.getKeysByValue(mapIndex, index).iterator().next();

            // Unselect all attributes
            for (final RelationalAttribute a : ((RelationalEntity) currentObject).getAttributes()) {
                if (a.equals(attribute)) continue;

                a.select();
                a.notifyObservers(IDiagramComponent.UpdateMessage.UNSELECT);
            }

            // Select the selected attribute
            attribute.select();
            attribute.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
        }

        @Override
        public void mouseReleased(MouseEvent e) {}

        public void setAttribute(RelationalAttribute attribute, int index) {
            data.set(index, new Object[] { attribute.getName(),
                    attribute.getType().getName(),
                    attribute.isUnique(),
                    attribute.isNotNull() });

            fireTableRowsUpdated(index, index);
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            try {
                data.get(row)[col] = value;
                fireTableCellUpdated(row, col);
            } catch (Exception ignored) {

            }
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            final int row = e.getFirstRow();
            final int column = e.getColumn();

            if (column == -1) return;

            final TableModel model = (TableModel) e.getSource();
            final Object data = model.getValueAt(row, column);
            final RelationalAttribute attribute = Utility.getKeysByValue(mapIndex, row).iterator().next();

            switch (column) {
                case 0: // nom

                    if (attribute.setName((String) data))
                        setValueAt(attribute.getName(), row, column);

                    break;

                case 1: // type
                    String s = (String) data;

                    if (!TypeName.getInstance().verifyName(s))
                        setValueAt(attribute.getType().getName(), row, column);
                    else
                        attribute.setType(new Type(s));

                    break;

                case 2: // unique
                    attribute.setUnique((Boolean) data);
                    break;

                case 3: // not null
                    attribute.setNotNull((Boolean) data);
                    break;
            }

            attribute.notifyObservers();
            attribute.getType().notifyObservers();

            attributesTable.addRowSelectionInterval(row, row);
        }

        @Override
        public void update(Observable observable, Object o) {
            final RelationalAttribute attribute = (RelationalAttribute) observable;
            try {
                final int index = mapIndex.get(attribute);

                if (index == -1) return;

                if (o instanceof IDiagramComponent.UpdateMessage)
                    switch ((IDiagramComponent.UpdateMessage) o) {
                        case SELECT:
                            btnRemoveAttribute.setEnabled(true);
                            btnUpAttribute.setEnabled(index > 0);
                            btnDownAttribute.setEnabled(index < mapIndex.size() - 1);
                            showInProperties();
                            attributesTable.addRowSelectionInterval(index, index);
                            attributesTable.scrollRectToVisible(attributesTable.getCellRect(
                                    attributesTable.getSelectedRow(),
                                    attributesTable.getSelectedColumn(), true));
                            break;
                        case UNSELECT:
                            attributesTable.removeRowSelectionInterval(index, index);
                            break;
                        default:
                            break;
                    }

                setAttribute(attribute, index);
            } catch (final Exception ignored) {

            }
        }
    }

    private final STable attributesTable;
    private final JButton btnRemoveAttribute, btnUpAttribute, btnDownAttribute;
    private final JTextField textName = new TextFieldWithPrompt("", "Enter the entity's name");

    public RelationalEntityProperties() {
        // Buttons for attributes.
        btnUpAttribute = new SButton(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "arrow-up-24.png"), "Up");
        btnDownAttribute = new SButton(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "arrow-down-24.png"), "Down");
        btnRemoveAttribute = new SButton(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "minus.png"), "Remove");

        attributesTable = new STable(new RelationalEntityProperties.AttributeTableModel(), ()
                -> addAttribute(false));
        attributesTable.setEmptyText("No attribute");
        attributesTable.setPreferredScrollableViewportSize(new Dimension(200, 0));

        attributesTable.getModel().addTableModelListener(
                (RelationalEntityProperties.AttributeTableModel) attributesTable.getModel()
        );

        attributesTable.addMouseListener(
                (RelationalEntityProperties.AttributeTableModel) attributesTable.getModel()
        );

        TableColumn column;
        for (int i = 0; i < attributesTable.getColumnCount(); i++) {
            column = attributesTable.getColumnModel().getColumn(i);

            if (i < 2)
                column.setPreferredWidth(60);
            else
                column.setPreferredWidth(10);
        }

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
            p.add(createRelationalEntityProperties(), gbc_btnNewButton);
        }

        add(p);

        p = new FlatPanel();
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setLayout(new BorderLayout());
        JPanel panel = createWhitePanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        panel.add(attributesTable.getScrollPane());

        JPanel panelButton = new JPanel();
        panelButton.setOpaque(false);
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.PAGE_AXIS));

        {
            final JButton button = new SButton(
                    PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "plus.png"),
                    "Add");
            button.setAlignmentX(CENTER_ALIGNMENT);
            button.addActionListener(arg0 -> addAttribute(true));

            panelButton.add(button);
        }

        {
            btnUpAttribute.setAlignmentX(CENTER_ALIGNMENT);
            btnUpAttribute.setEnabled(false);
            btnUpAttribute.addActionListener(arg0 -> {
                // Get the selected attribute
                final int index = attributesTable.getSelectionModel().getLeadSelectionIndex();
                final RelationalAttribute attribute = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.AttributeTableModel) attributesTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).moveAttributePosition(attribute, -1);
                ((RelationalEntity) currentObject).notifyObservers();
                attribute.select();
                attribute.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            });

            panelButton.add(btnUpAttribute);
        }
        {
            btnDownAttribute.setAlignmentX(CENTER_ALIGNMENT);
            btnDownAttribute.setEnabled(false);
            btnDownAttribute.addActionListener(arg0 -> {
                // Get the selected attribute
                final int index = attributesTable.getSelectionModel()
                        .getLeadSelectionIndex();
                final RelationalAttribute attribute = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.AttributeTableModel) attributesTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).moveAttributePosition(attribute, 1);
                ((RelationalEntity) currentObject).notifyObservers();
                attribute.select();
                attribute.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            });

            panelButton.add(btnDownAttribute);
        }

        {
            btnRemoveAttribute.setAlignmentX(CENTER_ALIGNMENT);
            btnRemoveAttribute.setEnabled(false);
            btnRemoveAttribute.addActionListener(arg0 -> {
                // Get the selected attribute
                final int index = attributesTable.getSelectionModel()
                        .getLeadSelectionIndex();
                RelationalAttribute attribute = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.AttributeTableModel) attributesTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).removeAttribute(attribute);
                ((RelationalEntity) currentObject).notifyObservers();

                for (int i = 0; i <= 1; i++) {
                    try {
                        attribute = Utility
                                .getKeysByValue(
                                        ((RelationalEntityProperties.AttributeTableModel) attributesTable.getModel())
                                                .getMapIndex(), index - i).iterator()
                                .next();
                    } catch (final NoSuchElementException e) {
                        continue;
                    }

                    attribute.select();
                    attribute.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
                    break;
                }
            });

            panelButton.add(btnRemoveAttribute);

        }

        p.add(panel, BorderLayout.CENTER);
        p.add(panelButton, BorderLayout.EAST);
        add(p);
    }

    public JPanel createRelationalEntityProperties() {
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
                    final RelationalEntity relationalEntity = (RelationalEntity) currentObject;

                    if (!relationalEntity.setName(textName.getText()))
                        textName.setText(relationalEntity.getName());
                    else
                        relationalEntity.notifyObservers();
                }
            }
        });
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

    private void addAttribute(boolean editRequest) {
        RelationalEntity entity = (RelationalEntity)currentObject;

        entity.addAttribute(new RelationalAttribute("attribute", PrimitiveType.VOID_TYPE));

        if (editRequest)
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_ATTRIBUTE);
        else
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_ATTRIBUTE_NO_EDIT);
    }

    private void stopEditingTables() {
        TableCellEditor a = attributesTable.getCellEditor();

        if (a != null) a.stopCellEditing();
    }

    @Override
    public void updateComponentInformations(IDiagramComponent.UpdateMessage msg) {
        if (currentObject == null) return;

        stopEditingTables();
        final RelationalEntity relationalEntity = (RelationalEntity) currentObject;
        final RelationalEntityProperties.AttributeTableModel modelAttributes =
                (RelationalEntityProperties.AttributeTableModel)attributesTable.getModel();

        final LinkedList<RelationalAttribute> attributes = relationalEntity.getAttributes();

        if (msg != null && msg.equals(IDiagramComponent.UpdateMessage.UNSELECT))
            if (!relationalEntity.getName().equals(textName.getText()))
                if (!relationalEntity.setName(textName.getText()))
                    textName.setText(relationalEntity.getName());
                else
                    relationalEntity.notifyObservers();

        textName.setText(relationalEntity.getName());

        modelAttributes.clearAll();

        for (RelationalAttribute attribute : attributes)
            modelAttributes.addAttribute(attribute);

        btnRemoveAttribute.setEnabled(false);
        btnUpAttribute.setEnabled(false);
        btnDownAttribute.setEnabled(false);

        validate();

        if (msg == IDiagramComponent.UpdateMessage.ADD_ATTRIBUTE
                || msg == IDiagramComponent.UpdateMessage.ADD_ATTRIBUTE_NO_EDIT)
            attributesTable.scrollRectToVisible(attributesTable.getCellRect(
                    attributesTable.getRowCount(), attributesTable.getColumnCount(),
                    true));

    }

    @Override
    public Component add(Component comp) {
        Component c = super.add(comp);
        super.add(Box.createHorizontalStrut(5));
        return c;
    }
}
