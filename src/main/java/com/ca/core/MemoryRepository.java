package com.ca.core;

import com.ca.db.model.*;
import com.ca.db.service.dto.ReturnedItemDTO;
import com.ca.db.model.ReceiverType;
import com.gt.common.utils.PasswordUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Fachada (Facade) hacia DataEngine. Mantiene la misma API pública que
 * tenia en la Etapa 2 para que los servicios (DBUtils, ItemServiceImpl, etc.)
 * no requieran cambios. Internamente delega todas las operaciones al
 * {@link DataEngine} que utiliza Queue, Stack y BinaryProductTree.
 */
public final class MemoryRepository {

    private static MemoryRepository instance;

    private MemoryRepository() {
    }

    public static synchronized MemoryRepository getInstance() {
        if (instance == null) {
            instance = new MemoryRepository();
            DataEngine.getInstance().cargarDatosPrueba();
        }
        return instance;
    }

    private DataEngine engine() {
        return DataEngine.getInstance();
    }

    // ── LoginUser ──

    public boolean userExists() {
        return engine().existeUsuario();
    }

    public LoginUser getLoginUser(String userName, String password) {
        return engine().login(userName, password);
    }

    public void changeLogin(String usrName, String password) {
        engine().cambiarLogin(usrName, password);
    }

    public void saveLoginUser(LoginUser user) {
        engine().guardarLoginUser(user);
    }

    // ── ApplicationLog ──

    public void saveLog(ApplicationLog log) {
        engine().guardarLog(log);
    }

    // ── BranchOffice ──

    public List<BranchOffice> readAllBranchOffices() {
        return engine().obtenerTodasBranchOffices();
    }

    public List<BranchOffice> readAllBranchOfficesNoStatus() {
        return engine().obtenerTodasBranchOfficesNoStatus();
    }

    public BranchOffice getBranchOfficeById(int id) {
        return engine().obtenerBranchOfficePorId(id);
    }

    public BranchOffice getBranchOfficeByIdNoStatus(int id) {
        return engine().obtenerBranchOfficePorIdNoStatus(id);
    }

    public void saveBranchOffice(BranchOffice office) {
        engine().guardarBranchOffice(office);
    }

    public void deleteBranchOffice(int id) {
        engine().eliminarBranchOffice(id);
    }

    // ── Vendor ──

    public List<Vendor> readAllVendors() {
        return engine().obtenerTodosVendors();
    }

    public List<Vendor> readAllVendorsNoStatus() {
        return engine().obtenerTodosVendorsNoStatus();
    }

    public Vendor getVendorById(int id) {
        return engine().obtenerVendorPorId(id);
    }

    public Vendor getVendorByIdNoStatus(int id) {
        return engine().obtenerVendorPorIdNoStatus(id);
    }

    public void saveVendor(Vendor vendor) {
        engine().guardarVendor(vendor);
    }

    public void deleteVendor(int id) {
        engine().eliminarVendor(id);
    }

    // ── Category ──

    public List<Category> readAllCategories() {
        return engine().obtenerTodasCategorias();
    }

    public List<Category> readAllCategoriesNoStatus() {
        return engine().obtenerTodasCategoriasNoStatus();
    }

    public Category getCategoryById(int id) {
        return engine().obtenerCategoriaPorId(id);
    }

    public Category getCategoryByIdNoStatus(int id) {
        return engine().obtenerCategoriaPorIdNoStatus(id);
    }

    public void saveCategory(Category category) {
        engine().guardarCategoria(category);
    }

    public void deleteCategory(int id) {
        engine().eliminarCategoria(id);
    }

    // ── UnitsString ──

    public List<UnitsString> readAllUnitsStrings() {
        return engine().obtenerTodasUnitsStrings();
    }

    public UnitsString getUnitsStringById(int id) {
        return engine().obtenerUnitsStringPorId(id);
    }

    public void saveUnitsString(UnitsString unitsString) {
        engine().guardarUnitsString(unitsString);
    }

    public void deleteUnitsString(int id) {
        engine().eliminarUnitsString(id);
    }

    // ── Specification ──

    public List<Specification> readAllSpecifications() {
        return engine().obtenerTodasSpecifications();
    }

    public Specification getSpecificationById(int id) {
        return engine().obtenerSpecificationPorId(id);
    }

    public void saveSpecification(Specification specification) {
        engine().guardarSpecification(specification);
    }

