package com.oalbarber.api.common.exception;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Convierte las excepciones en respuestas {@link ProblemDetail} (RFC 7807) con el campo extra
 * {@code codigo}, para que el frontend distinga el motivo aunque el HTTP coincida.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail credenciales(CredencialesInvalidasException ex, HttpServletRequest request) {
        return problema(HttpStatus.UNAUTHORIZED, "No pudimos iniciar sesión", ex.getMessage(),
                "CREDENCIALES_INVALIDAS", request);
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ProblemDetail tokenInvalido(TokenInvalidoException ex, HttpServletRequest request) {
        return problema(HttpStatus.UNAUTHORIZED, "Pase de acceso no válido", ex.getMessage(),
                "TOKEN_INVALIDO", request);
    }

    @ExceptionHandler(SesionInvalidaException.class)
    public ProblemDetail sesionInvalida(SesionInvalidaException ex, HttpServletRequest request) {
        return problema(HttpStatus.UNAUTHORIZED, "Sesión no válida", ex.getMessage(),
                ex.getCodigo(), request);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(),
                "NO_ENCONTRADO", request);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ProblemDetail duplicado(RecursoDuplicadoException ex, HttpServletRequest request) {
        return problema(HttpStatus.CONFLICT, "Recurso duplicado", ex.getMessage(),
                ex.getCodigo(), request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        return problema(HttpStatus.CONFLICT, "Regla de negocio", ex.getMessage(),
                ex.getCodigo(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Revisa los campos marcados.", "VALIDACION", request);
        detalle.setProperty("errors", errores);
        return detalle;
    }

    /** Validación de negocio con detalle por fila (por ejemplo, un día del horario). */
    @ExceptionHandler(ValidacionException.class)
    public ProblemDetail validacionNegocio(ValidacionException ex, HttpServletRequest request) {
        ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Revisa los datos enviados",
                ex.getMessage(), ex.getCodigo(), request);
        detalle.setProperty("errors", ex.getErrores());
        return detalle;
    }

    /** Un JSON mal formado o una fecha/hora con formato inválido es un 400, no un 500. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoIlegible(HttpMessageNotReadableException ex,
                                        HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "El cuerpo de la petición no tiene el formato esperado.",
                "CUERPO_INVALIDO", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail parametroObligatorioAusente(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return errorParametro(ex.getParameterName(), "El campo es obligatorio.", "VALIDACION", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail formatoParametroInvalido(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String mensaje = "fecha".equals(ex.getName())
                ? "Debe tener formato AAAA-MM-DD."
                : "Debe ser un identificador UUID válido.";
        return errorParametro(ex.getName(), mensaje, "PARAMETRO_INVALIDO", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Conflicto de integridad: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflicto",
                "Ya existe un registro con esos datos.", "CONFLICTO", request);
    }

    /**
     * El pase es válido pero el rol no alcanza (por ejemplo, un CLIENTE
     * pidiendo la lista de clientes). Debe ir ANTES del catch-all: si no,
     * el handler de Exception la captura y respondería 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para realizar esta acción.", "ACCESO_DENEGADO", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "No se pudo completar la operación.", "ERROR_INTERNO", request);
    }

    private ProblemDetail problema(HttpStatus estado, String titulo, String detalle,
                                   String codigo, HttpServletRequest request) {
        ProblemDetail detalleProblema = ProblemDetail.forStatusAndDetail(estado, detalle);
        detalleProblema.setTitle(titulo);
        detalleProblema.setType(URI.create("about:blank"));
        detalleProblema.setInstance(URI.create(request.getRequestURI()));
        detalleProblema.setProperty("codigo", codigo);
        return detalleProblema;
    }

    private ProblemDetail errorParametro(String campo, String mensaje, String codigo,
                                         HttpServletRequest request) {
        ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "Revisa los parámetros de la solicitud.", codigo, request);
        detalle.setProperty("errors", Map.of(campo, mensaje));
        return detalle;
    }
}
