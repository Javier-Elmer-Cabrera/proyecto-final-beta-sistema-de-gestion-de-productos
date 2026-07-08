package com.ca.core;

/**
 * Interfaz base del patrón Command.
 * <p>
 * Define las dos operaciones fundamentales que todo comando debe soportar:
 * <ul>
 *   <li><b>execute():</b> ejecuta la acción (ej: procesar una orden de compra)</li>
 *   <li><b>undo():</b> revierte la acción, restaurando el estado anterior
 *       (ej: devolver la orden a la cola y restaurar el stock)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Esta interfaz permite que el DataEngine maneje cualquier tipo de comando
 * de forma polimórfica, apilándolo en la pila de Undo después de ejecutarlo.
 * </p>
 */
// REQUISITO: Patrón Command para encapsular operaciones como objetos.
// Permite que DataEngine ejecute, deshaga (undo) y rehaga (redo) acciones
// de forma polimórfica usando las pilas CustomStack (LIFO).
public interface Command {

    /**
     * Ejecuta la acción del comando.
     */
    void execute();

    /**
     * Revierte la acción, restaurando el estado previo.
     */
    void undo();
}