    public void deleteSpecification(int id) {
        engine().eliminarSpecification(id);
    }

    // ── Item (products via BinaryProductTree) ──

    public List<Item> readAllItems() {
        return engine().obtenerTodosItems();
    }

    public Item getItemById(int id) {
        return engine().obtenerItemPorId(id);
    }

    public Item getItemByName(String name) {
        return engine().obtenerItemPorNombre(name);
    }

    public void saveItem(Item item) {
        engine().guardarItem(item);
    }

    public void deleteItem(int id) {
        engine().eliminarItem(id);
    }

    // ── Transfer ──

    public List<Transfer> readAllTransfers() {
        return engine().obtenerTodasTransfers();
    }

    public Transfer getTransferById(int id) {
        return engine().obtenerTransferPorId(id);
    }

    public void saveTransfer(Transfer transfer) {
        engine().guardarTransfer(transfer);
    }

    // ── ItemReturn ──

    public List<ItemReturn> readAllItemReturns() {
        return engine().obtenerTodosItemReturns();
    }

    public ItemReturn getItemReturnById(int id) {
        return engine().obtenerItemReturnPorId(id);
    }

    public void saveItemReturn(ItemReturn itemReturn) {
        engine().guardarItemReturn(itemReturn);
    }

    // ── CRUD genérico (usado por DBUtils.java) ──

