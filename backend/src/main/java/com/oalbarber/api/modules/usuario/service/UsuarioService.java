package com.oalbarber.api.modules.usuario.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.common.exception.RecursoDuplicadoException;
import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.modules.usuario.domain.RolUsuario;
import com.oalbarber.api.modules.usuario.domain.Usuario;
import com.oalbarber.api.modules.usuario.dto.UsuarioRequest;
import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;
import com.oalbarber.api.modules.usuario.mapper.UsuarioMapper;
import com.oalbarber.api.modules.usuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioMapper usuarioMapper,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un usuario con rol CLIENTE (RF-01). Mantiene el flujo de registro
     * público, que nunca permite elegir el rol desde la solicitud.
     */
    @Transactional
    public UsuarioResponse registrarUsuario(UsuarioRequest solicitud) {
        return crearUsuarioConRol(solicitud, RolUsuario.CLIENTE);
    }

    /**
     * Crea una cuenta con el rol seleccionado por el código que invoca este
     * service. Reutiliza la unicidad del celular y el hash BCrypt; el DTO no
     * contiene un campo de rol (RNF-01).
     *
     * @param solicitud datos de identidad y contraseña inicial
     * @param rol rol asignado por el código llamador
     * @return datos públicos del usuario creado, sin contraseña ni hash
     */
    @Transactional
    public UsuarioResponse crearUsuarioConRol(UsuarioRequest solicitud, RolUsuario rol) {
        Objects.requireNonNull(solicitud, "Los datos del usuario son obligatorios.");
        Objects.requireNonNull(rol, "El rol del usuario es obligatorio.");

        String celular = solicitud.getCelular().trim();

        if (usuarioRepository.existsByCelular(celular)) {
            throw new RecursoDuplicadoException("CELULAR_DUPLICADO",
                    "EL CELULAR YA ESTA REGISTRADO EN NUESTRO SISTEMA.");
        }

        Usuario usuario = usuarioMapper.toEntity(solicitud);
        usuario.setCelular(celular);
        usuario.setContrasenaHash(passwordEncoder.encode(solicitud.getContrasena()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(guardado);
    }


    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario indicado."));
        return usuarioMapper.toResponse(usuario);
    }

    /**
     * Lista los clientes ordenados por nombre. Para el panel de administración:
     * el rol del que consulta se exige en el controlador con @PreAuthorize.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarClientes() {
        return usuarioRepository.findByRolOrderByNombreAsc(RolUsuario.CLIENTE).stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }
}
