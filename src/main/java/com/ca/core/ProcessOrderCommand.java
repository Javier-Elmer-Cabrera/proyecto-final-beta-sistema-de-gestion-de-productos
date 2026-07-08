package com.ca.core;

import com.ca.db.model.Item;

/**
 * Comando concreto que encapsula la acción de procesar una orden de compra.
 * <p>
 * Al ejecutarse ({@link #execute()}), desencola la orden del frente de la
 * cola FIFO ({@link CustomQueue}) y actualiza el stock del producto.
 * Al deshacerse ({@link #undo()}), restaura el stock al valor anterior y
 * vuelve a encolar la orden al frente de la cola (simulando que nunca
 * se procesó).
 * </p>
 *
 * <p>
 * <b>Guardado de estado:</b> Antes de modificar el stock, el comando
 * captura la cantidad anterior del item para poder restaurarla en undo().
 * La orden se guarda en el momento de ejecutar ({@link #execute()}), no
 * en el constructor, porque el desencolado ocurre dentro del execute().
 * </p>
 */
public class ProcessOrderCommand implements Command {

    /** Referencia a la cola de órdenes para desencolar y re-encolar. */
    private final CustomQueue<PurchaseOrder> pendingOrders;

    /** La orden que se desencoló al ejecutar (se asigna en execute()). */
    private PurchaseOrder order;

    /** El item cuyo stock se modificó al procesar la orden. */
    private Item item;

    /** Cantidad que tenía el item antes de procesar la orden. */
    private int previousStock;

    /** Cantidad que se agregó al stock (la cantidad de la orden). */
    private int orderedQuantity;

    /**
     * Crea un comando listo para procesar la siguiente orden de la cola.
     *
     * @param pendingOrders la cola FIFO de órdenes pendientes
     */
    public ProcessOrderCommand(CustomQueue<PurchaseOrder> pendingOrders) {
        this.pendingOrders = pendingOrders;
        this.order = null;
        this.item = null;
        this.previousStock = 0;
        this.orderedQuantity = 0;
    }

    /**
     * Ejecuta el procesamiento de la orden:
     * <ol>
     *   <li>Desencola la orden del frente de la cola (dequeue)</li>
     *   <li>Guarda el stock anterior del item</li>
     *   <li>Suma la cantidad de la orden al stock del item</li>
     *   <li>Marca la orden como COMPLETED</li>
     * </ol>
     */
    // REQUISITO: Desencola (dequeue) del frente de la CustomQueue FIFO (O(1)).
    // Guarda el estado previo del stock para poder restaurarlo en undo().
    @Override
    public void execute() {
        // Desencolar la orden del frente de la cola FIFO
        this.order = pendingOrders.dequeue();

        // Guardar referencias y estado previo para poder revertir en undo()
        this.item = order.getItem();
        this.orderedQuantity = order.getQuantity();
        if (this.item != null) {
            // Guardar el stock ANTES de modificarlo
            this.previousStock = this.item.getQuantity();
            // Sumar la cantidad comprada al inventario
            this.item.setQuantity(previousStock + orderedQuantity);
        }

        // Marcar la orden como completada
        order.setStatus("COMPLETED");
    }

    /**
     * Revierte el procesamiento de la orden:
     * <ol>
     *   <li>Restaura el stock del item a su valor anterior</li>
     *   <li>Vuelve a encolar la orden al FRENTE de la cola
     *       (no al final, porque esa era su posición original)</li>
     *   <li>Marca la orden como PENDING nuevamente</li>
     * </ol>
     */
    // REQUISITO: Revierte dequeue() restaurando el stock y re-encolando al frente (enqueueFront).
    @Override
    public void undo() {
        // Restaurar el stock al valor previo al procesamiento
        if (item != null) {
            item.setQuantity(previousStock);
        }

        // Re-encolar la orden al frente de la cola (posición original)
        order.setStatus("PENDING");
        pendingOrders.enqueueFront(order);
    }

    /**
     * Retorna la orden que fue procesada (después de ejecutar execute()).
     * Útil para que la UI muestre información de la orden al usuario.
     *
     * @return la orden procesada, o null si aún no se ejecutó
     */
    public PurchaseOrder getOrder() {
        return order;
    }
}
