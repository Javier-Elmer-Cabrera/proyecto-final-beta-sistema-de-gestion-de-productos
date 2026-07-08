package com.ca.ui.panels;

import com.ca.core.Command;
import com.ca.core.CustomQueue;
import com.ca.core.EnqueueOrderCommand;
import com.ca.core.MemoryRepository;
import com.ca.core.ProcessOrderCommand;
import com.ca.core.PurchaseOrder;
import com.ca.db.model.Item;
import com.ca.db.model.Vendor;
import com.gt.common.constants.Status;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.gt.uilib.components.table.BetterJTable;
import com.gt.uilib.components.table.EasyTableModel;
import com.gt.uilib.inputverifier.Validator;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * Panel de gestión de Órdenes de Compra utilizando una Cola FIFO.
 * <p>
 * Permite:
 * <ul>
 *   <li>Registrar (encolar) nuevas órdenes de compra</li>
 *   <li>Visualizar todas las órdenes pendientes en una tabla</li>
 *   <li>Procesar (desencolar) la orden más antigua (FIFO)</li>
 * </ul>
 * </p>
 */
public class PurchaseOrderPanel extends AbstractFunctionPanel {

    private static final String[] HEADER = {"N.°", "ID", "Artículo", "Cantidad", "Proveedor", "Fecha", "Estado"};

    private JPanel formPanel;
    private JPanel buttonPanel;
    private Validator v;
    private JComboBox<Item> itemCombo;
    private JTextField quantityFld;
    private JComboBox<Vendor> vendorCombo;
    private JButton btnEnqueue;
    private JButton btnDequeue;
    private JButton btnUndo;
    private JButton btnRedo;
    private JButton btnRefresh;
    private JPanel upperPane;
    private JPanel lowerPane;
    private BetterJTable table;
    private EasyTableModel dataModel;