    @SuppressWarnings("unchecked")
    public <T> List<T> readAll(Class<T> clazz) {
        if (clazz == LoginUser.class) return (List<T>) engine().obtenerTodosLoginUsers();
        if (clazz == ApplicationLog.class) return (List<T>) engine().obtenerTodosLogs();
        if (clazz == BranchOffice.class) return (List<T>) engine().obtenerTodasBranchOffices();
        if (clazz == Vendor.class) return (List<T>) engine().obtenerTodosVendors();
        if (clazz == Category.class) return (List<T>) engine().obtenerTodasCategorias();
        if (clazz == UnitsString.class) return (List<T>) engine().obtenerTodasUnitsStrings();
        if (clazz == Specification.class) return (List<T>) engine().obtenerTodasSpecifications();
        if (clazz == Item.class) return (List<T>) engine().obtenerTodosItems();
        if (clazz == Transfer.class) return (List<T>) engine().obtenerTodasTransfers();
        if (clazz == ItemReturn.class) return (List<T>) engine().obtenerTodosItemReturns();
        throw new IllegalArgumentException("Tipo no soportado: " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> readAllNoStatus(Class<T> clazz) {
        if (clazz == LoginUser.class) return (List<T>) engine().obtenerTodosLoginUsers();
        if (clazz == BranchOffice.class) return (List<T>) engine().obtenerTodasBranchOfficesNoStatus();
        if (clazz == Vendor.class) return (List<T>) engine().obtenerTodosVendorsNoStatus();
        if (clazz == Category.class) return (List<T>) engine().obtenerTodasCategoriasNoStatus();
        if (clazz == Item.class) return (List<T>) engine().obtenerTodosItems();
        if (clazz == Transfer.class) return (List<T>) engine().obtenerTodasTransfers();
        if (clazz == ItemReturn.class) return (List<T>) engine().obtenerTodosItemReturns();
        return readAll(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T getById(Class<T> clazz, int id) {
        if (clazz == LoginUser.class) return (T) engine().obtenerLoginUserPorId(id);
        if (clazz == BranchOffice.class) return (T) engine().obtenerBranchOfficePorId(id);
        if (clazz == Vendor.class) return (T) engine().obtenerVendorPorId(id);
        if (clazz == Category.class) return (T) engine().obtenerCategoriaPorId(id);
        if (clazz == UnitsString.class) return (T) engine().obtenerUnitsStringPorId(id);
        if (clazz == Specification.class) return (T) engine().obtenerSpecificationPorId(id);
        if (clazz == Item.class) return (T) engine().obtenerItemPorId(id);
        if (clazz == Transfer.class) return (T) engine().obtenerTransferPorId(id);
        if (clazz == ItemReturn.class) return (T) engine().obtenerItemReturnPorId(id);
        throw new IllegalArgumentException("Tipo no soportado: " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> T getByIdNoStatus(Class<T> clazz, int id) {
        if (clazz == LoginUser.class) return (T) engine().obtenerLoginUserPorId(id);
        if (clazz == BranchOffice.class) return (T) engine().obtenerBranchOfficePorIdNoStatus(id);
        if (clazz == Vendor.class) return (T) engine().obtenerVendorPorIdNoStatus(id);
        if (clazz == Category.class) return (T) engine().obtenerCategoriaPorIdNoStatus(id);
        if (clazz == Item.class) return (T) engine().obtenerItemPorId(id);
        if (clazz == Transfer.class) return (T) engine().obtenerTransferPorId(id);
        if (clazz == ItemReturn.class) return (T) engine().obtenerItemReturnPorId(id);
        return getById(clazz, id);
    }

    @SuppressWarnings("unchecked")
    public <T> void saveOrUpdate(T entity) {
        if (entity instanceof LoginUser) engine().guardarLoginUser((LoginUser) entity);
        else if (entity instanceof ApplicationLog) engine().guardarLog((ApplicationLog) entity);
        else if (entity instanceof BranchOffice) engine().guardarBranchOffice((BranchOffice) entity);
        else if (entity instanceof Vendor) engine().guardarVendor((Vendor) entity);
        else if (entity instanceof Category) engine().guardarCategoria((Category) entity);
        else if (entity instanceof UnitsString) engine().guardarUnitsString((UnitsString) entity);
        else if (entity instanceof Specification) engine().guardarSpecification((Specification) entity);
        else if (entity instanceof Item) engine().guardarItem((Item) entity);
        else if (entity instanceof Transfer) engine().guardarTransfer((Transfer) entity);
        else if (entity instanceof ItemReturn) engine().guardarItemReturn((ItemReturn) entity);
        else throw new IllegalArgumentException("Tipo no soportado: " + entity.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public <T> void deleteById(Class<T> clazz, int id) {
        if (clazz == LoginUser.class) engine().eliminarLoginUser(id);
        else if (clazz == BranchOffice.class) engine().eliminarBranchOffice(id);
        else if (clazz == Vendor.class) engine().eliminarVendor(id);
        else if (clazz == Category.class) engine().eliminarCategoria(id);
        else if (clazz == UnitsString.class) engine().eliminarUnitsString(id);
        else if (clazz == Specification.class) engine().eliminarSpecification(id);
        else if (clazz == Item.class) engine().eliminarItem(id);
        else if (clazz == Transfer.class) engine().eliminarTransfer(id);
    }

    // ── Lógica de negocio delegada ──

    public List<Item> itemStockQuery(String itemName, int categoryId, int vendorId,
                                     String rackNumber, Date fromDate, Date toDate, List<String> specs) {
        return engine().consultarStock(itemName, categoryId, vendorId, rackNumber, fromDate, toDate, specs);
    }

    // VINCULACIÓN: Obtiene los Items nuevos a través del DataEngine,
    // que internamente realiza un recorrido Inorden sobre el BinaryProductTree.
    public List<Item> getAddedItems() {
        return engine().obtenerItemsNuevos();
    }

    public void saveTransfer(Map<Integer, Integer> cartMap, Date transferDate,
                             ReceiverType type, int id, String requestNumber) {
        engine().guardarTransferencia(cartMap, transferDate, type, id, requestNumber);
    }

    public List<Transfer> notReturnedTransferItemQuery(String itemName, int categoryId, int receiverId,
                                                       int returnedStatus, int receiveStatus,
                                                       String requestNumber, Date fromDate, Date toDate) {
        return engine().consultarTransferenciasNoRetornadas(itemName, categoryId, receiverId,
                returnedStatus, receiveStatus, requestNumber, fromDate, toDate);
    }

    public List<Category> getNonReturnableCategory() {
        return engine().obtenerCategoriasRetornables();
    }

    public void saveReturnedItem(Map<Integer, ReturnedItemDTO> cartMap, String returnNumber) {
        engine().guardarDevolucion(cartMap, returnNumber);
    }

    public BinaryProductTree getProductTree() {
        return engine().getProductTree();
    }

    public CustomQueue<PurchaseOrder> getPendingOrders() {
        return engine().getPendingOrders();
    }

    // ── Undo/Redo (Stack-based history) ──

    // VINCULACIÓN: Ejecuta un comando en DataEngine, que lo apila en undoStack (CustomStack LIFO).
    public void executeCommand(Command command) {
        engine().executeCommand(command);
    }

    public void undo() {
        engine().undo();
    }

    public void redo() {
        engine().redo();
    }

    public boolean canUndo() {
        return engine().canUndo();
    }

    public boolean canRedo() {
        return engine().canRedo();
    }
}
