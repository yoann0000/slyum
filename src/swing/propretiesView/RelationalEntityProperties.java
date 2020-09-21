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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

public class RelationalEntityProperties extends GlobalPropreties{

    private static final RelationalEntityProperties instance = new RelationalEntityProperties();

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
                            btnAddAttributeToPk.setEnabled(true);
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

    //FIXME
    private class PKTableModel extends AbstractTableModel implements Observer, TableModelListener, MouseListener {
        private final String[] columnNames = {"Pk Attributes"};

        private final LinkedList<Object[]> data = new LinkedList<>();

        private final HashMap<RelationalAttribute, Integer> mapIndex = new HashMap<>();

        public void addAttribute(RelationalAttribute attribute) {
            data.add(new Object[] { attribute.getName()});

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

            keyAttributesTable.addRowSelectionInterval(row, row);
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
            trigger.addObserver(triggerProcedure);
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
        public boolean isCellEditable(int row, int col) { //FIXME change this
            boolean isInterfaceEntityClass = currentObject.getClass()
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
            if (currentObject == null || !(currentObject instanceof RelationalEntity))
                return;

            // Get the selected trigger
            final int index = triggerTable.getSelectionModel()
                    .getLeadSelectionIndex();
            final Trigger trigger = Utility.getKeysByValue(mapIndex, index).iterator().next();

            // Unselect all triggers
            for (final Trigger t : ((RelationalEntity) currentObject).getTriggers()) {
                if (t.equals(trigger)) continue;

                t.select();
                t.notifyObservers(IDiagramComponent.UpdateMessage.UNSELECT);
            }

            // Select the selected trigger
            trigger.select();
            triggerProcedure.setText(trigger.getProcedure());
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

    private static class TriggerProcedure extends JTextArea implements Observer {

        private Trigger currentTrigger;

        public TriggerProcedure() {
            super();
            this.setEditable(false);
            this.setText("Select a Trigger to Edit");
        }

        public Trigger getCurrentTrigger() {
            return currentTrigger;
        }

        @Override
        public void update(Observable observable, Object o) {
            if (o instanceof IDiagramComponent.UpdateMessage) {
                switch ((IDiagramComponent.UpdateMessage) o) {
                    case SELECT:
                        currentTrigger = (Trigger) observable;
                        this.setText(currentTrigger.getProcedure());
                        this.setEditable(true);
                        break;
                    case UNSELECT:
                        currentTrigger = null;
                        this.setText("Select a Trigger to Edit");
                        this.setEditable(false);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /*private class AlternateKeyTableModel extends AbstractTableModel
            implements Observer, TableModelListener, MouseListener {

        private final String[] columnNames = {"name"};

        private final LinkedList<Object[]> data = new LinkedList<>();

        private final HashMap<Key, Integer> mapIndex = new HashMap<>();

        public void addAk(Key key) {
            data.add(new Object[] { key.getName() });

            key.addObserver(this);
            key.addObserver((RelationalEntityProperties.AkComponentTableModel) akComponentsTable.getModel());
            mapIndex.put(key, data.size() - 1);

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
        public HashMap<Key, Integer> getMapIndex() {
            return (HashMap<Key, Integer>) mapIndex.clone();
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
            return true;
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

            // Get the selected key
            final int index = alternateKeyTable.getSelectionModel()
                    .getLeadSelectionIndex();
            final Key key = Utility.getKeysByValue(mapIndex, index).iterator()
                    .next();

            // Unselect all keys
            for (final Key k : ((RelationalEntity) currentObject).getAlternateKeys()) {
                if (k.equals(key)) continue;

                k.select();
                k.notifyObservers(IDiagramComponent.UpdateMessage.UNSELECT);
            }

            // Select the selected key
            key.select();
            key.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
        }

        @Override
        public void mouseReleased(MouseEvent e) {}

        public void setKey(Key key, int index) {
            data.set(index,
                    new Object[] { key.getName() });

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
            final Key key = Utility.getKeysByValue(mapIndex, row).iterator()
                    .next();

            if (column == 0) { // nom
                if (key.setName((String) data))
                    setValueAt(key.getName(), row, column);
            }

            key.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            //key.getReturnType().notifyObservers();

            alternateKeyTable.addRowSelectionInterval(row, row);
        }

        @Override
        public void update(Observable observable, Object o) {
            try {
                final int index = mapIndex.get(observable);

                if (index == -1) return;

                if (o instanceof IDiagramComponent.UpdateMessage)
                    switch ((IDiagramComponent.UpdateMessage) o) {
                        case SELECT:
                            btnRemoveAk.setEnabled(true);
                            btnUpAk.setEnabled(index > 0);
                            btnDownAk.setEnabled(index < mapIndex.size() - 1);
                            showInProperties();
                            alternateKeyTable.addRowSelectionInterval(index, index);
                            alternateKeyTable.scrollRectToVisible(alternateKeyTable.getCellRect(
                                    alternateKeyTable.getSelectedRow(),
                                    alternateKeyTable.getSelectedColumn(), true));
                            break;
                        case UNSELECT:
                            alternateKeyTable.removeRowSelectionInterval(index, index);
                            break;
                        default:
                            break;
                    }

                setKey((Key) observable, index);
            } catch (final Exception ignored) {

            }
        }

    }

    private class AkComponentTableModel
            extends AbstractTableModel
            implements Observer, TableModelListener, ActionListener, MouseListener {
        private final String[] columnNames = { "name" };

        private Key currentAk;

        private RelationalAttribute currentAttribute;

        private final LinkedList<Object[]> data = new LinkedList<>();

        @Override
        public void actionPerformed(ActionEvent e) {

        }

        public void clearAll() {
            if (currentAk != null)
                for (Variable v : currentAk.getKeyComponents())
                    v.deleteObserver(this);

            data.clear();
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

        public RelationalAttribute getCurrentAttribute() {
            return currentAttribute;
        }

        public Key getCurrentAk() {
            return currentAk;
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
            return true;
        }

        @Override
        public void mouseClicked(MouseEvent arg0) {}

        @Override
        public void mouseEntered(MouseEvent arg0) {}

        @Override
        public void mouseExited(MouseEvent arg0) {}

        @Override
        public void mousePressed(MouseEvent arg0) {
            if (currentAk == null) return;

            // Get the selected parameter
            final int index = akComponentsTable.getSelectionModel()
                    .getLeadSelectionIndex();
            final RelationalAttribute attribute = currentAk.getKeyComponents().get(index);

            setCurrentAttribute(attribute);
        }

        @Override
        public void mouseReleased(MouseEvent arg0) {}

        private void attributeSelected() {
            btnRemoveAkAttribute.setEnabled(currentAttribute != null);
            btnUpAkAttribute.setEnabled(currentAk.getKeyComponents().indexOf(
                    currentAttribute) > 0);
            btnDownAkAttribute.setEnabled(currentAk.getKeyComponents().indexOf(
                    currentAttribute) < currentAk.getKeyComponents().size() - 1);
        }

        public void removeCurrentAttribute() {
            final RelationalAttribute attribute = getCurrentAttribute();
            int index = currentAk.getKeyComponents().indexOf(attribute);

            if (attribute == null) return;

            currentAk.removeKeyComponent(attribute);
            attribute.notifyObservers();
            currentAk.select();
            currentAk.notifyObservers();
            currentAk.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);

            {
                currentAk.select();
                currentAk.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);

                final int size = currentAk.getKeyComponents().size();
                if (size == index) index--;

                if (size == 0) return;

                akComponentsTable.addRowSelectionInterval(index, index);
                ((RelationalEntityProperties.AkComponentTableModel) akComponentsTable.getModel())
                        .setCurrentAttribute(currentAk.getKeyComponents().get(index));
            }
        }

        public void setCurrentAttribute(RelationalAttribute attribute) {
            currentAttribute = attribute;
            attributeSelected();
        }

        public void setAttribute(Key key) {
            if (key == null) return;

            clearAll();
            for (final RelationalAttribute ra : key.getKeyComponents()) {
                ra.addObserver(this);
                data.add(new Object[] { ra.getName() });
            }

            fireTableRowsInserted(0, data.size());
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            data.get(row)[col] = value;

            fireTableCellUpdated(row, col);
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            if (currentAk == null) return;

            final int row = e.getFirstRow();
            final int column = e.getColumn();

            if (column == -1) return;

            final TableModel model = (TableModel) e.getSource();
            final Object data = model.getValueAt(row, column);

            if (column == 0) { // Attribute name
                currentAk.getKeyComponents().get(row).setName((String) data);
            }

            currentAk.getKeyComponents().get(row).notifyObservers();
            currentAk.notifyObservers();
            currentAk.getKeyComponents().get(row).getType().notifyObservers();
            currentAk.select();
            currentAk.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);

            final RelationalAttribute attribute = currentAk.getKeyComponents().get(row);

            akComponentsTable.addRowSelectionInterval(row, row);
            ((RelationalEntityProperties.AkComponentTableModel) akComponentsTable.getModel())
                    .setCurrentAttribute(attribute);
        }

        @Override
        public void update(Observable observable, Object o) {
            if (o instanceof IDiagramComponent.UpdateMessage) {
                switch ((IDiagramComponent.UpdateMessage) o) {
                    case SELECT:
                        showInProperties();
                        clearAll();
                        currentAk = (Key) observable;
                        setAttribute(currentAk);
                        panelAkAttributes.setVisible(true);
                        final boolean hasAttributes = currentAk.getKeyComponents().size() > 0;
                        scrollPaneAkAttributes.setVisible(hasAttributes);
                        imgNoAttribute.setVisible(!hasAttributes);
                        imgAkSelected.setVisible(false);
                        break;
                    case UNSELECT:
                        clearAll();
                        scrollPaneAkAttributes.setVisible(false);
                        imgAkSelected.setVisible(true);
                        imgNoAttribute.setVisible(false);
                        btnRemoveAkAttribute.setEnabled(false);
                        btnUpAkAttribute.setEnabled(false);
                        btnDownAkAttribute.setEnabled(false);
                        break;
                    default:
                        break;
                }
            } else
                setAttribute(currentAk);
        }
    }*/


    private final STable attributesTable, keyAttributesTable, triggerTable; //, alternateKeyTable, akComponentsTable;
    private final JButton btnRemoveAttribute, btnUpAttribute, btnDownAttribute, btnAddAttributeToPk,
            btnUpPkAttribute, btnDownPkAttribute, btnRemovePkAttribute,
            btnUpTrigger, btnDownTrigger, btnRemoveTrigger;
            //, btnUpAk, btnDownAk, btnRemoveAk,
            //btnUpAkAttribute, btnDownAkAttribute, btnRemoveAkAttribute;
    private final JTextField textName = new TextFieldWithPrompt("", "Enter the entity's name"),
                               pkName = new TextFieldWithPrompt("","Enter the Primary key's name");
    //private final JLabel imgAkSelected, imgNoAttribute;
    private final JLabel pk = new JLabel();
    //private final JPanel panelAkAttributes;
    //private final JScrollPane scrollPaneAkAttributes;
    private final TriggerProcedure triggerProcedure = new TriggerProcedure();

    public RelationalEntityProperties() {
        // Buttons for attributes.
        btnUpAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-up-24.png"), "Up");
        btnDownAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-down-24.png"), "Down");
        btnRemoveAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "minus.png"), "Remove");
        btnAddAttributeToPk = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "plusKey.png"), "Add to Pk");


        // Buttons for pk attributes.
        btnUpPkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-up-24.png"), "Up");
        btnDownPkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-down-24.png"), "Down");
        btnRemovePkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "minus.png"), "Remove");

        //Buttons for triggers
        btnUpTrigger = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-up-24.png"), "Up");
        btnDownTrigger = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-down-24.png"), "Down");
        btnRemoveTrigger = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "minus.png"), "Remove");

