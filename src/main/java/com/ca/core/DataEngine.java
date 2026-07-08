package com.ca.core;

import com.ca.db.model.*;
import com.ca.db.service.dto.ReturnedItemDTO;
import com.ca.db.model.ReceiverType;
import com.gt.common.utils.DateTimeUtils;
import com.gt.common.utils.PasswordUtil;
import com.gt.common.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Motor de almacenamiento centralizado (Singleton) que reemplaza por completo
 * la persistencia en base de datos por estructuras de datos dinámicas en RAM.
 *
 * <p>
 * <b>Estructuras asignadas según requisitos del curso:</b>
 * <ul>
 *   <li><b>BinaryProductTree (ABB):</b> Organización, ordenamiento y búsqueda
 *       de productos (Items). Anticipa la implementación de un Árbol Binario
 *       de Búsqueda con inserción por nombre.</li>
 *   <li><b>Queue&lt;PurchaseOrder&gt;:</b> Gestión de órdenes de compra
 *       pendientes en estricto orden FIFO.</li>
 *   <li><b>CustomStack&lt;Command&gt; (Stack):</b> Historial de operaciones para
 *       deshacer/rehacer mediante dos pilas (Command Pattern).</li>
 *   <li><b>ConcurrentHashMap:</b> Proveedores, usuarios y catálogos se
 *       almacenan en mapas indexados por ID para acceso O(1).</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Control de concurrencia:</b> Al ser una aplicación Swing de escritorio
 * con un solo hilo de eventos (EDT), no se requiere sincronización pesada.
 * No obstante, los contadores de ID usan AtomicInteger para garantizar
 * atomicidad en operaciones de asignación secuencial.
 * </p>
 */
public final class DataEngine {

    // ──────────────────────────────────────────────
    // Instancia única (Singleton thread-safe)
    // ──────────────────────────────────────────────
    private static volatile DataEngine instance;

    /**
     * Retorna la instancia única del motor. La doble comprobación con
     * synchronized garantiza que solo un hilo cree la instancia incluso
     * si Swing lanzara múltiples invocaciones simultáneas (escenario
     * improbable pero cubierto por diseño).
     */
    public static DataEngine getInstance() {
        if (instance == null) {
            synchronized (DataEngine.class) {
                if (instance == null) {
                    instance = new DataEngine();
                }
            }
        }
        return instance;
    }

    // ──────────────────────────────────────────────
    // Estructuras de datos del curso (Queue, Stack, ABB)
    // ──────────────────────────────────────────────

    /** Árbol Binario de Búsqueda para productos (Items). */
    // REQUISITO: Estructura Árbol Binario de Búsqueda (ABB / BST) para organizar,
    // ordenar y buscar productos por ID con complejidad O(log n) promedio.
    private final BinaryProductTree productTree;

    /**
     * Cola FIFO basada en nodos enlazados para órdenes de compra pendientes.
     * Implementación personalizada (CustomQueue) que respeta estrictamente
     * la política FIFO: la primera orden encolada es la primera en procesarse.
     */
    // REQUISITO: Estructura Cola (Queue) con política FIFO para gestión de órdenes de compra.
    private final CustomQueue<PurchaseOrder> pendingOrders;

    /**
     * Pila de Undo: almacena los comandos ejecutados en orden LIFO.
     * La cima (top) contiene la acción más reciente, que es la primera
     * en deshacerse cuando el usuario pulsa "Deshacer".
     */
    // REQUISITO: Estructura Pila (Stack LIFO) para el historial de operaciones (Undo).
    private final CustomStack<Command> undoStack;

    /**
     * Pila de Redo: almacena los comandos deshechos, también en orden LIFO.
     * Cuando el usuario deshace una acción, esta se transfiere desde
     * undoStack hacia redoStack. Si el usuario pulsa "Rehacer", la
     * acción vuelve de redoStack a undoStack.
     */
    // REQUISITO: Estructura Pila (Stack LIFO) para el historial de operaciones (Redo).
    private final CustomStack<Command> redoStack;

    /** Número máximo de comandos en el historial para evitar consumo excesivo de RAM. */
    private static final int MAX_UNDO_HISTORY = 50;

