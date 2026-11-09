package com.oalbarber.api.common.exception;

/**
 * Se incumplió una regla de negocio (anticipación, cancelación, estado de una cita, etc.).
 */
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String mensaje) {
        this("REGLA_NEGOCIO", mensaje);
    }

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