        /*
        //Buttons for Alternate keys
        btnUpAk = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-up-24.png"), "Up");
        btnDownAk = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-down-24.png"), "Down");
        btnRemoveAk = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "minus.png"), "Remove");

        // Buttons for ak attributes.
        btnUpAkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-up-24.png"), "Up");
        btnDownAkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "arrow-down-24.png"), "Down");
        btnRemoveAkAttribute = new SButton(
                PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "minus.png"), "Remove");

        // Others components
        imgAkSelected = new JLabel(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "select_ak.png"));
        imgAkSelected.setAlignmentX(CENTER_ALIGNMENT);

        imgNoAttribute = new JLabel(
                PersonalizedIcon.createImageIcon(
                        Slyum.ICON_PATH + "empty_attribute.png"));
        imgNoAttribute.setAlignmentX(CENTER_ALIGNMENT);
        imgNoAttribute.setVisible(false);
        */

        attributesTable = new STable(new RelationalEntityProperties.AttributeTableModel(),
                () -> addAttribute(false));
        attributesTable.setEmptyText("No attribute");
        attributesTable.setPreferredScrollableViewportSize(new Dimension(200, 0));

        attributesTable.getModel().addTableModelListener(
                (RelationalEntityProperties.AttributeTableModel) attributesTable.getModel()
        );