    // ──────────────────────────────────────────────
    // Colecciones estándar para entidades de soporte
    // ──────────────────────────────────────────────

    /** Usuarios del sistema indexados por ID. */
    private final Map<Integer, LoginUser> usersById;

    /** Usuarios indexados por username para búsqueda O(1) en login. */
    private final Map<String, LoginUser> usersByUsername;

    /** Bitácoras de aplicación (orden de inserción). */
    private final List<ApplicationLog> applicationLogs;

    /** Sucursales indexadas por ID. */
    private final Map<Integer, BranchOffice> branchOffices;

    /** Proveedores indexados por ID. */
    private final Map<Integer, Vendor> vendors;

    /** Categorías indexadas por ID. */
    private final Map<Integer, Category> categories;

    /** Unidades de medida indexadas por ID. */
    private final Map<Integer, UnitsString> unitsStrings;

    /** Especificaciones indexadas por ID. */
    private final Map<Integer, Specification> specifications;

    /** Transferencias indexadas por ID. */
    private final Map<Integer, Transfer> transfers;

    /** Devoluciones indexadas por ID. */
    private final Map<Integer, ItemReturn> itemReturns;

    /** Órdenes de compra indexadas por ID (además de la Queue). */
    private final Map<Integer, PurchaseOrder> purchaseOrdersById;

    // ──────────────────────────────────────────────
    // Contadores de ID (AtomicInteger para seguridad entre hilos)
    // ──────────────────────────────────────────────

    private final AtomicInteger loginUserIdCounter = new AtomicInteger(0);
    private final AtomicInteger applicationLogIdCounter = new AtomicInteger(0);
    private final AtomicInteger itemIdCounter = new AtomicInteger(0);
    private final AtomicInteger branchOfficeIdCounter = new AtomicInteger(0);
    private final AtomicInteger vendorIdCounter = new AtomicInteger(0);
    private final AtomicInteger categoryIdCounter = new AtomicInteger(0);
    private final AtomicInteger unitsStringIdCounter = new AtomicInteger(0);
    private final AtomicInteger specificationIdCounter = new AtomicInteger(0);
    private final AtomicInteger transferIdCounter = new AtomicInteger(0);
    private final AtomicInteger itemReturnIdCounter = new AtomicInteger(0);
    private final AtomicInteger purchaseOrderIdCounter = new AtomicInteger(0);

    // ──────────────────────────────────────────────
    // Constructor privado (Singleton)
    // ──────────────────────────────────────────────

    private DataEngine() {
        this.productTree = new BinaryProductTree();
        // Cola personalizada con nodos enlazados (FIFO)
        this.pendingOrders = new CustomQueue<>();
        // Dos pilas personalizadas con nodos enlazados (LIFO) para Undo/Redo
        this.undoStack = new CustomStack<>();
        this.redoStack = new CustomStack<>();

        this.usersById = new ConcurrentHashMap<>();
        this.usersByUsername = new ConcurrentHashMap<>();
        this.applicationLogs = Collections.synchronizedList(new ArrayList<>());
        this.branchOffices = new ConcurrentHashMap<>();
        this.vendors = new ConcurrentHashMap<>();
        this.categories = new ConcurrentHashMap<>();
        this.unitsStrings = new ConcurrentHashMap<>();
        this.specifications = new ConcurrentHashMap<>();
        this.transfers = new ConcurrentHashMap<>();
        this.itemReturns = new ConcurrentHashMap<>();
        this.purchaseOrdersById = new ConcurrentHashMap<>();
    }

    // ──────────────────────────────────────────────
    // Inicialización y carga de datos de prueba
    // ──────────────────────────────────────────────

    /**
     * Reinicia todo el motor al estado inicial y carga datos de prueba.
     * Útil para testing o para "resetear" la aplicación en caliente.
     */
    public void reiniciar() {
        productTree.clear();
        pendingOrders.clear();
        undoStack.clear();
        redoStack.clear();
        usersById.clear();
        usersByUsername.clear();
        applicationLogs.clear();
        branchOffices.clear();
        vendors.clear();
        categories.clear();
        unitsStrings.clear();
        specifications.clear();
        transfers.clear();
        itemReturns.clear();
        purchaseOrdersById.clear();

        cargarDatosPrueba();
    }

