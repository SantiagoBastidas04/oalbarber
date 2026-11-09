package com.oalbarber.api.modules.usuario.service;

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

    @Transactional
    public UsuarioResponse registrarUsuario(UsuarioRequest solicitud) {
        String celular = solicitud.getCelular().trim();

        if (usuarioRepository.existsByCelular(celular)) {
            throw new RecursoDuplicadoException("CELULAR_DUPLICADO",
                    "EL CELULAR YA ESTA REGISTRADO EN NUESTRO SISTEMA.");
        }

        Usuario usuario = usuarioMapper.toEntity(solicitud);
        usuario.setCelular(celular);
        usuario.setContrasenaHash(passwordEncoder.encode(solicitud.getContrasena()));
        usuario.setRol(RolUsuario.CLIENTE);
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
    
}
