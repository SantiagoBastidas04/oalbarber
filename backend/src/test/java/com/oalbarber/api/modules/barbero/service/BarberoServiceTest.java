package com.oalbarber.api.modules.barbero.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.oalbarber.api.common.exception.ReglaNegocioException;
import com.oalbarber.api.modules.barbero.domain.Barbero;
import com.oalbarber.api.modules.barbero.domain.BarberoServicio;
import com.oalbarber.api.modules.barbero.dto.BarberoAdminResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoPublicoResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoRequest;
import com.oalbarber.api.modules.barbero.mapper.BarberoMapper;
import com.oalbarber.api.modules.barbero.repository.BarberoRepository;
import com.oalbarber.api.modules.barbero.repository.BarberoServicioRepository;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.service.ServicioService;
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
    private static final UUID SERVICIO_UNO_ID =
            UUID.fromString("cccccccc-cccc-4ccc-8ccc-ccccccccccc1");
    private static final UUID SERVICIO_DOS_ID =
            UUID.fromString("dddddddd-dddd-4ddd-8ddd-ddddddddddd2");
    private static final UUID SERVICIO_TRES_ID =
            UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeee3");
    private static final UUID SERVICIO_INEXISTENTE_ID =
            UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff");
    private static final UUID BARBERO_INACTIVO_ID =
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");

    @Mock
    private BarberoRepository barberoRepository;

    @Mock
    private BarberoServicioRepository barberoServicioRepository;

    @Mock
    private ServicioService servicioServiceCatalogo;

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
        assertEquals(respuesta, resultado);
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
        assertEquals(respuesta, resultado);
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

    @Test
    void listaSoloServiciosActivosAsignadosAUnBarberoExistente() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(barberoServicioRepository.findById_BarberoId(BARBERO_ID)).thenReturn(List.of(
                BarberoServicio.crear(BARBERO_ID, SERVICIO_UNO_ID),
                BarberoServicio.crear(BARBERO_ID, SERVICIO_INEXISTENTE_ID)));
        ServicioResponse servicioActivo = servicio(SERVICIO_UNO_ID);
        when(servicioServiceCatalogo.listar(false)).thenReturn(List.of(servicioActivo));

        List<ServicioResponse> resultado = barberoService.listarServiciosDe(BARBERO_ID);

        assertEquals(List.of(servicioActivo), resultado);
    }

    @Test
    void reemplazaAsignacionesAgregandoYQuitandoSoloLosParesNecesarios() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(servicioServiceCatalogo.listar(false)).thenReturn(List.of(
                servicio(SERVICIO_UNO_ID), servicio(SERVICIO_DOS_ID), servicio(SERVICIO_TRES_ID)));
        when(barberoServicioRepository.findById_BarberoId(BARBERO_ID)).thenReturn(List.of(
                BarberoServicio.crear(BARBERO_ID, SERVICIO_UNO_ID),
                BarberoServicio.crear(BARBERO_ID, SERVICIO_DOS_ID)));
        when(barberoServicioRepository.deleteById_BarberoIdAndId_ServicioId(
                BARBERO_ID, SERVICIO_UNO_ID)).thenReturn(1L);
        when(barberoServicioRepository.saveAll(anyList())).thenAnswer(invocacion -> invocacion.getArgument(0));

        List<ServicioResponse> resultado = barberoService.reemplazarServicios(
                BARBERO_ID, List.of(SERVICIO_DOS_ID, SERVICIO_TRES_ID));

        verify(barberoServicioRepository).deleteById_BarberoIdAndId_ServicioId(
                BARBERO_ID, SERVICIO_UNO_ID);
        verify(barberoServicioRepository).saveAll(List.of(
                BarberoServicio.crear(BARBERO_ID, SERVICIO_TRES_ID)));
        assertEquals(List.of(SERVICIO_DOS_ID, SERVICIO_TRES_ID),
                resultado.stream().map(ServicioResponse::getId).toList());
    }

    @Test
    void rechazaIdsDuplicadosConCodigoDeServicioNoValido() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class,
                () -> barberoService.reemplazarServicios(
                        BARBERO_ID, List.of(SERVICIO_UNO_ID, SERVICIO_UNO_ID)));

        assertEquals("SERVICIO_NO_VALIDO", excepcion.getCodigo());
        verifyNoInteractions(servicioServiceCatalogo, barberoServicioRepository);
    }

    @Test
    void rechazaServiciosInexistentesOInactivos() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(servicioServiceCatalogo.listar(false)).thenReturn(List.of(servicio(SERVICIO_UNO_ID)));

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class,
                () -> barberoService.reemplazarServicios(
                        BARBERO_ID, List.of(SERVICIO_INEXISTENTE_ID)));

        assertEquals("SERVICIO_NO_VALIDO", excepcion.getCodigo());
        verifyNoInteractions(barberoServicioRepository);
    }

    @Test
    void rechazaLaListaVaciaSoloCuandoElBarberoNoExiste() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class,
                () -> barberoService.reemplazarServicios(BARBERO_ID, List.of()));

        verifyNoInteractions(servicioServiceCatalogo, barberoServicioRepository);
    }

    @Test
    void permiteListaVaciaYEliminaLasAsignacionesActuales() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(servicioServiceCatalogo.listar(false)).thenReturn(List.of(servicio(SERVICIO_UNO_ID)));
        when(barberoServicioRepository.findById_BarberoId(BARBERO_ID)).thenReturn(List.of(
                BarberoServicio.crear(BARBERO_ID, SERVICIO_UNO_ID)));
        when(barberoServicioRepository.deleteById_BarberoIdAndId_ServicioId(
                BARBERO_ID, SERVICIO_UNO_ID)).thenReturn(1L);

        List<ServicioResponse> resultado = barberoService.reemplazarServicios(BARBERO_ID, List.of());

        assertTrue(resultado.isEmpty());
        verify(barberoServicioRepository).deleteById_BarberoIdAndId_ServicioId(
                BARBERO_ID, SERVICIO_UNO_ID);
        verify(barberoServicioRepository, never()).saveAll(anyList());
    }

    @Test
    void listaSoloBarberosActivosQueOfrecenUnServicioActivo() {
        Barbero activo = Barbero.crear(USUARIO_ID);
        Barbero inactivo = Barbero.crear(BARBERO_INACTIVO_ID);
        inactivo.cambiarEstado(false);
        when(servicioServiceCatalogo.listar(false)).thenReturn(List.of(servicio(SERVICIO_UNO_ID)));
        when(barberoServicioRepository.findById_ServicioId(SERVICIO_UNO_ID)).thenReturn(List.of(
                BarberoServicio.crear(BARBERO_ID, SERVICIO_UNO_ID),
                BarberoServicio.crear(BARBERO_INACTIVO_ID, SERVICIO_UNO_ID)));
        when(barberoRepository.findAllById(List.of(BARBERO_ID, BARBERO_INACTIVO_ID)))
                .thenReturn(List.of(activo, inactivo));

        assertEquals(List.of(activo),
                barberoService.listarActivosQueOfrecenServicio(SERVICIO_UNO_ID));
        assertTrue(barberoService.listarActivosQueOfrecenServicio(SERVICIO_DOS_ID).isEmpty());
    }

    @Test
    void compruebaAsignacionSoloParaUnServicioActivo() {
        when(servicioServiceCatalogo.listar(false)).thenReturn(List.of(servicio(SERVICIO_UNO_ID)));
        when(barberoServicioRepository.existsById_BarberoIdAndId_ServicioId(
                BARBERO_ID, SERVICIO_UNO_ID)).thenReturn(true);

        assertTrue(barberoService.ofreceServicio(BARBERO_ID, SERVICIO_UNO_ID));
        assertFalse(barberoService.ofreceServicio(BARBERO_ID, SERVICIO_DOS_ID));
        verify(barberoServicioRepository, never()).existsById_BarberoIdAndId_ServicioId(
                BARBERO_ID, SERVICIO_DOS_ID);
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
                "Nombre visible", "Descripción ficticia", "barbero-01.webp", true, List.of());
    }

    private static ServicioResponse servicio(UUID id) {
        ServicioResponse servicio = new ServicioResponse();
        servicio.setId(id);
        servicio.setNombre("Servicio ficticio");
        servicio.setActivo(true);
        return servicio;
    }
}
