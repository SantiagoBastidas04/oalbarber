package com.oalbarber.api.common.exception;

/**
 * El recurso solicitado no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
