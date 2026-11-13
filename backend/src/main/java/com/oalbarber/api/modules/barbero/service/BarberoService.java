package com.oalbarber.api.modules.barbero.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.modules.barbero.domain.Barbero;
import com.oalbarber.api.modules.barbero.dto.BarberoAdminResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoPublicoResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoRequest;
import com.oalbarber.api.modules.barbero.mapper.BarberoMapper;
import com.oalbarber.api.modules.barbero.repository.BarberoRepository;
import com.oalbarber.api.modules.usuario.domain.RolUsuario;
import com.oalbarber.api.modules.usuario.dto.UsuarioRequest;
import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;
import com.oalbarber.api.modules.usuario.service.UsuarioService;

import lombok.RequiredArgsConstructor;

/** Reglas de aplicación para perfiles de barbero (RF-10, RN-01 y RN-12). */
@Service
@RequiredArgsConstructor
public class BarberoService {

    private final BarberoRepository barberoRepository;
    private final UsuarioService usuarioService;
    private final BarberoMapper barberoMapper;

    /** Lista perfiles activos sin exponer nombre de cuenta ni celular. */
    @Transactional(readOnly = true)
    public List<BarberoPublicoResponse> listarActivos() {
        return barberoRepository.findByActivoTrue().stream()
                .map(barberoMapper::aPublico)
                .toList();
    }

    /** Lista perfiles activos e inactivos para el panel administrativo. */
    @Transactional(readOnly = true)
    public List<BarberoAdminResponse> listarTodos() {
        return barberoRepository.findAll().stream()
                .map(this::aAdmin)
                .toList();
    }

    /**
     * Crea la cuenta BARBERO y su perfil dentro de una sola transacción.
     * La creación de cuenta reutiliza la unicidad del celular y BCrypt del módulo usuario.
     *
     * @param solicitud datos de la cuenta y el perfil
     * @return perfil y cuenta para el panel
     */
    @Transactional
    public BarberoAdminResponse crear(BarberoRequest solicitud) {
        UsuarioRequest datosUsuario = new UsuarioRequest(
                solicitud.nombre(), solicitud.celular(), solicitud.contrasena());
        UsuarioResponse usuario = usuarioService.crearUsuarioConRol(
                datosUsuario, RolUsuario.BARBERO);

        Barbero barbero = barberoMapper.aEntidad(solicitud, usuario.getId());
        Barbero guardado = barberoRepository.save(barbero);
        return barberoMapper.aAdmin(guardado, usuario);
    }

    /** Cambia el estado del perfil sin desactivar la cuenta ni cancelar citas. */
    @Transactional
    public BarberoAdminResponse cambiarEstado(UUID id, boolean activo) {
        Barbero barbero = barberoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el perfil de barbero indicado."));
        barbero.cambiarEstado(activo);
        Barbero guardado = barberoRepository.save(barbero);
        return aAdmin(guardado);
    }

    /** Obtiene el perfil administrativo por identificador. */
    @Transactional(readOnly = true)
    public BarberoAdminResponse obtener(UUID id) {
        Barbero barbero = barberoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el perfil de barbero indicado."));
        return aAdmin(barbero);
    }

    /**
     * Garantiza el perfil del usuario administrador inicial sin crear ni modificar su cuenta.
     * Si ya existe, no inserta otro; si el usuario tiene otro rol, no crea el perfil.
     *
     * @param usuarioId UUID del usuario que debe verificarse
     * @return {@code true} si el usuario es administrador y tiene perfil; {@code false} si no lo es
     */
    @Transactional
    public boolean asegurarPerfilAdministrador(UUID usuarioId) {
        UsuarioResponse usuario = usuarioService.obtenerPorId(usuarioId);
        if (usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            return false;
        }
        if (!barberoRepository.existsByUsuarioId(usuarioId)) {
            barberoRepository.save(Barbero.crear(usuarioId));
        }
        return true;
    }

    private BarberoAdminResponse aAdmin(Barbero barbero) {
        UsuarioResponse usuario = usuarioService.obtenerPorId(barbero.getUsuarioId());
        return barberoMapper.aAdmin(barbero, usuario);
    }
}
