package com.ca.core;

import com.ca.db.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Árbol Binario de Búsqueda (ABB / BST) para organizar, ordenar y buscar
 * productos (Items) en memoria RAM.
 * 
 * Criterio de ordenamiento: ID del Item ({@link Item#getId()}).
 * Los nodos con ID menor se insertan en el subárbol izquierdo; los de ID
 * mayor en el derecho. Esto garantiza que el recorrido Inorden
 * (izquierdo → raíz → derecho) devuelva los productos ordenados
 * ascendentemente por ID sin necesidad de algoritmos externos.
 * 
 * 
 * Complejidad algorítmica promedio:
 *   insert() = O(log n) — inserción recursiva comparando ID
 *   searchById() = O(log n) — búsqueda binaria por ID
 *   inOrderToList() = O(n) — recorre todo el árbol una vez
 * 
 * Manejo de duplicados: Si se intenta insertar un Item con un ID
 * que ya existe en el árbol, se lanza {@link IllegalArgumentException}.
 * La actualización de un Item existente debe hacerse mediante
 * {@link #replace(Item)}.
 * 
 */
public class BinaryProductTree {

    /** Raíz del árbol (nodo superior). Si es null, el árbol está vacío. */
    private ProductTreeNode root;

    /** Cantidad de nodos en el árbol. */
    private int size;

    public BinaryProductTree() {
        this.root = null;
        this.size = 0;
    }

    // ──────────────────────────────────────────────
    // Inserción recursiva
    // ──────────────────────────────────────────────

    /**
     * Inserta un Item en el árbol ordenado por ID.
     * 
     * Algoritmo recursivo:
     * 
     *   Caso base: si el nodo actual es null, se crea un nuevo nodo
     *       y se retorna (el padre lo enlazará como left o right).
     *   Caso recursivo izquierdo: si el ID del nuevo Item es
     *       menor que el ID del nodo actual, se inserta en el subárbol
     *       izquierdo ({@code node.left = insertRec(node.left, item)}).
     *   Caso recursivo derecho: si es mayor, se inserta en el
     *       subárbol derecho.
     *   Duplicado: si es igual, se lanza una excepción para
     *       evitar colapso del árbol.
     * 
     * 
     *
     * @param item el producto a insertar
     * @throws IllegalArgumentException si ya existe un Item con el mismo ID
     */
    // REQUISITO: Inserción en el Árbol Binario de Búsqueda (BST) usando el ID del Item como clave.
    // La inserción es recursiva: compara el ID del nuevo Item contra el nodo actual y decide
    // si va al subárbol izquierdo (menor) o derecho (mayor). Complejidad O(log n) promedio.
    public void insert(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("No se puede insertar un Item nulo");
        }
        root = insertRec(root, item);
        size++;
    }

    /**
     * Método recursivo privado que recorre el árbol hasta encontrar
     * la posición correcta para el nuevo nodo.
     *
     * @param node nodo actual en la recursión
     * @param item producto a insertar
     * @return el nodo (nuevo o actualizado) para que el padre lo enlace
     */
    private ProductTreeNode insertRec(ProductTreeNode node, Item item) {
        // CASO BASE: llegamos a un hueco donde insertar
        if (node == null) {
            return new ProductTreeNode(item);
        }

        // Comparar por ID para decidir dirección
        int cmp = Integer.compare(item.getId(), node.item.getId());

        if (cmp < 0) {
            // El nuevo ID es menor → insertar en el subárbol izquierdo
            node.left = insertRec(node.left, item);
        } else if (cmp > 0) {
            // El nuevo ID es mayor → insertar en el subárbol derecho
            node.right = insertRec(node.right, item);
        } else {
            // ID duplicado: el árbol no puede tener dos nodos con el mismo ID
            throw new IllegalArgumentException(
                    "Ya existe un producto con ID " + item.getId() + ": " + node.item.getName());
        }

        return node;
    }

    // ──────────────────────────────────────────────
    // Búsqueda binaria por ID
    // ──────────────────────────────────────────────

    /**
     * Busca un Item por su ID utilizando la propiedad de orden del BST.
     * 
     * Algoritmo:
     * 
     *   Si el nodo actual es null → no encontrado (null)
     *   Si el ID buscado es menor que el ID del nodo actual →
     *       buscar en el subárbol izquierdo
     *   Si es mayor → buscar en el subárbol derecho
     *   Si es igual → encontrado, retornar el Item
     * 
     * 
     *
     * @param id el ID a buscar
     * @return el Item encontrado, o null si no existe
     */
    // REQUISITO: Búsqueda binaria por ID en el BST. Aprovecha la propiedad de orden del árbol
    // para descartar la mitad de los nodos en cada nivel. Complejidad O(log n) promedio.
    public Item searchById(int id) {
        return searchRec(root, id);
    }

    private Item searchRec(ProductTreeNode node, int id) {
        // CASO BASE: el ID no está en el árbol
        if (node == null) {
            return null;
        }

        int cmp = Integer.compare(id, node.item.getId());

        if (cmp < 0) {
            // El ID buscado es menor → buscar a la izquierda
            return searchRec(node.left, id);
        } else if (cmp > 0) {
            // El ID buscado es mayor → buscar a la derecha
            return searchRec(node.right, id);
        } else {
            // Encontrado
            return node.item;
        }
    }

    // ──────────────────────────────────────────────
    // Reemplazar (update) un Item existente
    // ──────────────────────────────────────────────

    /**
     * Reemplaza (actualiza) un Item existente identificado por su ID.
     * Si el ID no existe en el árbol, lo inserta como nuevo.
     *
     * @param item el Item con los datos actualizados
     */
    public void replace(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("No se puede reemplazar con un Item nulo");
        }
        ProductTreeNode found = findNodeRec(root, item.getId());
        if (found != null) {
            // Actualizar la referencia del nodo existente
            found.item = item;
        } else {
            // No existe: insertar como nuevo
            insert(item);
        }
    }

    /**
     * Busca el nodo que contiene un ID específico, retornando el nodo mismo
     * (no solo el Item). Útil para replace().
     */
    private ProductTreeNode findNodeRec(ProductTreeNode node, int id) {
        if (node == null) return null;
        int cmp = Integer.compare(id, node.item.getId());
        if (cmp < 0) return findNodeRec(node.left, id);
        if (cmp > 0) return findNodeRec(node.right, id);
        return node;
    }

    // ──────────────────────────────────────────────
    // Soft-delete por ID
    // ──────────────────────────────────────────────

    /**
     * Marca un Item como inactivo (dFlag = 0) sin removerlo del árbol.
     * Esto preserva la estructura del BST para futuras búsquedas.
     *
     * @param id el ID del Item a desactivar
     */
    public void deleteById(int id) {
        Item found = searchById(id);
        if (found != null) {
            found.setdFlag(0);
        }
    }

    // ──────────────────────────────────────────────
    // Recorrido Inorden (izquierdo → raíz → derecho)
    // ──────────────────────────────────────────────

    /**
     * Realiza un recorrido Inorden recursivo y retorna la lista
     * de Items ordenada ascendentemente por ID.
     * 
     * El recorrido Inorden visita:
     * 
     *   Subárbol izquierdo (IDs menores)
     *   Raíz (ID actual)
     *   Subárbol derecho (IDs mayores)
     * 
     * Esto produce automáticamente una secuencia ordenada sin necesidad
     * de algoritmos de ordenamiento adicionales como BubbleSort o QuickSort.
     * 
     *
     * @return lista de Items en orden ascendente por ID
     */
    // REQUISITO: Recorrido Inorden (izquierdo → raíz → derecho) del BST.
    // Este recorrido devuelve los Items ordenados ascendentemente por ID sin necesidad
    // de algoritmos de ordenamiento externos como BubbleSort o QuickSort.
    // Complejidad: O(n) pues visita cada nodo una sola vez.
    public List<Item> inOrderToList() {
        List<Item> result = new ArrayList<>();
        inOrderRec(root, result);
        return result;
    }

    /**
     * Método recursivo que recorre el árbol en Inorden.
     *
     * @param node   nodo actual de la recursión
     * @param result lista acumuladora (se modifica en cada llamada)
     */
    private void inOrderRec(ProductTreeNode node, List<Item> result) {
        if (node == null) {
            return; // CASO BASE: nodo vacío, terminar la rama
        }
        // 1. Visitar subárbol izquierdo (IDs menores)
        inOrderRec(node.left, result);
        // 2. Visitar la raíz (nodo actual)
        result.add(node.item);
        // 3. Visitar subárbol derecho (IDs mayores)
        inOrderRec(node.right, result);
    }

    // ──────────────────────────────────────────────
    // Consultas de listado
    // ──────────────────────────────────────────────

    /**
     * Retorna todos los Items activos (dFlag = 1) ordenados por ID,
     * extrayéndolos mediante el recorrido Inorden.
     *
     * @return lista ordenada de Items activos
     */
    public List<Item> getAllActive() {
        List<Item> all = inOrderToList();
        List<Item> active = new ArrayList<>();
        for (Item i : all) {
            if (i.getdFlag() == 1) {
                active.add(i);
            }
        }
        return active;
    }

    /**
     * Retorna todos los Items (activos e inactivos) ordenados por ID.
     *
     * @return lista ordenada completa
     */
    public List<Item> getAll() {
        return inOrderToList();
    }

    // ──────────────────────────────────────────────
    // Búsqueda por nombre (recorrido completo)
    // ──────────────────────────────────────────────

    /**
     * Busca un Item por su nombre (coincidencia exacta, case-insensitive).
     * Dado que el árbol está ordenado por ID (no por nombre), esta
     * operación requiere un recorrido O(n).
     *
     * @param name el nombre a buscar
     * @return el primer Item que coincida, o null si no existe
     */
    public Item searchByName(String name) {
        return searchByNameRec(root, name);
    }

    private Item searchByNameRec(ProductTreeNode node, String name) {
        if (node == null) return null;
        // Buscar en inorden para mantener orden
        Item leftResult = searchByNameRec(node.left, name);
        if (leftResult != null) return leftResult;
        if (node.item.getName() != null && node.item.getName().equalsIgnoreCase(name)) {
            return node.item;
        }
        return searchByNameRec(node.right, name);
    }

    // ──────────────────────────────────────────────
    // Métodos auxiliares
    // ──────────────────────────────────────────────

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public String toString() {
        List<Item> items = getAllActive();
        return "BinaryProductTree{size=" + size + ", active=" + items.size() + "}";
    }
}
