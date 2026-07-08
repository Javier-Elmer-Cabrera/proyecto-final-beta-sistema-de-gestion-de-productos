package com.ca.core;

/**
 * Excepción personalizada para indicar que la pila está vacía.
 * Se lanza al intentar desapilar (pop) o consultar (peek) la cima
 * de una {@link CustomStack} que no contiene elementos.
 */
public class StackEmptyException extends RuntimeException {

    public StackEmptyException(String message) {
        super(message);
    }

    public StackEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
