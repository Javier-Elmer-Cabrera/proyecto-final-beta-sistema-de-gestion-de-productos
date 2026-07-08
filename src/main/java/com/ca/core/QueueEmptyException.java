package com.ca.core;

/**
 * Excepción personalizada para indicar que la cola está vacía.
 * Se lanza al intentar desencolar o consultar el frente de una
 * {@link CustomQueue} que no contiene elementos.
 */
public class QueueEmptyException extends RuntimeException {

    public QueueEmptyException(String message) {
        super(message);
    }

    public QueueEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