        attributesTable.addMouseListener(
                (RelationalEntityProperties.AttributeTableModel) attributesTable.getModel()
        );

        keyAttributesTable = new STable(new PKTableModel());
        keyAttributesTable.setEmptyText("No key attribute");
        keyAttributesTable.setPreferredScrollableViewportSize(new Dimension(70, 0));

        keyAttributesTable.getModel().addTableModelListener(
                (RelationalEntityProperties.PKTableModel) keyAttributesTable.getModel()
        );

        keyAttributesTable.addMouseListener(
                (RelationalEntityProperties.PKTableModel) keyAttributesTable.getModel()
        );

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

        /*
        alternateKeyTable = new STable(new RelationalEntityProperties.AlternateKeyTableModel(), () -> addAk(false));
        alternateKeyTable.setEmptyText("No alternate keys");
        alternateKeyTable.setPreferredScrollableViewportSize(new Dimension(70, 0));
        alternateKeyTable.getModel().addTableModelListener(
                (RelationalEntityProperties.AlternateKeyTableModel) alternateKeyTable.getModel());
        alternateKeyTable.addMouseListener((RelationalEntityProperties.AlternateKeyTableModel) alternateKeyTable.getModel());

        akComponentsTable = new STable(new RelationalEntityProperties.AkComponentTableModel());
        akComponentsTable.setPreferredScrollableViewportSize(new Dimension(70, 0));
        akComponentsTable.getModel().addTableModelListener(
                (RelationalEntityProperties.AkComponentTableModel) akComponentsTable.getModel());
        akComponentsTable.addMouseListener((RelationalEntityProperties.AkComponentTableModel) akComponentsTable.getModel());
         */

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
                updateComponentInformations(null);
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
                updateComponentInformations(null);
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
                updateComponentInformations(null);

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