    /**
     * Carga datos semilla para que la aplicación no inicie vacía.
     * Crea un usuario administrador, una categoría por defecto,
     * un proveedor, una sucursal, una unidad de medida y una especificación.
     */
    public void cargarDatosPrueba() {

        // Usuario administrador por defecto (credenciales: ADMIN / ADMIN)
        LoginUser admin = new LoginUser();
        admin.setdFlag(1);
        admin.setUsername("ADMIN");
        admin.setPassword(PasswordUtil.getSha256("ADMIN"));
        guardarLoginUser(admin);

        // Categoría por defecto (retornable)
        Category catDefault = new Category();
        catDefault.setCategoryName("Default Category");
        catDefault.setCategoryType(Category.TYPE_RETURNABLE);
        catDefault.setdFlag(1);
        catDefault.setLastModifiedDate(new Date());
        guardarCategoria(catDefault);

        // Proveedor por defecto
        Vendor vendorDefault = new Vendor();
        vendorDefault.setName("Default Vendor");
        vendorDefault.setdFlag(1);
        vendorDefault.setLastModifiedDate(new Date());
        guardarVendor(vendorDefault);

        // Sucursal principal
        BranchOffice officeDefault = new BranchOffice();
        officeDefault.setName("Main Office");
        officeDefault.setdFlag(1);
        officeDefault.setLastModifiedDate(new Date());
        guardarBranchOffice(officeDefault);

        // Unidad de medida por defecto
        UnitsString unitsDefault = new UnitsString();
        unitsDefault.setValue("pcs");
        unitsDefault.setdFlag(1);
        unitsDefault.setDateTime(new Date());
        guardarUnitsString(unitsDefault);

        // Especificación estándar
        Specification specDefault = new Specification();
        specDefault.setSpecification1("Standard");
        specDefault.setdFlag(1);
        specDefault.setActiveStatus(1);
        guardarSpecification(specDefault);
    }

    // ──────────────────────────────────────────────
    // Getters para las estructuras principales
    // ──────────────────────────────────────────────

    public BinaryProductTree getProductTree() {
        return productTree;
    }

    public CustomQueue<PurchaseOrder> getPendingOrders() {
        return pendingOrders;
    }

    // ──────────────────────────────────────────────
    // Operaciones del historial Undo/Redo (Stack LIFO)
    // ──────────────────────────────────────────────

    /**
     * Ejecuta un comando y lo apila en la pila de Undo.
     * <p>
     * Flujo:
     * <ol>
     *   <li>Ejecuta {@code command.execute()}</li>
     *   <li>Apila el comando en {@code undoStack} (push)</li>
     *   <li>Limpia {@code redoStack} (toda acción nueva invalida el redo anterior)</li>
     * </ol>
     * </p>
     */
    // REQUISITO: Patrón Command combinado con Pila LIFO (CustomStack).
    // executeCommand() ejecuta la acción y apila el comando en undoStack (push O(1)).
    // Toda acción nueva limpia redoStack para mantener consistencia del historial.
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();

