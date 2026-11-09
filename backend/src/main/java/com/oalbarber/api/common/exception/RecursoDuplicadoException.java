package com.oalbarber.api.common.exception;

/**
 * Ya existe un recurso con los mismos datos únicos (por ejemplo, el celular de un usuario).
 *
 * <p>Lleva un {@code codigo} para que el frontend distinga el motivo aunque el HTTP sea 409.</p>
 */
public class RecursoDuplicadoException extends RuntimeException {

    private final String codigo;

    public RecursoDuplicadoException(String mensaje) {
        this("RECURSO_DUPLICADO", mensaje);
    }

    public RecursoDuplicadoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
