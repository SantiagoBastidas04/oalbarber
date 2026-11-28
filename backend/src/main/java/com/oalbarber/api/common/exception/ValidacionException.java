package com.oalbarber.api.common.exception;

import java.util.Map;

/**
 * Error de validación de negocio con el detalle por campo o por fila.
 * Se traduce a un 400 con el mapa {@code errors}, igual que las validaciones
 * de Bean Validation, para que el frontend marque cada día del horario.
 */
public class ValidacionException extends RuntimeException {

    private final String codigo;
    private final Map<String, String> errores;

    public ValidacionException(String codigo, String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.codigo = codigo;
        this.errores = errores;
    }

    public String getCodigo() {
        return codigo;
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