        {
            btnAddAttributeToPk.setAlignmentX(CENTER_ALIGNMENT);
            btnAddAttributeToPk.setEnabled(false);
            btnAddAttributeToPk.addActionListener(arg0 -> {
                // Get the selected attribute
                final int index = attributesTable.getSelectionModel().getLeadSelectionIndex();
                RelationalAttribute attribute = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.AttributeTableModel) attributesTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                addPkAttribute(attribute);
                ((PKTableModel) keyAttributesTable.getModel()).fireTableDataChanged();
                attribute.notifyObservers(IDiagramComponent.UpdateMessage.ADD_KEY_NO_EDIT);
                keyAttributesTable.repaint();
            });

            panelButton.add(btnAddAttributeToPk);
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
                    PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "plus.png"), "Add");
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

        p = new FlatPanel();
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setLayout(new BorderLayout());
        panel = createWhitePanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));


        triggerProcedure.setLineWrap(true);
        triggerProcedure.setWrapStyleWord(true);
        triggerProcedure.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                change.Change.setHasChange(true);
                Trigger trigger = triggerProcedure.getCurrentTrigger();
                if(trigger != null)
                    trigger.setProcedure(triggerProcedure.getText());
            }



        });

        JScrollPane textAreaPane = new JScrollPane(triggerProcedure);
        textAreaPane.setPreferredSize(new Dimension(250, 0));
        textAreaPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textAreaPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel proc = new JLabel();
        proc.setText("Trigger Procedure");
        panel.add(proc);
        panel.add(Box.createVerticalStrut(5));
        panel.add(textAreaPane);

        p.add(panel, BorderLayout.CENTER);
        add(p);

        /*
        panelButton = new JPanel();
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.PAGE_AXIS));
        panelButton.setOpaque(false);

        {
            btnUpAk.setAlignmentX(CENTER_ALIGNMENT);
            btnUpAk.setEnabled(false);
            btnUpAk.addActionListener(evt -> {

                // Get the selected key
                final int index = alternateKeyTable.getSelectionModel()
                        .getLeadSelectionIndex();
                final Key key = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.AlternateKeyTableModel) alternateKeyTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).moveAkPosition(key, -1);
                ((RelationalEntity) currentObject).notifyObservers();
                key.select();
                key.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            });

            panelButton.add(btnUpAk);
        }
        {
            btnDownAk.setAlignmentX(CENTER_ALIGNMENT);
            btnDownAk.setEnabled(false);
            btnDownAk.addActionListener(evt -> {

                // Get the selected key
                final int index = alternateKeyTable.getSelectionModel()
                        .getLeadSelectionIndex();
                final Key key = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.AlternateKeyTableModel) alternateKeyTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).moveAkPosition(key, 1);
                ((SimpleEntity) currentObject).notifyObservers();
                key.select();
                key.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            });

            panelButton.add(btnDownAk);
        }

        {
            btnRemoveAk.setAlignmentX(CENTER_ALIGNMENT);
            btnRemoveAk.setEnabled(false);
            btnRemoveAk.addActionListener(arg0 -> {
                // Get the selected key
                final int index = alternateKeyTable.getSelectionModel()
                        .getLeadSelectionIndex();
                Key key = Utility
                        .getKeysByValue(
                                ((RelationalEntityProperties.AlternateKeyTableModel) alternateKeyTable.getModel())
                                        .getMapIndex(), index).iterator().next();

                ((RelationalEntity) currentObject).removeAlternateKey(key);
                ((RelationalEntity) currentObject).notifyObservers();

                for (int i = 0; i <= 1; i++) {
                    try {
                        key = Utility
                                .getKeysByValue(
                                        ((RelationalEntityProperties.AlternateKeyTableModel) alternateKeyTable.getModel())
                                                .getMapIndex(), index - i).iterator()
                                .next();
                    } catch (final NoSuchElementException e) {
                        continue;
                    }

                    key.select();
                    key.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
                    break;
                }
            });

            panelButton.add(btnRemoveAk);
        }

        p.add(panel, BorderLayout.CENTER);
        p.add(panelButton, BorderLayout.EAST);
        add(p);

        panel = panelAkAttributes = new FlatPanel();
        panel.setLayout(new MultiBorderLayout());
        panel.setAlignmentY(TOP_ALIGNMENT);
        scrollPaneAkAttributes = akComponentsTable.getScrollPane();
        scrollPaneAkAttributes.setVisible(false);
        panel.setMaximumSize(new Dimension(100, Short.MAX_VALUE));
        panel.setPreferredSize(new Dimension(200, 0));
        panel.add(scrollPaneAkAttributes, BorderLayout.CENTER);

        final JPanel btnPanel = new JPanel();

        btnUpAkAttribute.setAlignmentX(CENTER_ALIGNMENT);
        btnUpAkAttribute.setEnabled(false);
        btnUpAkAttribute.addActionListener(e -> {
            // Get the selected relationalAttribute
            int index = alternateKeyTable.getSelectionModel().getLeadSelectionIndex();
            final Key key = Utility
                    .getKeysByValue(
                            ((RelationalEntityProperties.AlternateKeyTableModel) alternateKeyTable.getModel())
                                    .getMapIndex(), index).iterator().next();

            final RelationalAttribute relationalAttribute = key.getKeyComponents().get(
                    akComponentsTable.getSelectionModel().getLeadSelectionIndex());

            index = akComponentsTable.getSelectionModel().getLeadSelectionIndex();
            key.moveAttributePosition(relationalAttribute, -1);
            key.notifyObservers();

            key.select();
            key.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);

            index--;
            akComponentsTable.addRowSelectionInterval(index, index);
            ((RelationalEntityProperties.AkComponentTableModel) akComponentsTable.getModel())
                    .setCurrentAttribute(relationalAttribute);
        });
        btnPanel.add(btnUpAkAttribute);

        btnDownAkAttribute.setAlignmentX(CENTER_ALIGNMENT);
        btnDownAkAttribute.setEnabled(false);
        btnDownAkAttribute.addActionListener(e -> {
            // Get the selected attribute
            int index = alternateKeyTable.getSelectionModel().getLeadSelectionIndex();
            final Key key = Utility
                    .getKeysByValue(
                            ((RelationalEntityProperties.AlternateKeyTableModel) alternateKeyTable.getModel())
                                    .getMapIndex(), index).iterator().next();

            index = akComponentsTable.getSelectionModel().getLeadSelectionIndex();
            final RelationalAttribute attribute = key.getKeyComponents().get(index);

            key.moveAttributePosition(attribute, 1);
            key.notifyObservers();

            key.select();
            key.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);

            index++;
            akComponentsTable.addRowSelectionInterval(index, index);
            ((RelationalEntityProperties.AkComponentTableModel) akComponentsTable.getModel())
                    .setCurrentAttribute(attribute);
        });
        btnPanel.add(btnDownAkAttribute);

        btnRemoveAkAttribute.setAlignmentX(CENTER_ALIGNMENT);
        btnRemoveAkAttribute.setEnabled(false);
        btnRemoveAkAttribute.addActionListener(e -> ((RelationalEntityProperties.AkComponentTableModel) akComponentsTable.getModel())
                .removeCurrentAttribute());
        btnPanel.add(btnRemoveAkAttribute);
        btnPanel.setBackground(null);
        btnPanel.setPreferredSize(new Dimension(190, 30));
        panel.add(imgAkSelected, BorderLayout.CENTER);
        panel.add(imgNoAttribute, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        add(panel);*/
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

        //pk name
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
                        relationalEntity.getPrimaryKey().notifyObservers();
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

    private void addPkAttribute(RelationalAttribute ra) {
        RelationalEntity entity = (RelationalEntity)currentObject;

        entity.getPrimaryKey().addKeyComponent(ra);
        updateComponentInformations(null);
        entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_KEY_NO_EDIT);
    }

    private void addTrigger(boolean editRequest) {
        RelationalEntity entity = (RelationalEntity)currentObject;

        entity.addTrigger(new Trigger("trigger"));

        if (editRequest)
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_TRIGGER);
        else
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_TRIGGER_NO_EDIT);
    }

    private void addAk(boolean editRequest) {
        RelationalEntity entity = (RelationalEntity)currentObject;

        entity.addAlternateKey(new Key("AK", entity));

        if (editRequest)
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_KEY);
        else
            entity.notifyObservers(IDiagramComponent.UpdateMessage.ADD_KEY_NO_EDIT);
    }


    private void stopEditingTables() {
        TableCellEditor a = attributesTable.getCellEditor();
        TableCellEditor pk = keyAttributesTable.getCellEditor();
        TableCellEditor t = triggerTable.getCellEditor();

        if (a != null) a.stopCellEditing();
        if (pk != null) pk.stopCellEditing();
        if (t != null) t.stopCellEditing();
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

        if (msg == IDiagramComponent.UpdateMessage.ADD_KEY
                || msg == IDiagramComponent.UpdateMessage.ADD_KEY_NO_EDIT)
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
