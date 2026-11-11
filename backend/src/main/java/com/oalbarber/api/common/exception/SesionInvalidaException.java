package com.oalbarber.api.common.exception;

import lombok.Getter;

/**
 * La sesión de refresco no sirve: desconocida, vencida o reutilizada.
 * El código permite al frontend distinguir "vencida" (pedir login) de
 * "reutilizada" (posible robo; se cerraron todas las sesiones).
 */
@Getter
public class SesionInvalidaException extends RuntimeException {

    private final String codigo;

    public SesionInvalidaException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
}
