package com.ca.core;

import com.ca.db.model.Item;

/**
 * Nodo del Árbol Binario de Búsqueda (BST) que envuelve un producto (Item).
 * <p>
 * Cada nodo contiene:
 * <ul>
 *   <li><b>producto (Item):</b> el dato del producto almacenado</li>
 *   <li><b>left (hijo izquierdo):</b> subárbol con productos de ID menor</li>
 *   <li><b>right (hijo derecho):</b> subárbol con productos de ID mayor</li>
 * </ul>
 * </p>
 *
 * <p>
 * El criterio de ordenamiento es el ID del Item ({@link Item#getId()}).
 * Los productos con ID menor van a la izquierda, los de ID mayor a la derecha.
 * </p>
 */
public class ProductTreeNode {

    /** El producto almacenado en este nodo. */
    public Item item;

    /** Hijo izquierdo: contiene productos con ID menor al de este nodo. */
    public ProductTreeNode left;

    /** Hijo derecho: contiene productos con ID mayor al de este nodo. */
    public ProductTreeNode right;

    /**
     * Construye un nodo hoja (sin hijos).
     *
     * @param item el producto a almacenar
     */
    public ProductTreeNode(Item item) {
        this.item = item;
        this.left = null;
        this.right = null;
    }
}
