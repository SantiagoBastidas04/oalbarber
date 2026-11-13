package com.oalbarber.api.modules.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.oalbarber.api.common.exception.RecursoDuplicadoException;
import com.oalbarber.api.modules.usuario.domain.RolUsuario;
import com.oalbarber.api.modules.usuario.domain.Usuario;
import com.oalbarber.api.modules.usuario.dto.UsuarioRequest;
import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;
import com.oalbarber.api.modules.usuario.mapper.UsuarioMapper;
import com.oalbarber.api.modules.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void creaUsuarioConElRolAsignadoPorElCodigoYContrasenaCifrada() {
        UsuarioRequest solicitud = solicitud(" +573000000001 ");
        Usuario usuario = Usuario.builder().nombre("Usuario ficticio").build();
        UsuarioResponse respuesta = new UsuarioResponse();
        when(usuarioMapper.toEntity(solicitud)).thenReturn(usuario);
        when(passwordEncoder.encode("ClaveFicticia123")).thenReturn("hash-bcrypt-ficticio");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(usuarioMapper.toResponse(usuario)).thenReturn(respuesta);

        UsuarioResponse resultado = usuarioService.crearUsuarioConRol(
                solicitud, RolUsuario.BARBERO);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).existsByCelular("+573000000001");
        verify(passwordEncoder).encode("ClaveFicticia123");
        verify(usuarioRepository).save(usuarioGuardado.capture());
        assertEquals("+573000000001", usuarioGuardado.getValue().getCelular());
        assertEquals("hash-bcrypt-ficticio", usuarioGuardado.getValue().getContrasenaHash());
        assertEquals(RolUsuario.BARBERO, usuarioGuardado.getValue().getRol());
        assertTrue(usuarioGuardado.getValue().isActivo());
        assertSame(respuesta, resultado);
    }

    @Test
    void registrarUsuarioConservaElRolCliente() {
        UsuarioRequest solicitud = solicitud("+573000000002");
        Usuario usuario = Usuario.builder().nombre("Cliente ficticio").build();
        UsuarioResponse respuesta = new UsuarioResponse();
        when(usuarioMapper.toEntity(solicitud)).thenReturn(usuario);
        when(passwordEncoder.encode("ClaveFicticia123")).thenReturn("hash-bcrypt-ficticio");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(usuarioMapper.toResponse(usuario)).thenReturn(respuesta);

        UsuarioResponse resultado = usuarioService.registrarUsuario(solicitud);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioGuardado.capture());
        assertEquals(RolUsuario.CLIENTE, usuarioGuardado.getValue().getRol());
        assertSame(respuesta, resultado);
    }

    @Test
    void rechazaCelularDuplicadoAlCrearConRolInterno() {
        UsuarioRequest solicitud = solicitud(" +573000000003 ");
        when(usuarioRepository.existsByCelular("+573000000003")).thenReturn(true);

        RecursoDuplicadoException excepcion = assertThrows(RecursoDuplicadoException.class,
                () -> usuarioService.crearUsuarioConRol(solicitud, RolUsuario.BARBERO));

        assertEquals("CELULAR_DUPLICADO", excepcion.getCodigo());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(any());
    }

    private static UsuarioRequest solicitud(String celular) {
        return new UsuarioRequest("Usuario ficticio", celular, "ClaveFicticia123");
    }
}
