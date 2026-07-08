package com.ca.core;

public class EnqueueOrderCommand implements Command {

    private final CustomQueue<PurchaseOrder> pendingOrders;
    private final PurchaseOrder order;

    public EnqueueOrderCommand(CustomQueue<PurchaseOrder> pendingOrders, PurchaseOrder order) {
        this.pendingOrders = pendingOrders;
        this.order = order;
    }

    // REQUISITO: Ejecuta enqueue() sobre la CustomQueue FIFO (O(1)).
    @Override
    public void execute() {
        pendingOrders.enqueue(order);
    }

    // REQUISITO: Revierte enqueue() eliminando el último elemento de la cola (removeLast).
    @Override
    public void undo() {
        PurchaseOrder removed = pendingOrders.removeLast();
        if (removed != order) {
            pendingOrders.enqueue(removed);
        }
    }

    public PurchaseOrder getOrder() {
        return order;
    }
}