    public PurchaseOrderPanel() {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.4);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(getUpperSplitPane());
        splitPane.setRightComponent(getLowerSplitPane());
        init();
    }

    @Override
    public final void init() {
        super.init();
        UIUtils.clearAllFields(upperPane);
        loadComboData();
        changeStatus(Status.NONE);
    }

    private void loadComboData() {
        try {
            List<Item> items = MemoryRepository.getInstance().readAll(Item.class);
            itemCombo.removeAllItems();
            for (Item i : items) {
                itemCombo.addItem(i);
            }

            List<Vendor> vendors = MemoryRepository.getInstance().readAll(Vendor.class);
            vendorCombo.removeAllItems();
            for (Vendor v : vendors) {
                vendorCombo.addItem(v);
            }
        } catch (Exception e) {
            handleDBError(e);
        }
    }

    @Override
    public final void enableDisableComponents() {
        switch (status) {
            case NONE:
                UIUtils.toggleAllChildren(buttonPanel, true);
                UIUtils.toggleAllChildren(formPanel, true);
                break;
            default:
                break;
        }
    }

    @Override
    public final void handleSaveAction() {
        // No se usa el patrón Create/Modify; las acciones son directas.
    }

    // REQUISITO: Operación Encolar (enqueue) bajo el principio FIFO en la Cola de Órdenes.
    // Crea un PurchaseOrder y lo envuelve en un EnqueueOrderCommand para que,
    // al ejecutarse, el DataEngine lo apile en la pila de Undo (CustomStack LIFO).
    /**
     * Encola una nueva orden de compra.
     */
    private void handleEnqueue() {
        initValidator();
        if (!v.validate()) return;

        try {
            Item selectedItem = (Item) itemCombo.getSelectedItem();
            Vendor selectedVendor = (Vendor) vendorCombo.getSelectedItem();
            int qty = Integer.parseInt(quantityFld.getText().trim());

            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un artículo");
                return;
            }
            if (selectedVendor == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un proveedor");
                return;
            }

            PurchaseOrder order = new PurchaseOrder(selectedItem, qty, selectedVendor);
            EnqueueOrderCommand cmd = new EnqueueOrderCommand(
                    MemoryRepository.getInstance().getPendingOrders(), order);
            MemoryRepository.getInstance().executeCommand(cmd);

            JOptionPane.showMessageDialog(this, "Orden encolada exitosamente");
            UIUtils.clearAllFields(upperPane);
            loadComboData();
            refreshTable();
        } catch (Exception e) {
            handleDBError(e);
        }
    }

    // REQUISITO: Operación Desencolar (dequeue) bajo el principio FIFO en la Cola de Órdenes.
    // REQUISITO: Uso de la Pila (Stack LIFO) para el historial Undo/Redo mediante Command Pattern.
    // VINCULACIÓN: Este panel envía comandos al MemoryRepository, que los ejecuta en el DataEngine.
    // DataEngine apila el comando ejecutado en undoStack (CustomStack) para permitir deshacerlo.
    /**
     * Desencola (procesa) la orden más antigua de la cola FIFO.
     * La primera orden en llegar es la primera en procesarse.
     * <p>
     * Ahora encapsula la acción en un {@link ProcessOrderCommand}
     * y la ejecuta a través de {@link MemoryRepository#executeCommand(Command)},
     * lo que la apila automáticamente en la pila de Undo para
     * permitir deshacer/rehacer.
     * </p>
     */
    private void handleDequeue() {
        try {
            CustomQueue<PurchaseOrder> queue = MemoryRepository.getInstance().getPendingOrders();
            if (queue.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay órdenes pendientes en la cola");
                return;
            }

            // Crear el comando que encapsula toda la operación (desencolar + actualizar stock)
            ProcessOrderCommand cmd = new ProcessOrderCommand(queue);

            // Ejecutar el comando a través del DataEngine, que lo apila en undoStack
            MemoryRepository.getInstance().executeCommand(cmd);

            JOptionPane.showMessageDialog(this,
                    "Orden #" + cmd.getOrder().getId() + " procesada: "
                            + cmd.getOrder().getItem().getName()
                            + " x" + cmd.getOrder().getQuantity());

            refreshTable();
        } catch (Exception e) {
            handleDBError(e);
        }
    }

    // REQUISITO: Operación Desapilar (pop) de la pila de Undo (CustomStack LIFO).
    // El comando se transfiere de undoStack a redoStack para permitir rehacer.
    /**
     * Deshace la última orden procesada.
     * Saca el comando de undoStack, llama a su undo() que restaura
     * el stock y re-encola la orden al frente de la cola, y luego
     * apila el comando en redoStack.
     */
    private void handleUndo() {
        try {
            if (!MemoryRepository.getInstance().canUndo()) {
                JOptionPane.showMessageDialog(this, "No hay operaciones para deshacer");
                return;
            }
            MemoryRepository.getInstance().undo();
            JOptionPane.showMessageDialog(this, "Última operación deshecha");
            refreshTable();
        } catch (Exception e) {
            handleDBError(e);
        }
    }

    // REQUISITO: Operación Rehacer: desapila de redoStack y apila en undoStack (ambos CustomStack LIFO).
    /**
     * Rehace la última operación deshecha.
     * Saca el comando de redoStack, re-ejecuta execute() y lo vuelve
     * a apilar en undoStack.
     */
    private void handleRedo() {
        try {
            if (!MemoryRepository.getInstance().canRedo()) {
                JOptionPane.showMessageDialog(this, "No hay operaciones para rehacer");
                return;
            }
            MemoryRepository.getInstance().redo();
            JOptionPane.showMessageDialog(this, "Última operación rehecha");
            refreshTable();
        } catch (Exception e) {
            handleDBError(e);
        }
    }

    private void initValidator() {
        if (v != null) v.resetErrors();
        v = new Validator(mainApp, true);
        v.addTask(quantityFld, "Req", null, true);
    }

    // REQUISITO: Recorrido no destructivo de la Cola FIFO para mostrar los elementos en la tabla.
    // ACLARACIÓN: Se desencolan todos los elementos a una lista temporal y se re-encolan para
    // preservar el orden FIFO original sin perder datos.
    private void refreshTable() {
        try {
            CustomQueue<PurchaseOrder> queue = MemoryRepository.getInstance().getPendingOrders();
            dataModel.resetModel();
            int sn = 0;

            // Recorremos la cola temporalmente para mostrar sus elementos
            // sin desencolar. Usamos un arreglo porque CustomQueue no
            // implementa Iterable aún (se puede añadir después).
            java.util.List<PurchaseOrder> allOrders = new java.util.ArrayList<>();
            CustomQueue<PurchaseOrder> temp = new CustomQueue<>();
            while (!queue.isEmpty()) {
                PurchaseOrder o = queue.dequeue();
                allOrders.add(o);
                temp.enqueue(o);
            }
            // Restauramos la cola original
            while (!temp.isEmpty()) {
                queue.enqueue(temp.dequeue());
            }

            for (PurchaseOrder po : allOrders) {
                String itemName = po.getItem() != null ? po.getItem().getName() : "N/D";
                String vendorName = po.getVendor() != null ? po.getVendor().getName() : "N/D";
                dataModel.addRow(new Object[]{
                        ++sn,
                        po.getId(),
                        itemName,
                        po.getQuantity(),
                        vendorName,
                        po.getOrderDate(),
                        po.getStatus()
                });
            }

            table.setModel(dataModel);
            dataModel.fireTableDataChanged();
            table.adjustColumns();
        } catch (Exception e) {
            handleDBError(e);
        }
    }

    @Override
    public final String getFunctionName() {
        return "Órdenes de Compra (Cola)";
    }

    // DISEÑO UI: Configuración visual de botones de acción directa (Encolar, Desencolar,
    // Undo, Redo). La interacción con las estructuras (Queue/Stack) se realiza a través de
    // MemoryRepository.executeCommand(). No altera las estructuras internas directamente.
    // ─── Componentes UI ───

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();

            btnEnqueue = new JButton("Encolar Orden");
            btnEnqueue.addActionListener(e -> handleEnqueue());
            buttonPanel.add(btnEnqueue);

            btnDequeue = new JButton("Procesar Siguiente (FIFO)");
            btnDequeue.addActionListener(e -> handleDequeue());
            buttonPanel.add(btnDequeue);

            btnUndo = new JButton("Deshacer");
            btnUndo.addActionListener(e -> handleUndo());
            buttonPanel.add(btnUndo);

            btnRedo = new JButton("Rehacer");
            btnRedo.addActionListener(e -> handleRedo());
            buttonPanel.add(btnRedo);

            btnRefresh = new JButton("Actualizar");
            btnRefresh.addActionListener(e -> refreshTable());
            buttonPanel.add(btnRefresh);
        }
        return buttonPanel;
    }

    private JPanel getUpperFormPanel() {
        if (formPanel == null) {
            formPanel = new JPanel();
            formPanel.setBorder(new TitledBorder(null, "Nueva Orden de Compra",
                    TitledBorder.LEADING, TitledBorder.TOP, null, null));
            formPanel.setLayout(new FormLayout(
                    new ColumnSpec[]{
                            FormFactory.RELATED_GAP_COLSPEC,
                            FormFactory.DEFAULT_COLSPEC,
                            FormFactory.RELATED_GAP_COLSPEC,
                            ColumnSpec.decode("left:default:grow"),
                            FormFactory.RELATED_GAP_COLSPEC,
                            FormFactory.DEFAULT_COLSPEC,
                    },
                    new RowSpec[]{
                            FormFactory.RELATED_GAP_ROWSPEC,
                            FormFactory.DEFAULT_ROWSPEC,
                            FormFactory.RELATED_GAP_ROWSPEC,
                            FormFactory.DEFAULT_ROWSPEC,
                            FormFactory.RELATED_GAP_ROWSPEC,
                            FormFactory.DEFAULT_ROWSPEC,
                    }));

            JLabel lblItem = new JLabel("Artículo");
            formPanel.add(lblItem, "2, 2");

            itemCombo = new JComboBox<>();
            formPanel.add(itemCombo, "4, 2, fill, default");

            JLabel lblQty = new JLabel("Cantidad");
            formPanel.add(lblQty, "2, 4");

            quantityFld = new JTextField();
            formPanel.add(quantityFld, "4, 4, fill, default");

            JLabel lblVendor = new JLabel("Proveedor");
            formPanel.add(lblVendor, "2, 6");

            vendorCombo = new JComboBox<>();
            formPanel.add(vendorCombo, "4, 6, fill, default");
        }
        return formPanel;
    }

    private JPanel getUpperSplitPane() {
        if (upperPane == null) {
            upperPane = new JPanel();
            upperPane.setLayout(new BorderLayout(0, 0));
            upperPane.add(getUpperFormPanel(), BorderLayout.CENTER);
            upperPane.add(getButtonPanel(), BorderLayout.SOUTH);
        }
        return upperPane;
    }

    private JPanel getLowerSplitPane() {
        if (lowerPane == null) {
            lowerPane = new JPanel();
            lowerPane.setLayout(new BorderLayout());
            dataModel = new EasyTableModel(HEADER);
            table = new BetterJTable(dataModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane sp = new JScrollPane(table,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            lowerPane.add(sp, BorderLayout.CENTER);
        }
        return lowerPane;
    }
}
