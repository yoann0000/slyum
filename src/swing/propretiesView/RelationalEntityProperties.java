package swing.propretiesView;

import classDiagram.IDiagramComponent;
import classDiagram.components.*;
import classDiagram.verifyName.TypeName;
import graphic.entity.ClassView;
import swing.MultiViewManager;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
            if (currentObject == null || !(currentObject instanceof RelationalEntity))
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

    //TODO
    private class PKTableModel extends AbstractTableModel implements Observer, TableModelListener, MouseListener {
        private final String[] columnNames = {"Pk Attributes"};

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
            if (currentObject == null || !(currentObject instanceof RelationalEntity))
                return;

            // Get the selected attribute
            final int index = keyAttributesTable.getSelectionModel().getLeadSelectionIndex();
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
            data.set(index, new Object[] { attribute.getName() });

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

            if (column == 0) { // nom
                if (attribute.setName((String) data))
                    setValueAt(attribute.getName(), row, column);
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
                            btnRemovePkAttribute.setEnabled(true);
                            btnUpPkAttribute.setEnabled(index > 0);
                            btnDownPkAttribute.setEnabled(index < mapIndex.size() - 1);
                            showInProperties();
                            keyAttributesTable.addRowSelectionInterval(index, index);
                            keyAttributesTable.scrollRectToVisible(keyAttributesTable.getCellRect(
                                    keyAttributesTable.getSelectedRow(),
                                    keyAttributesTable.getSelectedColumn(), true));
                            break;
                        case UNSELECT:
                            keyAttributesTable.removeRowSelectionInterval(index, index);
                            break;
                        default:
                            break;
                    }

                setAttribute(attribute, index);
            } catch (final Exception ignored) {

            }
        }
    }

    private class TriggerTableModel extends AbstractTableModel
            implements Observer, TableModelListener, MouseListener {

        private final String[] columnNames = { "Trigger", "Activation Time",
                "Trigger Type" };

        private final LinkedList<Object[]> data = new LinkedList<>();

        private final HashMap<Trigger, Integer> mapIndex = new HashMap<>();

        public void addTrigger(Trigger trigger) {
            data.add(new Object[] { trigger.getName(),
                    trigger.getActivationTime().getName(),
                    trigger.getTriggerType().getName(),
                    trigger.getProcedure() });

            trigger.addObserver(this);
            //trigger.addObserver((RelationalEntityProperties.ParametersTableModel) parametersTable.getModel());//FIXME
            mapIndex.put(trigger, data.size() - 1);

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

        @SuppressWarnings({ "unchecked" })
        public HashMap<Trigger, Integer> getMapIndex() {
            return (HashMap<Trigger, Integer>) mapIndex.clone();
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
            Boolean isInterfaceEntityClass = currentObject.getClass()
                    .equals(InterfaceEntity.class);
            boolean isConstructorClass = Utility.getKeysByValue(mapIndex, row)
                    .iterator().next().getClass()
                    .equals(ConstructorMethod.class);

            return !(((isInterfaceEntityClass || isConstructorClass) && col == 3) ||
                    isConstructorClass && col == 4 ||
                    isConstructorClass && col == 1);
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

            // Get the selected trigger
            final int index = triggerTable.getSelectionModel()
                    .getLeadSelectionIndex();
            final Trigger trigger = Utility.getKeysByValue(mapIndex, index).iterator()
                    .next();

            // Unselect all triggers
            for (final Trigger t : ((RelationalEntity) currentObject).getTriggers()) {
                if (t.equals(trigger)) continue;

                t.select();
                t.notifyObservers(IDiagramComponent.UpdateMessage.UNSELECT);
            }

            // Select the selected method
            trigger.select();
            trigger.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
        }

        @Override
        public void mouseReleased(MouseEvent e) {}

        public void setTrigger(Trigger trigger, int index) {
            data.set(index,
                    new Object[] { trigger.getName(),
                            trigger.getActivationTime().getName(),
                            trigger.getTriggerType().getName(),
                            trigger.getProcedure() });
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
            final Trigger trigger = Utility.getKeysByValue(mapIndex, row).iterator().next();

            switch (column) {
                case 0: // nom
                    if (trigger.setName((String) data))
                        setValueAt(trigger.getName(), row, column);
                    break;
                case 1: // activation time
                    trigger.setActivationTime(ActivationTime.getFromName((String) data));
                    break;
                case 2: // trigger type
                    trigger.setTriggerType(TriggerType.getFromName((String) data));
                    break;
            }

            trigger.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);

            triggerTable.addRowSelectionInterval(row, row);
        }

        @Override
        public void update(Observable observable, Object o) {
            try {
                final int index = mapIndex.get(observable);

                if (index == -1) return;

                if (o instanceof IDiagramComponent.UpdateMessage)
                    switch ((IDiagramComponent.UpdateMessage) o) {
                        case SELECT:
                            btnRemoveTrigger.setEnabled(true);
                            btnUpTrigger.setEnabled(index > 0);
                            btnDownTrigger.setEnabled(index < mapIndex.size() - 1);
                            showInProperties();
                            triggerTable.addRowSelectionInterval(index, index);
                            triggerTable.scrollRectToVisible(triggerTable.getCellRect(
                                    triggerTable.getSelectedRow(),
                                    triggerTable.getSelectedColumn(), true));
                            break;
                        case UNSELECT:
                            triggerTable.removeRowSelectionInterval(index, index);
                            break;
                        default:
                            break;
                    }

                setTrigger((Trigger) observable, index);
            } catch (final Exception ignored) {

            }
        }

    }

    private final STable attributesTable, keyAttributesTable, triggerTable;
    private final JButton btnRemoveAttribute, btnUpAttribute, btnDownAttribute,
            btnUpPkAttribute, btnDownPkAttribute, btnRemovePkAttribute,
            btnUpTrigger, btnDownTrigger, btnRemoveTrigger;
    private final JTextField textName = new TextFieldWithPrompt("", "Enter the entity's name"),
                               pkName = new TextFieldWithPrompt("","Enter the Primary key's name");
    private final JLabel pk = new JLabel();

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

        // Buttons for pk attributes.
        btnUpPkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "arrow-up-24.png"), "Up");
        btnDownPkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "arrow-down-24.png"), "Down");
        btnRemovePkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "minus.png"), "Remove");

        btnUpTrigger = new SButton(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "arrow-up-24.png"), "Up");
        btnDownTrigger = new SButton(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "arrow-down-24.png"), "Down");
        btnRemoveTrigger = new SButton(
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

        keyAttributesTable = new STable(new PKTableModel(), () -> addAttribute(false));
        keyAttributesTable.setEmptyText("No key attribute");
        keyAttributesTable.setPreferredScrollableViewportSize(new Dimension(70, 0));

        keyAttributesTable.getModel().addTableModelListener(
                (RelationalEntityProperties.PKTableModel) keyAttributesTable.getModel());

        keyAttributesTable.addMouseListener((RelationalEntityProperties.PKTableModel) keyAttributesTable
                .getModel());

        triggerTable = new STable(new TriggerTableModel(), () -> addTrigger(false));
        triggerTable.setEmptyText("No trigger");
        triggerTable.setPreferredScrollableViewportSize(new Dimension(200, 0));
        triggerTable.getModel().addTableModelListener(
                (RelationalEntityProperties.TriggerTableModel) triggerTable.getModel());
        triggerTable.addMouseListener((RelationalEntityProperties.TriggerTableModel) triggerTable.getModel());

        TableColumn comboBoxColumn = triggerTable.getColumnModel().getColumn(1);
        comboBoxColumn.setCellEditor(new DefaultCellEditor(Utility.getActivationTimeComboBox()));

        comboBoxColumn = triggerTable.getColumnModel().getColumn(2);
        comboBoxColumn.setCellEditor(new DefaultCellEditor(Utility.getTriggerTypeComboBox()));

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
        panel.add(keyAttributesTable.getScrollPane());

        JPanel panelButton = new JPanel();
        panelButton.setOpaque(false);
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.PAGE_AXIS));

        {
            final JButton button = new SButton(
                    PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "plus.png"),
                    "Add");
            button.setAlignmentX(CENTER_ALIGNMENT);
            button.addActionListener(arg0 -> addPkAttribute(true));

            panelButton.add(button);
        }

        {
            btnUpPkAttribute.setAlignmentX(CENTER_ALIGNMENT);
            btnUpPkAttribute.setEnabled(false);
            btnUpPkAttribute.addActionListener(arg0 -> {
                // Get the selected attribute
                final int index = keyAttributesTable.getSelectionModel().getLeadSelectionIndex();
                final RelationalAttribute attribute = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.PKTableModel) keyAttributesTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).getPrimaryKey().moveAttributePosition(attribute, -1);
                ((RelationalEntity) currentObject).getPrimaryKey().notifyObservers();
                attribute.select();
                attribute.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            });

            panelButton.add(btnUpPkAttribute);
        }
        {
            btnDownPkAttribute.setAlignmentX(CENTER_ALIGNMENT);
            btnDownPkAttribute.setEnabled(false);
            btnDownPkAttribute.addActionListener(arg0 -> {
                // Get the selected attribute
                final int index = keyAttributesTable.getSelectionModel()
                        .getLeadSelectionIndex();
                final RelationalAttribute attribute = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.PKTableModel) keyAttributesTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).getPrimaryKey().moveAttributePosition(attribute, 1);
                ((RelationalEntity) currentObject).getPrimaryKey().notifyObservers();
                attribute.select();
                attribute.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            });

            panelButton.add(btnDownPkAttribute);
        }

        {
            btnRemovePkAttribute.setAlignmentX(CENTER_ALIGNMENT);
            btnRemovePkAttribute.setEnabled(false);
            btnRemovePkAttribute.addActionListener(arg0 -> {
                // Get the selected attribute
                final int index = keyAttributesTable.getSelectionModel()
                        .getLeadSelectionIndex();
                RelationalAttribute attribute = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.PKTableModel) keyAttributesTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).getPrimaryKey().removeKeyComponent(attribute);
                ((RelationalEntity) currentObject).getPrimaryKey().notifyObservers();

                for (int i = 0; i <= 1; i++) {
                    try {
                        attribute = Utility
                                .getKeysByValue(
                                        ((RelationalEntityProperties.PKTableModel) keyAttributesTable.getModel())
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

            panelButton.add(btnRemovePkAttribute);

        }

        p.add(panel, BorderLayout.CENTER);
        p.add(panelButton, BorderLayout.EAST);
        add(p);


        p = new FlatPanel();
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setLayout(new BorderLayout());
        panel = createWhitePanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        panel.add(attributesTable.getScrollPane());

        panelButton = new JPanel();
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

        p = new FlatPanel();
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setLayout(new BorderLayout());
        panel = createWhitePanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        panel.add(triggerTable.getScrollPane());

        panelButton = new JPanel();
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.PAGE_AXIS));
        panelButton.setOpaque(false);

        {
            final JButton button = new SButton(
                    PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "plus.png"),
                    "Add");
            button.setAlignmentX(CENTER_ALIGNMENT);
            button.addActionListener(arg0 -> addTrigger(true));

            panelButton.add(button);
        }
        {
            btnUpTrigger.setAlignmentX(CENTER_ALIGNMENT);
            btnUpTrigger.setEnabled(false);
            btnUpTrigger.addActionListener(evt -> {

                // Get the selected method
                final int index = triggerTable.getSelectionModel()
                        .getLeadSelectionIndex();
                final Trigger trigger = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.TriggerTableModel) triggerTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).moveTriggerPosition(trigger, -1);
                ((RelationalEntity) currentObject).notifyObservers();
                trigger.select();
                trigger.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            });

            panelButton.add(btnUpTrigger);
        }
        {
            btnDownTrigger.setAlignmentX(CENTER_ALIGNMENT);
            btnDownTrigger.setEnabled(false);
            btnDownTrigger.addActionListener(evt -> {

                // Get the selected method
                final int index = triggerTable.getSelectionModel()
                        .getLeadSelectionIndex();
                final Trigger trigger = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.TriggerTableModel) triggerTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).moveTriggerPosition(trigger, 1);
                ((RelationalEntity) currentObject).notifyObservers();
                trigger.select();
                trigger.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            });

            panelButton.add(btnDownTrigger);
        }

        {
            btnRemoveTrigger.setAlignmentX(CENTER_ALIGNMENT);
            btnRemoveTrigger.setEnabled(false);
            btnRemoveTrigger.addActionListener(arg0 -> {
                // Get the selected method
                final int index = triggerTable.getSelectionModel()
                        .getLeadSelectionIndex();
                Trigger trigger = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.TriggerTableModel) triggerTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).removeTrigger(trigger);
                ((RelationalEntity) currentObject).notifyObservers();

                for (int i = 0; i <= 1; i++) {
                    try {
                        trigger = Utility
                                .getKeysByValue(
                                        ((RelationalEntityProperties.TriggerTableModel) triggerTable.getModel())
                                                .getMapIndex(), index - i).iterator()
                                .next();
                    } catch (final NoSuchElementException e) {
                        continue;
                    }

                    trigger.select();
                    trigger.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
                    break;
                }
            });

            panelButton.add(btnRemoveTrigger);
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
        pk.setText("Primary Key Name");
        panel.add(pk);

        pkName.setAlignmentX(LEFT_ALIGNMENT);
        pkName.setPreferredSize(new Dimension(230, 25));
        pkName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    final RelationalEntity relationalEntity = (RelationalEntity) currentObject;

                    if (!relationalEntity.getPrimaryKey().setName(pkName.getText()))
                        pkName.setText(relationalEntity.getPrimaryKey().getName());
                    else
                        relationalEntity.notifyObservers();
                }
            }
        });

        panel.add(pkName);
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

    private void addPkAttribute(boolean editRequest) {
        RelationalEntity entity = (RelationalEntity)currentObject;

        entity.getPrimaryKey().addKeyComponent(new RelationalAttribute("attribute", PrimitiveType.VOID_TYPE));

        if (editRequest)
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_PK_ATTRIBUTE);
        else
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_PK_ATTRIBUTE_NO_EDIT);
    }

    private void addTrigger(boolean editRequest) {
        RelationalEntity entity = (RelationalEntity)currentObject;

        entity.addTrigger(new Trigger("trigger"));

        if (editRequest)
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_TRIGGER);
        else
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_TRIGGER_NO_EDIT);
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
        final RelationalEntityProperties.PKTableModel modelPkAttributes =
                (RelationalEntityProperties.PKTableModel)keyAttributesTable.getModel();
        final RelationalEntityProperties.TriggerTableModel modelTriggers =
                (RelationalEntityProperties.TriggerTableModel)triggerTable.getModel();

        final LinkedList<RelationalAttribute> attributes = relationalEntity.getAttributes();
        final LinkedList<RelationalAttribute> pkAttributes = relationalEntity.getPrimaryKey().getKeyComponents();
        final LinkedList<Trigger> triggers = relationalEntity.getTriggers();

        if (msg != null && msg.equals(IDiagramComponent.UpdateMessage.UNSELECT))
            if (!relationalEntity.getName().equals(textName.getText()))
                if (!relationalEntity.setName(textName.getText()))
                    textName.setText(relationalEntity.getName());
                else
                    relationalEntity.notifyObservers();

        textName.setText(relationalEntity.getName());

        if (msg != null && msg.equals(IDiagramComponent.UpdateMessage.UNSELECT))
            if (!relationalEntity.getPrimaryKey().getName().equals(pkName.getText()))
                if (!relationalEntity.getPrimaryKey().setName(pkName.getText()))
                    pkName.setText(relationalEntity.getName());
                else
                    relationalEntity.notifyObservers();

        pkName.setText(relationalEntity.getPrimaryKey().getName());

        modelAttributes.clearAll();

        for (RelationalAttribute attribute : attributes)
            modelAttributes.addAttribute(attribute);

        modelPkAttributes.clearAll();

        for (RelationalAttribute attribute : pkAttributes)
            modelPkAttributes.addAttribute(attribute);

        modelTriggers.clearAll();

        for (Trigger trigger : triggers)
            modelTriggers.addTrigger(trigger);

        btnRemoveAttribute.setEnabled(false);
        btnUpAttribute.setEnabled(false);
        btnDownAttribute.setEnabled(false);

        validate();

        if (msg == IDiagramComponent.UpdateMessage.ADD_ATTRIBUTE
                || msg == IDiagramComponent.UpdateMessage.ADD_ATTRIBUTE_NO_EDIT)
            attributesTable.scrollRectToVisible(attributesTable.getCellRect(
                    attributesTable.getRowCount(), attributesTable.getColumnCount(),
                    true));

        if (msg == IDiagramComponent.UpdateMessage.ADD_PK_ATTRIBUTE
                || msg == IDiagramComponent.UpdateMessage.ADD_PK_ATTRIBUTE_NO_EDIT)
            keyAttributesTable.scrollRectToVisible(keyAttributesTable.getCellRect(
                    keyAttributesTable.getRowCount(), keyAttributesTable.getColumnCount(),
                    true));

        if (msg == IDiagramComponent.UpdateMessage.ADD_TRIGGER
                || msg == IDiagramComponent.UpdateMessage.ADD_TRIGGER_NO_EDIT)
            triggerTable.scrollRectToVisible(triggerTable.getCellRect(
                    triggerTable.getRowCount(), triggerTable.getColumnCount(),
                    true));

    }

    @Override
    public Component add(Component comp) {
        Component c = super.add(comp);
        super.add(Box.createHorizontalStrut(5));
        return c;
    }
}
