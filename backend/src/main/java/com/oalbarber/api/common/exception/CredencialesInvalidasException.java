package com.oalbarber.api.common.exception;

/**
 * El login falló: celular o contraseña equivocados (o cuenta desactivada).
 * Se usa el mismo mensaje para los tres casos para no revelar cuál falló.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("EL CELULAR O LA CONTRASEÑA SON INCORRECTOS.");
    }
}
