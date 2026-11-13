package com.oalbarber.api.modules.barbero.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oalbarber.api.common.exception.RecursoDuplicadoException;
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

@ExtendWith(MockitoExtension.class)
class BarberoServiceTest {

    private static final UUID BARBERO_ID =
            UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    private static final UUID USUARIO_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Mock
    private BarberoRepository barberoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private BarberoMapper barberoMapper;

    @InjectMocks
    private BarberoService barberoService;

    @Test
    void listaPerfilesActivosEnLaVistaPublica() {
        Barbero barbero = Barbero.crear(USUARIO_ID);
        BarberoPublicoResponse respuesta = publico(BARBERO_ID);
        when(barberoRepository.findByActivoTrue()).thenReturn(List.of(barbero));
        when(barberoMapper.aPublico(barbero)).thenReturn(respuesta);

        List<BarberoPublicoResponse> resultado = barberoService.listarActivos();

        assertEquals(List.of(respuesta), resultado);
        verify(barberoRepository).findByActivoTrue();
    }

    @Test
    void listaTodosLosPerfilesParaElPanel() {
        Barbero barbero = Barbero.crear(USUARIO_ID);
        UsuarioResponse usuario = usuario(RolUsuario.BARBERO);
        BarberoAdminResponse respuesta = admin(BARBERO_ID);
        when(barberoRepository.findAll()).thenReturn(List.of(barbero));
        when(usuarioService.obtenerPorId(USUARIO_ID)).thenReturn(usuario);
        when(barberoMapper.aAdmin(barbero, usuario)).thenReturn(respuesta);

        List<BarberoAdminResponse> resultado = barberoService.listarTodos();

        assertEquals(List.of(respuesta), resultado);
        verify(usuarioService).obtenerPorId(USUARIO_ID);
    }

    @Test
    void creaCuentaBarberoYPerfil() {
        BarberoRequest solicitud = solicitud();
        UsuarioResponse usuario = usuario(RolUsuario.BARBERO);
        Barbero entidad = Barbero.crear(USUARIO_ID);
        BarberoAdminResponse respuesta = admin(BARBERO_ID);
        when(usuarioService.crearUsuarioConRol(any(UsuarioRequest.class), eq(RolUsuario.BARBERO)))
                .thenReturn(usuario);
        when(barberoMapper.aEntidad(solicitud, USUARIO_ID)).thenReturn(entidad);
        when(barberoRepository.save(entidad)).thenReturn(entidad);
        when(barberoMapper.aAdmin(entidad, usuario)).thenReturn(respuesta);

        BarberoAdminResponse resultado = barberoService.crear(solicitud);

        ArgumentCaptor<UsuarioRequest> datosCuenta = ArgumentCaptor.forClass(UsuarioRequest.class);
        verify(usuarioService).crearUsuarioConRol(datosCuenta.capture(), eq(RolUsuario.BARBERO));
        assertEquals("Barbero ficticio", datosCuenta.getValue().getNombre());
        assertEquals("+573000000001", datosCuenta.getValue().getCelular());
        verify(barberoRepository).save(entidad);
        assertSame(respuesta, resultado);
    }

    @Test
    void propagaElCelularDuplicadoSinCrearPerfil() {
        BarberoRequest solicitud = solicitud();
        when(usuarioService.crearUsuarioConRol(any(UsuarioRequest.class), eq(RolUsuario.BARBERO)))
                .thenThrow(new RecursoDuplicadoException("CELULAR_DUPLICADO",
                        "EL CELULAR YA ESTA REGISTRADO EN NUESTRO SISTEMA."));

        RecursoDuplicadoException excepcion = assertThrows(RecursoDuplicadoException.class,
                () -> barberoService.crear(solicitud));

        assertEquals("CELULAR_DUPLICADO", excepcion.getCodigo());
        verifyNoInteractions(barberoRepository, barberoMapper);
    }

    @Test
    void lanzaNoEncontradoAlConsultarUnPerfilAusente() {
        when(barberoRepository.findById(BARBERO_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> barberoService.obtener(BARBERO_ID));
    }

    @Test
    void cambiarEstadoSoloModificaElPerfil() {
        Barbero barbero = Barbero.crear(USUARIO_ID);
        UsuarioResponse usuario = usuario(RolUsuario.BARBERO);
        BarberoAdminResponse respuesta = admin(BARBERO_ID);
        when(barberoRepository.findById(BARBERO_ID)).thenReturn(Optional.of(barbero));
        when(barberoRepository.save(barbero)).thenReturn(barbero);
        when(usuarioService.obtenerPorId(USUARIO_ID)).thenReturn(usuario);
        when(barberoMapper.aAdmin(barbero, usuario)).thenReturn(respuesta);

        BarberoAdminResponse resultado = barberoService.cambiarEstado(BARBERO_ID, false);

        assertFalse(barbero.isActivo());
        assertSame(respuesta, resultado);
        verify(usuarioService, never()).crearUsuarioConRol(
                any(UsuarioRequest.class), any(RolUsuario.class));
    }

    @Test
    void aseguraPerfilAdministradorUnaSolaVezYNoCreaPerfilParaOtroRol() {
        UsuarioResponse administrador = usuario(RolUsuario.ADMINISTRADOR);
        when(usuarioService.obtenerPorId(USUARIO_ID)).thenReturn(administrador);
        when(barberoRepository.existsByUsuarioId(USUARIO_ID)).thenReturn(true, false);

        assertTrue(barberoService.asegurarPerfilAdministrador(USUARIO_ID));
        assertTrue(barberoService.asegurarPerfilAdministrador(USUARIO_ID));
        verify(barberoRepository).save(any(Barbero.class));

        UsuarioResponse cliente = usuario(RolUsuario.CLIENTE);
        when(usuarioService.obtenerPorId(BARBERO_ID)).thenReturn(cliente);
        assertFalse(barberoService.asegurarPerfilAdministrador(BARBERO_ID));
        verify(barberoRepository, never()).existsByUsuarioId(BARBERO_ID);
    }

    private static BarberoRequest solicitud() {
        return new BarberoRequest("Barbero ficticio", "+573000000001", "ClaveFicticia123",
                "Nombre visible", "Descripción ficticia", "barbero-01.webp");
    }

    private static UsuarioResponse usuario(RolUsuario rol) {
        UsuarioResponse usuario = new UsuarioResponse();
        usuario.setId(USUARIO_ID);
        usuario.setNombre("Usuario ficticio");
        usuario.setCelular("+573000000001");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }

    private static BarberoPublicoResponse publico(UUID id) {
        return new BarberoPublicoResponse(id, "Nombre visible", "Descripción ficticia",
                "barbero-01.webp", true);
    }

    private static BarberoAdminResponse admin(UUID id) {
        return new BarberoAdminResponse(id, USUARIO_ID, "Usuario ficticio", "+573000000001",
                "Nombre visible", "Descripción ficticia", "barbero-01.webp", true);
    }
}
