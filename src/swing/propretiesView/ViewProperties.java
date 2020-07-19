package swing.propretiesView;

import classDiagram.IDiagramComponent;
import classDiagram.components.EnumEntity;
import classDiagram.components.EnumValue;
import swing.Slyum;
import swing.slyumCustomizedComponents.FlatPanel;
import swing.slyumCustomizedComponents.SButton;
import swing.slyumCustomizedComponents.STable;
import swing.slyumCustomizedComponents.TextFieldWithPrompt;
import utility.PersonalizedIcon;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

public class ViewProperties extends GlobalPropreties{

    private static ViewProperties instance;

    /**
     * Get the unique instance of this class.
     *
     * @return the unique instance of SimpleEntityPropreties
     */
    public static ViewProperties getInstance() {
        if (instance == null) instance = new ViewProperties();
        return instance;
    }

    private JTextField txtFieldName;
    private STable viewTable;
    private JButton btnUp, btnDown, btnDelete;

    Observer rowObserver = new Observer() {

        @Override
        public void update(Observable o, Object object) {
            if (object instanceof IDiagramComponent.UpdateMessage)
                switch ((IDiagramComponent.UpdateMessage) object) {
                    case SELECT:
                        int rowCount = viewTable.getRowCount();
                        int selectedRow = viewTable.getSelectedRow();
                        viewTable.selectRow(o);
                        btnDelete.setEnabled(true);
                        btnUp.setEnabled(selectedRow > 0);
                        btnDown.setEnabled(selectedRow < rowCount - 1);
                        viewTable.scrollToCell(selectedRow, 0);
                        break;
                    case UNSELECT:
                        btnDelete.setEnabled(false);
                        break;
                    default:
                        break;
                }
            else
                ((AbstractTableModel) viewTable.getModel())
                        .fireTableDataChanged();
        }
    };

    public ViewProperties() {
        initializeComponents();
    }

    @Override
    public void updateComponentInformations(IDiagramComponent.UpdateMessage msg) {
        if (currentObject == null) return;

        if (viewTable.getCellEditor() != null)
            viewTable.getCellEditor().stopCellEditing();

        EnumEntity enumEntity = (EnumEntity) currentObject;

        if (!txtFieldName.getText().equals(enumEntity.getName()))
            txtFieldName.setText(enumEntity.getName());
        enumEntity.addObserver(this);

        // Mise à jour des champs de la table
        AbstractTableModel model = (AbstractTableModel) viewTable.getModel();
        model.fireTableStructureChanged();

        for (int i = 0; i < model.getRowCount(); i++)
            ((EnumValue) model.getValueAt(i, 0)).addObserver(rowObserver);

        // Désactivation des composants.
        btnDelete.setEnabled(false);
        btnDown.setEnabled(false);
        btnUp.setEnabled(false);
    }

