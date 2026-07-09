package com.ca.core;

/**
 * Cola (Queue) personalizada implementada mediante nodos enlazados dinámicamente.
 * 
 * Sigue la política FIFO (First-In, First-Out): el primer elemento en
 * ingresar (enqueue) es el primero en salir (dequeue). Es la estructura ideal
 * para gestionar órdenes de compra pendientes, donde la prioridad es atender
 * las solicitudes en el orden cronológico exacto en que llegaron.
 * 
 * 
 * Complejidad algorítmica:
 * 
 *   enqueue() = O(1) — inserción al final
 *   dequeue() = O(1) — extracción del frente
 *   peek()    = O(1) — consulta sin extraer
 * 
 * 
 *
 * @param <T> tipo de los elementos almacenados
 */
public class CustomQueue<T> {

    /**
     * Nodo interno con enlace al siguiente nodo (puntero "siguiente").
     * La cola se mantiene con dos referencias:
     * 
     *   head (frente): apunta al nodo más antiguo (primero en salir)
     *   tail (final): apunta al nodo más reciente (último en llegar)
     * 
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
     * 
     * Algoritmo:
     * <ol>
     *   Crear un nuevo nodo con el dato
     *   Si la cola está vacía, head y tail apuntan al nuevo nodo
     *   Si no, el next del tail actual apunta al nuevo nodo, y tail se mueve
     *   Incrementar size
     * </ol>
     * 
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
     * 
     * Algoritmo:
     * <ol>
     *   Si la cola está vacía, lanza QueueEmptyException
     *   Guardar el dato del head
     *   Mover head al siguiente nodo
     *   Si la cola quedó vacía, tail también se pone en null
     *   Decrementar size y retornar el dato
     * </ol>
     * 
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
     * 
     * Método necesario para el patrón Undo: cuando se revierte una orden
     * procesada (undo), la orden debe regresar a su posición original
     * en el frente de la cola.
     * 
     * 
     * Algoritmo:
     * <ol>
     *   Crear un nuevo nodo con el dato
     *   El nuevo nodo apunta al head actual
     *   Head se mueve al nuevo nodo
     *   Si la cola estaba vacía, tail también apunta al nuevo nodo
     *   Incrementar size
     * </ol>
     * 
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
     * 
     * Método necesario para el patrón Undo de EnqueueOrderCommand:
     * cuando se revierte una orden encolada, se debe eliminar el
     * elemento que acabamos de agregar al final de la cola.
     * 
     * 
     * Algoritmo:
     * <ol>
     *   Si la cola está vacía, lanza QueueEmptyException
     *   Si head == tail (un solo elemento), se vacía la cola
     *   Si no, se recorre desde head hasta el penúltimo nodo
     *   El tail se mueve al penúltimo nodo, y su next se pone en null
     *   Decrementar size y retornar el dato
     * </ol>
     * 
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
