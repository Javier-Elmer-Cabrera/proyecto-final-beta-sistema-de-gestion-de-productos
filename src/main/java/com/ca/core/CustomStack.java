package com.ca.core;

/**
 * Pila (Stack) personalizada implementada mediante nodos enlazados dinámicamente.
 * <p>
 * Sigue la política LIFO (Last-In, First-Out): el último elemento en apilarse
 * (push) es el primero en desapilarse (pop). Es la estructura ideal para
 * el historial Undo/Redo, donde la acción más reciente debe poder deshacerse
 * primero.
 * </p>
 * <p>
 * <b>Operaciones fundamentales:</b>
 * <ul>
 *   <li>push() = O(1) — apilar en la cima</li>
 *   <li>pop()  = O(1) — desapilar desde la cima</li>
 *   <li>peek() = O(1) — consultar la cima sin extraer</li>
 * </ul>
 * </p>
 *
 * @param <T> tipo de los elementos almacenados
 */
public class CustomStack<T> {

    /**
     * Nodo interno con enlace al nodo inferior (hacia abajo en la pila).
     * La pila solo mantiene una referencia a la cima (top), y cada nodo
     * apunta al que está debajo de él.
     */
    private static class Node<T> {
        T data;          // Dato almacenado en el nodo
        Node<T> below;   // Apuntador al nodo inferior (debajo de este)

        Node(T data) {
            this.data = data;
            this.below = null;
        }
    }

    // Apunta al nodo en la cima de la pila (el más reciente)
    private Node<T> top;

    // Cantidad de elementos en la pila
    private int size;

    public CustomStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Apila (inserta) un elemento en la cima de la pila.
     * <p>
     * Algoritmo:
     * <ol>
     *   <li>Crear un nuevo nodo con el dato</li>
     *   <li>El nuevo nodo apunta (below) al antiguo top</li>
     *   <li>Top se mueve al nuevo nodo</li>
     *   <li>Incrementar size</li>
     * </ol>
     * </p>
     *
     * @param item elemento a apilar (no puede ser null)
     * @throws IllegalArgumentException si el elemento es null
     */
    // REQUISITO: Operación Apilar (push) de la Pila LIFO con nodos enlazados.
    // Inserta en la cima (top) con complejidad O(1). El nuevo nodo apunta (below)
    // al nodo que antes era la cima.
    public void push(T item) {
        if (item == null) {
            throw new IllegalArgumentException("No se puede apilar un elemento nulo");
        }
        Node<T> newNode = new Node<>(item);
        // El nuevo nodo apunta hacia abajo, al nodo que era la cima
        newNode.below = top;
        // La cima avanza al nuevo nodo
        top = newNode;
        size++;
    }

    /**
     * Desapila (extrae) el elemento de la cima de la pila.
     * <p>
     * Algoritmo:
     * <ol>
     *   <li>Si la pila está vacía, lanza StackEmptyException</li>
     *   <li>Guardar el dato del top</li>
     *   <li>Mover top al nodo inferior (below)</li>
     *   <li>Decrementar size y retornar el dato</li>
     * </ol>
     * </p>
     *
     * @return el elemento de la cima
     * @throws StackEmptyException si la pila está vacía
     */
    // REQUISITO: Operación Desapilar (pop) de la Pila LIFO con nodos enlazados.
    // Extrae de la cima (top) con complejidad O(1). Top se mueve al nodo inferior (below).
    public T pop() {
        if (isEmpty()) {
            throw new StackEmptyException("No se puede desapilar: la pila está vacía");
        }
        T data = top.data;
        // La cima se mueve al nodo que está debajo
        top = top.below;
        size--;
        return data;
    }

    /**
     * Consulta el elemento de la cima sin extraerlo.
     *
     * @return el elemento de la cima
     * @throws StackEmptyException si la pila está vacía
     */
    public T peek() {
        if (isEmpty()) {
            throw new StackEmptyException("No se puede consultar: la pila está vacía");
        }
        return top.data;
    }

    /**
     * Verifica si la pila está vacía.
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Retorna la cantidad de elementos en la pila.
     */
    public int size() {
        return size;
    }

    /**
     * Vacía la pila por completo.
     */
    public void clear() {
        top = null;
        size = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CustomStack[");
        Node<T> current = top;
        while (current != null) {
            sb.append(current.data);
            current = current.below;
            if (current != null) sb.append(", ");
        }
        sb.append("] (top)");
        return sb.toString();
    }
}
