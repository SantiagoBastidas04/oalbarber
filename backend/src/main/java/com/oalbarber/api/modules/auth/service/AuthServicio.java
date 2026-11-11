package com.oalbarber.api.modules.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.common.exception.CredencialesInvalidasException;
import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.modules.auth.domain.SesionRefresco;
import com.oalbarber.api.modules.auth.dto.AuthResponse;
import com.oalbarber.api.modules.usuario.domain.Usuario;
import com.oalbarber.api.modules.usuario.dto.UsuarioRequest;
import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;
import com.oalbarber.api.modules.usuario.mapper.UsuarioMapper;
import com.oalbarber.api.modules.usuario.repository.UsuarioRepository;
import com.oalbarber.api.modules.usuario.service.UsuarioService;

/**
 * Orquesta la sesión completa: verifica credenciales, firma el pase corto y
 * deja el comprobante de refresco en manos de {@link SesionRefrescoServicio}.
 */
@Service
public class AuthServicio {

    private final UsuarioRepository usuarios;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioService usuarioService;
    private final SesionRefrescoServicio sesiones;
    private final TokenServicio pases;
    private final PasswordEncoder codificador;

    public AuthServicio(UsuarioRepository usuarios,
                        UsuarioMapper usuarioMapper,
                        UsuarioService usuarioService,
                        SesionRefrescoServicio sesiones,
                        TokenServicio pases,
                        PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.usuarioMapper = usuarioMapper;
        this.usuarioService = usuarioService;
        this.sesiones = sesiones;
        this.pases = pases;
        this.codificador = codificador;
    }

    /** Lo que entrega un login: la respuesta HTTP y el comprobante para la cookie. */
    public record SesionAbierta(AuthResponse respuesta, String comprobante) {}

    /**
     * Registra el cliente (rol CLIENTE, misma lógica del módulo usuario) y de
     * una vez le abre la sesión: el formulario nunca pide dos veces la
     * contraseña.
     */
    @Transactional
    public SesionAbierta registrar(UsuarioRequest solicitud, String agente, String ip) {
        UsuarioResponse nuevo = usuarioService.registrarUsuario(solicitud);
        Usuario usuario = usuarios.findByCelular(nuevo.getCelular())
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario recién registrado no se encontró."));
        SesionRefrescoServicio.SesionNueva sesion = sesiones.abrir(usuario, agente, ip);
        return new SesionAbierta(new AuthResponse(pases.firmarAcceso(usuario), nuevo),
                sesion.tokenEnClaro());
    }

    /**
     * Valida celular + contraseña y abre la sesión del dispositivo.
     * El mensaje de error es el mismo si falla el celular, la contraseña o la
     * cuenta está desactivada: no le damos pistas a quien no es el dueño.
     */
    @Transactional
    public SesionAbierta ingresar(String celular, String contrasena, String agente, String ip) {
        Usuario usuario = usuarios.findByCelular(celular.trim())
                .filter(u -> codificador.matches(contrasena, u.getContrasenaHash()))
                .filter(Usuario::isActivo)
                .orElseThrow(CredencialesInvalidasException::new);

        SesionRefrescoServicio.SesionNueva sesion = sesiones.abrir(usuario, agente, ip);
        return new SesionAbierta(respuestaConPase(usuario), sesion.tokenEnClaro());
    }

    /**
     * Renueva el pase corto usando el comprobante de la cookie.
     * {@link SesionRefrescoServicio#refrescar} se encarga de rotar el
     * comprobante (el nuevo viaja en la cookie que reemplaza el controller).
     */
    @Transactional
    public SesionAbierta refrescar(String comprobante, String agente, String ip) {
        SesionRefrescoServicio.SesionNueva sesion = sesiones.refrescar(comprobante, agente, ip);
        return new SesionAbierta(respuestaConPase(sesion.sesion().getUsuario()),
                sesion.tokenEnClaro());
    }

    /** Cierra la sesión del comprobante: revoca la fila; el controller limpia la cookie. */
    @Transactional
    public void cerrarSesion(String comprobante) {
        sesiones.cerrar(comprobante);
    }

    /** Datos del usuario del pase ya validado (GET /api/auth/yo). */
    @Transactional(readOnly = true)
    public UsuarioResponse yo(UUID usuarioId) {
        return usuarios.findById(usuarioId)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario del pase ya no existe."));
    }

    private AuthResponse respuestaConPase(Usuario usuario) {
        return new AuthResponse(pases.firmarAcceso(usuario), usuarioMapper.toResponse(usuario));
    }
}
