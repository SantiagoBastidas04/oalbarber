package com.oalbarber.api.common.exception;

/**
 * El pase de acceso no sirve: firma inválida, vencido o con formato raro.
 * El handler la convertirá en HTTP 401 cuando armemos los endpoints.
 */
public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
