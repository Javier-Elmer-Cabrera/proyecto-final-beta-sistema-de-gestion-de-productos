package com.ca.core;

import com.ca.db.model.Item;
import com.ca.db.model.Vendor;

import java.util.Date;

/**
 * Modelo de Órden de Compra utilizado por la Queue de órdenes pendientes.
 *
 * Cada PurchaseOrder representa una solicitud de compra de un producto a un
 * proveedor. Las órdenes se procesan en estricto orden FIFO (First In, First
 * Out) mediante una CustomQueue (cola basada en nodos enlazados), lo que garantiza que los pedidos más
 * antiguos se atienden primero.
 */
public class PurchaseOrder {

    private int id;
    private Item item;
    private int quantity;
    private Vendor vendor;
    private Date orderDate;
    /** Pendiente, Completada, Cancelada */
    private String status;
    private String notes;

    public PurchaseOrder() {
    }

    public PurchaseOrder(Item item, int quantity, Vendor vendor) {
        this.item = item;
        this.quantity = quantity;
        this.vendor = vendor;
        this.orderDate = new Date();
        this.status = "PENDING";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "PurchaseOrder{id=" + id + ", item=" + (item != null ? item.getName() : "null")
                + ", qty=" + quantity + ", status='" + status + "'}";
    }
}