    private void initializeComponents() {
        JPanel panelAttributes = new JPanel(), panelButtons = new JPanel(), panelMain = new FlatPanel();
        JButton btnAdd;

        panelAttributes.setLayout(new BoxLayout(panelAttributes,
                BoxLayout.PAGE_AXIS));
        panelAttributes.setMaximumSize(new Dimension(200, Short.MAX_VALUE));

        // Enum name
        JLabel txtFieldName = new JLabel("Views");
        txtFieldName.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        panelAttributes.add(txtFieldName);
        // -----

        // Enum values
        viewTable = new STable(new AbstractTableModel() {

            @Override
            public Object getValueAt(int row, int col) {
                if (currentObject == null) return null;

                return ((EnumEntity) currentObject).getEnumValues().get(row);
            }

            @Override
            public int getRowCount() {
                if (currentObject == null) return 0;

                EnumEntity enumEntity = (EnumEntity) currentObject;
                return enumEntity.getEnumValues().size();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return true;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (currentObject == null) return;
                //TODO Changes here
                EnumEntity enumEntity = (EnumEntity) currentObject;
                EnumValue enumValue = enumEntity.getEnumValues().get(row);
                enumValue.setValue((String)value);
                enumValue.notifyObservers();
            }
        }) {
            @Override
            public void changeSelection(int rowIndex, int columnIndex,
                                        boolean toggle, boolean extend) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);

                EnumValue currentEnumValue = (EnumValue) getModel().getValueAt(
                        rowIndex, columnIndex);
                currentEnumValue.select();
                currentEnumValue.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            }
        };
        viewTable.setDefaultEditor(EnumValue.class, viewTable.new CustomCellEditor());
        viewTable.setTableHeader(null);
        viewTable.setFillsViewportHeight(true);
        viewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        viewTable.setPreferredScrollableViewportSize(new Dimension(0, 0));

        panelAttributes.add(Box.createVerticalStrut(5));
        panelAttributes.add(viewTable.getScrollPane());
        // -----

        // Buttons
        panelButtons.setBackground(null);
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.PAGE_AXIS));
        panelButtons.setMaximumSize(new Dimension(30, Short.MAX_VALUE));

        panelButtons.add(btnAdd = new SButton(PersonalizedIcon
                .createImageIcon(Slyum.ICON_PATH + "plus.png"), "Add"));
        btnAdd.addActionListener(evt -> {
            if (currentObject == null) return;

            ((EnumEntity) currentObject).createEnumValue();
        });

        panelButtons.add(btnUp = new SButton(PersonalizedIcon
                .createImageIcon(Slyum.ICON_PATH + "arrow-up-24.png"), "Up"));
        btnUp.addActionListener(evt -> {
            if (currentObject == null) return;

            EnumEntity enumEntity = (EnumEntity) currentObject;
            EnumValue enumValue = (EnumValue) viewTable.getSelectedRowValue();
            enumEntity.moveEnumPosition(enumValue, -1);
            enumEntity.notifyObservers();
            enumValue.select();
            enumValue.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
        });

        panelButtons.add(btnDown = new SButton(PersonalizedIcon
                .createImageIcon(Slyum.ICON_PATH + "arrow-down-24.png"), "Down"));
        btnDown.addActionListener(evt -> {
            if (currentObject == null) return;

            EnumEntity enumEntity = (EnumEntity) currentObject;
            EnumValue enumValue = (EnumValue) viewTable.getSelectedRowValue();
            enumEntity.moveEnumPosition(
                    (EnumValue) viewTable.getSelectedRowValue(), 1);
            enumEntity.notifyObservers();
            enumValue.select();
            enumValue.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
        });

        panelButtons.add(btnDelete = new SButton(PersonalizedIcon
                .createImageIcon(Slyum.ICON_PATH + "minus.png"), "Delete"));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(evt -> {
            if (currentObject == null) return;

            EnumEntity enumEntity = (EnumEntity) currentObject;
            int selectedRow = viewTable.getSelectedRow(), rowCount, rowToSelect;

            enumEntity.removeEnumValue((EnumValue) viewTable.getModel()
                    .getValueAt(selectedRow, 0));
            enumEntity.notifyObservers();

            // Recherche de l'enum devant être sélectionné après la suppression.
            rowCount = viewTable.getRowCount();
            rowToSelect = selectedRow >= rowCount ? rowCount - 1 : selectedRow;

            if (rowCount > 0) {
                EnumValue enumValueToSelect = (EnumValue) viewTable.getModel()
                        .getValueAt(rowToSelect, 0);
                enumValueToSelect.select();
                enumValueToSelect.notifyObservers(IDiagramComponent.UpdateMessage.SELECT);
            }
        });
        // -----

        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.LINE_AXIS));
        panelMain.setMaximumSize(new Dimension(250, Short.MAX_VALUE));
        panelMain.add(panelAttributes);
        panelMain.add(Box.createHorizontalStrut(5));
        panelMain.add(panelButtons);
        add(panelMain);
    }
}