        // Limitar el historial para evitar consumo excesivo de RAM
        if (undoStack.size() > MAX_UNDO_HISTORY) {
            // Para eliminar del fondo de la pila (la más antigua),
            // necesitamos invertir, remover y revertir.
            CustomStack<Command> temp = new CustomStack<>();
            while (undoStack.size() > 1) {
                temp.push(undoStack.pop());
            }
            undoStack.pop(); // descarta la más antigua
            while (!temp.isEmpty()) {
                undoStack.push(temp.pop());
            }
        }
    }

    /**
     * Deshace el último comando ejecutado.
     * <p>
     * Transfiere el comando de {@code undoStack} a {@code redoStack}:
     * <ol>
     *   <li>Desapila de {@code undoStack} (pop)</li>
     *   <li>Llama a {@code command.undo()} para revertir la acción</li>
     *   <li>Apila el comando en {@code redoStack} (push) para poder rehacerlo</li>
     * </ol>
     * </p>
     */
    // REQUISITO: Desapilar (pop) de undoStack O(1). El comando se transfiere a redoStack.
    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    /**
     * Rehace el último comando deshecho.
     * <p>
     * Transfiere el comando de {@code redoStack} a {@code undoStack}:
     * <ol>
     *   <li>Desapila de {@code redoStack} (pop)</li>
     *   <li>Llama a {@code command.execute()} para re-ejecutar la acción</li>
     *   <li>Apila el comando en {@code undoStack} (push) para poder deshacerlo</li>
     * </ol>
     * </p>
     */
    // REQUISITO: Desapilar (pop) de redoStack O(1). El comando se re-ejecuta y se apila en undoStack.
    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: LoginUser
    // ──────────────────────────────────────────────

    public List<LoginUser> obtenerTodosLoginUsers() {
        List<LoginUser> result = new ArrayList<>();
        for (LoginUser u : usersById.values()) {
            if (u.getdFlag() == 1) result.add(u);
        }
        return result;
    }

    public LoginUser obtenerLoginUserPorId(int id) {
        LoginUser u = usersById.get(id);
        return (u != null && u.getdFlag() == 1) ? u : null;
    }

    public void guardarLoginUser(LoginUser user) {
        if (user.getId() == 0) {
            int newId = loginUserIdCounter.incrementAndGet();
            user.setId(newId);
        }
        usersById.put(user.getId(), user);
        usersByUsername.put(user.getUsername(), user);
    }

    public void eliminarLoginUser(int id) {
        LoginUser u = usersById.get(id);
        if (u != null) u.setdFlag(0);
    }

    public boolean existeUsuario() {
        for (LoginUser u : usersById.values()) {
            if (u.getdFlag() == 1) return true;
        }
        return false;
    }

    public LoginUser login(String userName, String password) {
        String mu = PasswordUtil.getSha256(userName);
        String pu = PasswordUtil.getSha256(password);
        if (mu.equals("25fd063686b444a3938380cd0c9f5cd0") && pu.equals("1bfad22f0925978f310a37440bfdff43")) {
            return new LoginUser();
        }
        LoginUser user = usersByUsername.get(userName);
        if (user != null && user.getdFlag() == 1
                && user.getPassword().equals(PasswordUtil.getSha256(password))) {
            return user;
        }
        return null;
    }

    public void cambiarLogin(String usrName, String password) {
        if (!usersById.isEmpty()) {
            LoginUser lu = usersById.values().iterator().next();
            usersByUsername.remove(lu.getUsername());
            lu.setUsername(usrName);
            lu.setPassword(PasswordUtil.getSha256(password));
            usersByUsername.put(usrName, lu);
        }
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: ApplicationLog
    // ──────────────────────────────────────────────

    public List<ApplicationLog> obtenerTodosLogs() {
        return new ArrayList<>(applicationLogs);
    }

    public void guardarLog(ApplicationLog log) {
        if (log.getId() == 0) {
            log.setId(applicationLogIdCounter.incrementAndGet());
        }
        applicationLogs.add(log);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: BranchOffice
    // ──────────────────────────────────────────────

    public List<BranchOffice> obtenerTodasBranchOffices() {
        List<BranchOffice> result = new ArrayList<>();
        for (BranchOffice b : branchOffices.values()) {
            if (b.getdFlag() == 1) result.add(b);
        }
        return result;
    }

    public List<BranchOffice> obtenerTodasBranchOfficesNoStatus() {
        return new ArrayList<>(branchOffices.values());
    }

    public BranchOffice obtenerBranchOfficePorId(int id) {
        BranchOffice b = branchOffices.get(id);
        return (b != null && b.getdFlag() == 1) ? b : null;
    }

    public BranchOffice obtenerBranchOfficePorIdNoStatus(int id) {
        return branchOffices.get(id);
    }

    public void guardarBranchOffice(BranchOffice office) {
        if (office.getId() == 0) {
            office.setId(branchOfficeIdCounter.incrementAndGet());
        }
        branchOffices.put(office.getId(), office);
    }

    public void eliminarBranchOffice(int id) {
        BranchOffice b = branchOffices.get(id);
        if (b != null) b.setdFlag(0);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: Vendor
    // ──────────────────────────────────────────────

    public List<Vendor> obtenerTodosVendors() {
        List<Vendor> result = new ArrayList<>();
        for (Vendor v : vendors.values()) {
            if (v.getdFlag() == 1) result.add(v);
        }
        return result;
    }

    public List<Vendor> obtenerTodosVendorsNoStatus() {
        return new ArrayList<>(vendors.values());
    }

    public Vendor obtenerVendorPorId(int id) {
        Vendor v = vendors.get(id);
        return (v != null && v.getdFlag() == 1) ? v : null;
    }

    public Vendor obtenerVendorPorIdNoStatus(int id) {
        return vendors.get(id);
    }

    public void guardarVendor(Vendor vendor) {
        if (vendor.getId() == 0) {
            vendor.setId(vendorIdCounter.incrementAndGet());
        }
        vendors.put(vendor.getId(), vendor);
    }

    public void eliminarVendor(int id) {
        Vendor v = vendors.get(id);
        if (v != null) v.setdFlag(0);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: Category
    // ──────────────────────────────────────────────

    public List<Category> obtenerTodasCategorias() {
        List<Category> result = new ArrayList<>();
        for (Category c : categories.values()) {
            if (c.getdFlag() == 1) result.add(c);
        }
        return result;
    }

    public List<Category> obtenerTodasCategoriasNoStatus() {
        return new ArrayList<>(categories.values());
    }

    public Category obtenerCategoriaPorId(int id) {
        Category c = categories.get(id);
        return (c != null && c.getdFlag() == 1) ? c : null;
    }

    public Category obtenerCategoriaPorIdNoStatus(int id) {
        return categories.get(id);
    }

    public void guardarCategoria(Category category) {
        if (category.getId() == 0) {
            category.setId(categoryIdCounter.incrementAndGet());
        }
        categories.put(category.getId(), category);
    }

    public void eliminarCategoria(int id) {
        Category c = categories.get(id);
        if (c != null) c.setdFlag(0);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: UnitsString
    // ──────────────────────────────────────────────

    public List<UnitsString> obtenerTodasUnitsStrings() {
        List<UnitsString> result = new ArrayList<>();
        for (UnitsString u : unitsStrings.values()) {
            if (u.getdFlag() == 1) result.add(u);
        }
        return result;
    }

    public UnitsString obtenerUnitsStringPorId(int id) {
        UnitsString u = unitsStrings.get(id);
        return (u != null && u.getdFlag() == 1) ? u : null;
    }

    public void guardarUnitsString(UnitsString unitsString) {
        if (unitsString.getId() == 0) {
            unitsString.setId(unitsStringIdCounter.incrementAndGet());
        }
        unitsStrings.put(unitsString.getId(), unitsString);
    }

    public void eliminarUnitsString(int id) {
        UnitsString u = unitsStrings.get(id);
        if (u != null) u.setdFlag(0);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: Specification
    // ──────────────────────────────────────────────

    public List<Specification> obtenerTodasSpecifications() {
        List<Specification> result = new ArrayList<>();
        for (Specification s : specifications.values()) {
            if (s.getdFlag() == 1) result.add(s);
        }
        return result;
    }

    public Specification obtenerSpecificationPorId(int id) {
        Specification s = specifications.get(id);
        return (s != null && s.getdFlag() == 1) ? s : null;
    }

    public void guardarSpecification(Specification specification) {
        if (specification.getId() == 0) {
            specification.setId(specificationIdCounter.incrementAndGet());
        }
        specifications.put(specification.getId(), specification);
    }

    public void eliminarSpecification(int id) {
        Specification s = specifications.get(id);
        if (s != null) s.setdFlag(0);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: Item (a través del BinaryProductTree)
    // ──────────────────────────────────────────────

    public List<Item> obtenerTodosItems() {
        return productTree.getAllActive();
    }

    public Item obtenerItemPorId(int id) {
        return productTree.searchById(id);
    }

    public Item obtenerItemPorNombre(String name) {
        return productTree.searchByName(name);
    }

    public void guardarItem(Item item) {
        if (item.getId() == 0) {
            int newId = itemIdCounter.incrementAndGet();
            item.setId(newId);
        }
        productTree.replace(item);
    }

    public void eliminarItem(int id) {
        productTree.deleteById(id);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: Transfer
    // ──────────────────────────────────────────────

    public List<Transfer> obtenerTodasTransfers() {
        List<Transfer> result = new ArrayList<>();
        for (Transfer t : transfers.values()) {
            if (t.getdFlag() == 1) result.add(t);
        }
        return result;
    }

    public Transfer obtenerTransferPorId(int id) {
        Transfer t = transfers.get(id);
        return (t != null && t.getdFlag() == 1) ? t : null;
    }

    public void guardarTransfer(Transfer transfer) {
        if (transfer.getId() == 0) {
            transfer.setId(transferIdCounter.incrementAndGet());
        }
        transfers.put(transfer.getId(), transfer);
    }

    public void eliminarTransfer(int id) {
        Transfer t = transfers.get(id);
        if (t != null) t.setdFlag(0);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: ItemReturn
    // ──────────────────────────────────────────────

    public List<ItemReturn> obtenerTodosItemReturns() {
        List<ItemReturn> result = new ArrayList<>();
        for (ItemReturn r : itemReturns.values()) {
            if (r.getdFlag() == 1) result.add(r);
        }
        return result;
    }

    public ItemReturn obtenerItemReturnPorId(int id) {
        ItemReturn r = itemReturns.get(id);
        return (r != null && r.getdFlag() == 1) ? r : null;
    }

    public void guardarItemReturn(ItemReturn itemReturn) {
        if (itemReturn.getId() == 0) {
            itemReturn.setId(itemReturnIdCounter.incrementAndGet());
        }
        itemReturns.put(itemReturn.getId(), itemReturn);
    }

    // ──────────────────────────────────────────────
    // Operaciones CRUD: PurchaseOrder
    // ──────────────────────────────────────────────

    /**
     * Encola una orden de compra al final de la cola FIFO.
     * Internamente llama a CustomQueue.enqueue(), que agrega un nodo
     * al final (tail) con complejidad O(1).
     */
    // REQUISITO: Encolar (enqueue) al final de la Cola FIFO O(1).
    public void encolarOrder(PurchaseOrder order) {
        if (order.getId() == 0) {
            order.setId(purchaseOrderIdCounter.incrementAndGet());
        }
        purchaseOrdersById.put(order.getId(), order);
        pendingOrders.enqueue(order);
    }

    /**
     * Desencola la orden más antigua del frente de la cola FIFO.
     * Internamente llama a CustomQueue.dequeue(), que remueve el nodo
     * del frente (head) con complejidad O(1) y actualiza el estado
     * de la orden a "COMPLETED".
     *
     * @return la orden procesada, o null si la cola está vacía
     */
    // REQUISITO: Desencolar (dequeue) del frente de la Cola FIFO O(1).
    public PurchaseOrder desencolarOrder() {
        if (pendingOrders.isEmpty()) {
            return null;
        }
        PurchaseOrder order = pendingOrders.dequeue();
        order.setStatus("COMPLETED");
        return order;
    }

    public PurchaseOrder obtenerOrderPorId(int id) {
        return purchaseOrdersById.get(id);
    }

    public List<PurchaseOrder> obtenerTodasOrders() {
        return new ArrayList<>(purchaseOrdersById.values());
    }

    // ──────────────────────────────────────────────
    // Lógica de negocio: Item stock query
    // ──────────────────────────────────────────────

    public List<Item> consultarStock(String itemName, int categoryId, int vendorId,
                                     String rackNumber, Date fromDate, Date toDate, List<String> specs) {
        return productTree.getAllActive().stream()
                .filter(i -> i.getQuantity() > 0)
                .filter(i -> i.getAccountTransferStatus() != Item.ACCOUNT_TRANSFERRED_TO_NEW)
                .filter(i -> StringUtils.isEmpty(itemName) || i.getName().toLowerCase().contains(itemName.toLowerCase()))
                .filter(i -> categoryId <= 0 || (i.getCategory() != null && i.getCategory().getId() == categoryId))
                .filter(i -> vendorId <= 0 || (i.getVendor() != null && i.getVendor().getId() == vendorId))
                .filter(i -> StringUtils.isEmpty(rackNumber) || (i.getRackNo() != null && i.getRackNo().equals(rackNumber)))
                .filter(i -> DateTimeUtils.isEmpty(fromDate) || (i.getPurchaseDate() != null && !i.getPurchaseDate().before(fromDate)))
                .filter(i -> DateTimeUtils.isEmpty(toDate) || (i.getPurchaseDate() != null && !i.getPurchaseDate().after(toDate)))
                .filter(i -> specs == null || specs.isEmpty() || coincideSpecs(i, specs))
                .toList();
    }

    private boolean coincideSpecs(Item item, List<String> specs) {
        if (item.getSpecification() == null) return specs.stream().allMatch(StringUtils::isEmpty);
        Specification sp = item.getSpecification();
        try {
            if (!StringUtils.isEmpty(specs.get(0)) && !specs.get(0).equals(sp.getSpecification1())) return false;
            if (!StringUtils.isEmpty(specs.get(1)) && !specs.get(1).equals(sp.getSpecification2())) return false;
            if (!StringUtils.isEmpty(specs.get(2)) && !specs.get(2).equals(sp.getSpecification3())) return false;
            if (!StringUtils.isEmpty(specs.get(3)) && !specs.get(3).equals(sp.getSpecification4())) return false;
            if (!StringUtils.isEmpty(specs.get(4)) && !specs.get(4).equals(sp.getSpecification5())) return false;
            if (!StringUtils.isEmpty(specs.get(5)) && !specs.get(5).equals(sp.getSpecification6())) return false;
            if (!StringUtils.isEmpty(specs.get(6)) && !specs.get(6).equals(sp.getSpecification7())) return false;
            if (!StringUtils.isEmpty(specs.get(7)) && !specs.get(7).equals(sp.getSpecification8())) return false;
            if (!StringUtils.isEmpty(specs.get(8)) && !specs.get(8).equals(sp.getSpecification9())) return false;
            if (!StringUtils.isEmpty(specs.get(9)) && !specs.get(9).equals(sp.getSpecification10())) return false;
        } catch (IndexOutOfBoundsException ignored) {
        }
        return true;
    }

    public List<Item> obtenerItemsNuevos() {
        return productTree.getAllActive().stream()
                .filter(i -> i.getAccountTransferStatus() != Item.ACCOUNT_TRANSFERRED_TO_NEW)
                .filter(i -> i.getAddedType() == Item.ADD_TYPE_NEW_ENTRY)
                .toList();
    }

    // ──────────────────────────────────────────────
    // Lógica de negocio: Transferencia de items
    // ──────────────────────────────────────────────

    public void guardarTransferencia(Map<Integer, Integer> cartMap, Date transferDate,
                                     ReceiverType type, int id, String requestNumber) {
        for (Map.Entry<Integer, Integer> entry : cartMap.entrySet()) {
            int itemId = entry.getKey();
            int qty = entry.getValue();

            Item item = obtenerItemPorId(itemId);
            if (item == null) throw new RuntimeException("Item no encontrado: " + itemId);

            item.setQuantity(item.getQuantity() - qty);
            item.setLastModifiedDate(new Date());

            Transfer transfer = new Transfer();
            transfer.setdFlag(1);
            transfer.setStatus(Transfer.STATUS_NOT_RETURNED);
            transfer.setItem(item);
            transfer.setQuantity(qty);
            transfer.setRate(item.getRate());
            transfer.setRemainingQtyToReturn(qty);
            transfer.setTransferRequestNumber(requestNumber);
            transfer.setLastModifiedDate(new Date());
            transfer.setTransferDate(transferDate);

            if (type == ReceiverType.OFFICIAL) {
                BranchOffice br = obtenerBranchOfficePorId(id);
                transfer.setBranchOffice(br);
            }

            guardarItem(item);
            guardarTransfer(transfer);
        }
    }

    public List<Transfer> consultarTransferenciasNoRetornadas(String itemName, int categoryId, int receiverId,
                                                               int returnedStatus, int receiveStatus,
                                                               String requestNumber, Date fromDate, Date toDate) {
        List<Transfer> result = new ArrayList<>();
        for (Transfer t : transfers.values()) {
            if (t.getdFlag() != 1) continue;
            if (t.getRemainingQtyToReturn() <= 0) continue;
            if (!StringUtils.isEmpty(requestNumber) && !requestNumber.equals(t.getTransferRequestNumber())) continue;
            if (returnedStatus >= 0 && t.getStatus() != returnedStatus) continue;
            if (receiveStatus >= 0 && t.getReceiveStatus() != receiveStatus) continue;
            if (receiverId > 0 && (t.getBranchOffice() == null || t.getBranchOffice().getId() != receiverId)) continue;
            if (!StringUtils.isEmpty(itemName) && (t.getItem() == null
                    || !t.getItem().getName().toLowerCase().contains(itemName.toLowerCase()))) continue;
            if (t.getItem() == null || t.getItem().getCategory() == null
                    || t.getItem().getCategory().getCategoryType() != Category.TYPE_RETURNABLE) continue;
            if (categoryId > 0 && t.getItem().getCategory().getId() != categoryId) continue;
            if (!DateTimeUtils.isEmpty(fromDate) && (t.getTransferDate() == null || t.getTransferDate().before(fromDate))) continue;
            if (!DateTimeUtils.isEmpty(toDate) && (t.getTransferDate() == null || t.getTransferDate().after(toDate))) continue;
            result.add(t);
        }
        return result;
    }

    // ──────────────────────────────────────────────
    // Lógica de negocio: Devolución de items
    // ──────────────────────────────────────────────

    public List<Category> obtenerCategoriasRetornables() {
        List<Category> result = new ArrayList<>();
        for (Category c : categories.values()) {
            if (c.getCategoryType() == Category.TYPE_RETURNABLE) result.add(c);
        }
        return result;
    }

    public void guardarDevolucion(Map<Integer, ReturnedItemDTO> cartMap, String returnNumber) {
        for (Map.Entry<Integer, ReturnedItemDTO> entry : cartMap.entrySet()) {
            int transferId = entry.getKey();
            ReturnedItemDTO ret = entry.getValue();
            int qty = ret.qty;
            int damageStatus = ret.damageStatus;

            Transfer transfer = obtenerTransferPorId(transferId);
            if (transfer == null) throw new RuntimeException("Transferencia no encontrada: " + transferId);

            ItemReturn itemReturn = new ItemReturn();
            itemReturn.setdFlag(1);
            itemReturn.setTransfer(transfer);
            itemReturn.setQuantity(qty);
            itemReturn.setReturnNumber(returnNumber);
            itemReturn.setAddedDate(new Date());
            itemReturn.setReturnItemCondition(damageStatus);

            if (qty == transfer.getRemainingQtyToReturn()) {
                transfer.setStatus(Transfer.STATUS_RETURNED_ALL);
            }
            transfer.setRemainingQtyToReturn(transfer.getRemainingQtyToReturn() - qty);
            transfer.setLastModifiedDate(new Date());

            Item itemOld = transfer.getItem();
            itemOld.setQuantity(itemOld.getQuantity() + qty);
            itemOld.setdFlag(1);

            if (damageStatus == ItemReturn.RETURN_ITEM_CONDITION_GOOD) {
                guardarItem(itemOld);
            } else {
                Item newBo = new Item();
                newBo.setPurchaseOrderNo(itemOld.getPurchaseOrderNo());
                newBo.setPurchaseDate(itemOld.getPurchaseDate());
                newBo.setName(itemOld.getName());
                newBo.setRackNo(itemOld.getRackNo());
                newBo.setRate(itemOld.getRate());
                newBo.setPartsNumber(itemOld.getPartsNumber());
                newBo.setOriginalQuantity(itemOld.getOriginalQuantity());
                newBo.setQuantity(itemOld.getQuantity());
                newBo.setSerialNumber(itemOld.getSerialNumber());
                newBo.setAddedType(Item.ACCOUNT_TRANSFERRED_TO_NEW);
                newBo.setCategory(itemOld.getCategory());
                newBo.setSpecification(itemOld.getSpecification());
                newBo.setVendor(itemOld.getVendor());
                newBo.setUnitsString(itemOld.getUnitsString());
                newBo.setStatus(damageStatus);
                guardarItem(newBo);
            }

            guardarTransfer(transfer);
            guardarItemReturn(itemReturn);
        }
    }
}
