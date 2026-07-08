package com.ca.core;

/**
 * Cola (Queue) personalizada implementada mediante nodos enlazados dinámicamente.
 * <p>
 * Sigue la política FIFO (First-In, First-Out): el primer elemento en
 * ingresar (enqueue) es el primero en salir (dequeue). Es la estructura ideal
 * para gestionar órdenes de compra pendientes, donde la prioridad es atender
 * las solicitudes en el orden cronológico exacto en que llegaron.
 * </p>
 * <p>
 * <b>Complejidad algorítmica:</b>
 * <ul>
 *   <li>enqueue() = O(1) — inserción al final</li>
 *   <li>dequeue() = O(1) — extracción del frente</li>
 *   <li>peek()    = O(1) — consulta sin extraer</li>
 * </ul>
 * </p>
 *
 * @param <T> tipo de los elementos almacenados
 */
public class CustomQueue<T> {

    /**
     * Nodo interno con enlace al siguiente nodo (puntero "siguiente").
     * La cola se mantiene con dos referencias:
     * <ul>
     *   <li><b>head (frente):</b> apunta al nodo más antiguo (primero en salir)</li>
     *   <li><b>tail (final):</b> apunta al nodo más reciente (último en llegar)</li>
     * </ul>
     */
    private static class Node<T> {
        T data;          // Dato almacenado en el nodo
        Node<T> next;    // Apuntador al siguiente nodo (null si es el último)

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    // Apunta al primer nodo (el más antiguo, por donde se desencola)
    private Node<T> head;

    // Apunta al último nodo (el más reciente, por donde se encola)
    private Node<T> tail;

    // Cantidad de elementos actualmente en la cola
    private int size;

    public CustomQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Encola (inserta) un elemento al final de la cola.
     * <p>
     * Algoritmo:
     * <ol>
     *   <li>Crear un nuevo nodo con el dato</li>
     *   <li>Si la cola está vacía, head y tail apuntan al nuevo nodo</li>
     *   <li>Si no, el next del tail actual apunta al nuevo nodo, y tail se mueve</li>
     *   <li>Incrementar size</li>
     * </ol>
     * </p>
     *
     * @param item elemento a encolar (no puede ser null)
     * @throws IllegalArgumentException si el elemento es null
     */
    // REQUISITO: Operación Encolar (enqueue) de la Cola FIFO con nodos enlazados.
    // Inserta al final (tail) con complejidad O(1). Si la cola está vacía,
    // head y tail apuntan al mismo nuevo nodo.
    public void enqueue(T item) {
        if (item == null) {
            throw new IllegalArgumentException("No se puede encolar un elemento nulo");
        }
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            // Caso especial: cola vacía, el nuevo nodo es head y tail a la vez
            head = newNode;
            tail = newNode;
        } else {
            // El tail actual apunta al nuevo nodo, y tail avanza
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Desencola (extrae) el elemento del frente de la cola.
     * <p>
     * Algoritmo:
     * <ol>
     *   <li>Si la cola está vacía, lanza QueueEmptyException</li>
     *   <li>Guardar el dato del head</li>
     *   <li>Mover head al siguiente nodo</li>
     *   <li>Si la cola quedó vacía, tail también se pone en null</li>
     *   <li>Decrementar size y retornar el dato</li>
     * </ol>
     * </p>
     *
     * @return el elemento del frente
     * @throws QueueEmptyException si la cola está vacía
     */
    // REQUISITO: Operación Desencolar (dequeue) de la Cola FIFO con nodos enlazados.
    // Extrae del frente (head) con complejidad O(1). Si la cola queda vacía,
    // tail también se establece a null para mantener la invariante.
    public T dequeue() {
        if (isEmpty()) {
            throw new QueueEmptyException("No se puede desencolar: la cola está vacía");
        }
        T data = head.data;
        head = head.next;
        if (head == null) {
            // La cola quedó vacía: tail también debe ser null
            tail = null;
        }
        size--;
        return data;
    }

    /**
     * Encola un elemento al FRENTE de la cola (no al final).
     * <p>
     * Método necesario para el patrón Undo: cuando se revierte una orden
     * procesada (undo), la orden debe regresar a su posición original
     * en el frente de la cola.
     * </p>
     * <p>
     * Algoritmo:
     * <ol>
     *   <li>Crear un nuevo nodo con el dato</li>
     *   <li>El nuevo nodo apunta al head actual</li>
     *   <li>Head se mueve al nuevo nodo</li>
     *   <li>Si la cola estaba vacía, tail también apunta al nuevo nodo</li>
     *   <li>Incrementar size</li>
     * </ol>
     * </p>
     *
     * @param item elemento a encolar al frente
     * @throws IllegalArgumentException si el elemento es null
     */
    // ACLARACIÓN: enqueueFront() inserta al FRENTE de la cola (no al final).
    // Es necesario para el patrón Undo: cuando se revierte un ProcessOrderCommand,
    // la orden debe regresar a su posición original en el frente de la cola.
    public void enqueueFront(T item) {
        if (item == null) {
            throw new IllegalArgumentException("No se puede encolar un elemento nulo");
        }
        Node<T> newNode = new Node<>(item);
        newNode.next = head;
        head = newNode;
        if (tail == null) {
            // Cola vacía: el nuevo nodo es head y tail a la vez
            tail = newNode;
        }
        size++;
    }

    /**
     * Elimina y retorna el último elemento de la cola (el del tail).
     * <p>
     * Método necesario para el patrón Undo de EnqueueOrderCommand:
     * cuando se revierte una orden encolada, se debe eliminar el
     * elemento que acabamos de agregar al final de la cola.
     * </p>
     * <p>
     * Algoritmo:
     * <ol>
     *   <li>Si la cola está vacía, lanza QueueEmptyException</li>
     *   <li>Si head == tail (un solo elemento), se vacía la cola</li>
     *   <li>Si no, se recorre desde head hasta el penúltimo nodo</li>
     *   <li>El tail se mueve al penúltimo nodo, y su next se pone en null</li>
     *   <li>Decrementar size y retornar el dato</li>
     * </ol>
     * </p>
     *
     * @return el elemento eliminado del final
     * @throws QueueEmptyException si la cola está vacía
     */
    public T removeLast() {
        if (isEmpty()) {
            throw new QueueEmptyException("No se puede eliminar: la cola está vacía");
        }
        T data = tail.data;
        if (head == tail) {
            // Un solo elemento: la cola queda vacía
            head = null;
            tail = null;
        } else {
            // Recorrer hasta encontrar el nodo anterior al tail
            Node<T> current = head;
            while (current.next != tail) {
                current = current.next;
            }
            tail = current;
            tail.next = null;
        }
        size--;
        return data;
    }

    /**
     * Consulta el elemento del frente sin extraerlo.
     *
     * @return el elemento del frente
     * @throws QueueEmptyException si la cola está vacía
     */
    public T peek() {
        if (isEmpty()) {
            throw new QueueEmptyException("No se puede consultar: la cola está vacía");
        }
        return head.data;
    }

    /**
     * Verifica si la cola está vacía.
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Retorna la cantidad de elementos en la cola.
     */
    public int size() {
        return size;
    }

    /**
     * Vacía la cola por completo.
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CustomQueue[");
        Node<T> current = head;
        while (current != null) {
            sb.append(current.data);
            current = current.next;
            if (current != null) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
