package com.oalbarber.api.modules.servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oalbarber.api.common.exception.RecursoDuplicadoException;
import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.modules.servicio.domain.Servicio;
import com.oalbarber.api.modules.servicio.dto.ServicioRequest;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.mapper.ServicioMapper;
import com.oalbarber.api.modules.servicio.repository.ServicioRepository;

/**
 * Reglas del catálogo (RF-07 y RF-08). El service concentra la lógica: filtro
 * de activos, nombre único sin distinguir mayúsculas y desactivación lógica
 * (RN-16). El controlador solo delega y el mapper no decide nada.
 */
@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private ServicioMapper servicioMapper;

    @InjectMocks
    private ServicioService servicioService;

    // ------------------------------------------------------------------
    // Catálogo público (RF-07)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("sin inactivos consulta solo los servicios activos ordenados por nombre")
    void sinInactivosConsultaSoloActivos() {
        Servicio corte = servicio("Corte normal", true);
        ServicioResponse respuesta = respuesta(corte);
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(corte));
        when(servicioMapper.toResponse(corte)).thenReturn(respuesta);

        List<ServicioResponse> resultado = servicioService.listar(false);

        assertThat(resultado).containsExactly(respuesta);
        verify(servicioRepository).findByActivoTrueOrderByNombreAsc();
        verify(servicioRepository, never()).findAllByOrderByNombreAsc();
    }

    @Test
    @DisplayName("con inactivos consulta todo el catálogo ordenado por nombre")
    void conInactivosConsultaTodoElCatalogo() {
        Servicio corte = servicio("Corte normal", true);
        Servicio mechas = servicio("Mechas", false);
        when(servicioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(corte, mechas));
        when(servicioMapper.toResponse(corte)).thenReturn(respuesta(corte));
        when(servicioMapper.toResponse(mechas)).thenReturn(respuesta(mechas));

        List<ServicioResponse> resultado = servicioService.listar(true);

        assertThat(resultado).hasSize(2);
        verify(servicioRepository).findAllByOrderByNombreAsc();
        verify(servicioRepository, never()).findByActivoTrueOrderByNombreAsc();
    }

    @Test
    @DisplayName("si no hay servicios devuelve una lista vacía")
    void sinServiciosDevuelveListaVacia() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of());

        List<ServicioResponse> resultado = servicioService.listar(false);

        assertThat(resultado).isEmpty();
        verify(servicioMapper, never()).toResponse(any());
    }

    // ------------------------------------------------------------------
    // Crear (RF-08)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("crear guarda el servicio activo y con el nombre sin espacios sobrantes")
    void crearGuardaActivoYRecortaElNombre() {
        ServicioRequest peticion = peticion("  Corte normal  ");
        Servicio entidad = servicio("  Corte normal  ", false);
        ServicioResponse respuesta = respuesta(entidad);
        when(servicioRepository.existsByNombreIgnoreCase("Corte normal")).thenReturn(false);
        when(servicioMapper.toEntity(peticion)).thenReturn(entidad);
        when(servicioRepository.save(entidad)).thenReturn(entidad);
        when(servicioMapper.toResponse(entidad)).thenReturn(respuesta);

        ServicioResponse resultado = servicioService.crear(peticion);

        assertThat(resultado).isSameAs(respuesta);
        assertThat(entidad.getNombre()).isEqualTo("Corte normal");
        assertThat(entidad.isActivo()).isTrue();
        verify(servicioRepository).save(entidad);
    }

    @Test
    @DisplayName("crear con un nombre que ya existe responde NOMBRE_DUPLICADO sin guardar")
    void crearConNombreRepetidoLanzaConflicto() {
        ServicioRequest peticion = peticion("corte NORMAL");
        when(servicioRepository.existsByNombreIgnoreCase("corte NORMAL")).thenReturn(true);

        assertThatThrownBy(() -> servicioService.crear(peticion))
                .isInstanceOf(RecursoDuplicadoException.class)
                .isInstanceOfSatisfying(RecursoDuplicadoException.class,
                        ex -> assertThat(ex.getCodigo()).isEqualTo("NOMBRE_DUPLICADO"));

        verify(servicioRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // Editar (RF-08)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("editar el mismo servicio conservando su nombre no lo marca como duplicado")
    void actualizarConservandoElPropioNombreEsValido() {
        UUID id = UUID.randomUUID();
        Servicio existente = servicio("Corte normal", true);
        existente.setId(id);
        ServicioRequest peticion = peticion("Corte normal");
        peticion.setPrecio(new BigDecimal("30000"));
        when(servicioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(servicioRepository.existsByNombreIgnoreCaseAndIdNot("Corte normal", id))
                .thenReturn(false);
        when(servicioMapper.toResponse(existente)).thenReturn(respuesta(existente));

        servicioService.actualizar(id, peticion);

        verify(servicioMapper).actualizar(peticion, existente);
        verify(servicioRepository, never()).save(any());
    }

    @Test
    @DisplayName("editar con el nombre de otro servicio responde NOMBRE_DUPLICADO")
    void actualizarConNombreDeOtroLanzaConflicto() {
        UUID id = UUID.randomUUID();
        Servicio existente = servicio("Corte normal", true);
        existente.setId(id);
        ServicioRequest peticion = peticion("Mechas");
        when(servicioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(servicioRepository.existsByNombreIgnoreCaseAndIdNot("Mechas", id)).thenReturn(true);

        assertThatThrownBy(() -> servicioService.actualizar(id, peticion))
                .isInstanceOf(RecursoDuplicadoException.class)
                .isInstanceOfSatisfying(RecursoDuplicadoException.class,
                        ex -> assertThat(ex.getCodigo()).isEqualTo("NOMBRE_DUPLICADO"));

        verify(servicioMapper, never()).actualizar(any(), any());
    }

    @Test
    @DisplayName("editar un servicio que no existe responde recurso no encontrado")
    void actualizarInexistenteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(servicioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.actualizar(id, peticion("Corte normal")))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining(id.toString());

        verify(servicioMapper, never()).actualizar(any(), any());
    }

    // ------------------------------------------------------------------
    // Activar y desactivar (RN-16)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("desactivar solo cambia el estado: el registro y sus datos siguen intactos (RN-16)")
    void desactivarNoBorraNiAlteraLosDatos() {
        UUID id = UUID.randomUUID();
        Servicio existente = servicio("Mechas", true);
        existente.setId(id);
        when(servicioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(servicioMapper.toResponse(existente)).thenReturn(respuesta(existente));

        servicioService.cambiarEstado(id, false);

        assertThat(existente.isActivo()).isFalse();
        assertThat(existente.getNombre()).isEqualTo("Mechas");
        assertThat(existente.getDuracionMin()).isEqualTo(40);
        assertThat(existente.getPrecio()).isEqualByComparingTo("25000");
        verify(servicioRepository, never()).delete(any());
        verify(servicioRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("reactivar un servicio desactivado lo devuelve al catálogo")
    void reactivarVuelveADejarElServicioActivo() {
        UUID id = UUID.randomUUID();
        Servicio existente = servicio("Mechas", false);
        existente.setId(id);
        when(servicioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(servicioMapper.toResponse(existente)).thenReturn(respuesta(existente));

        servicioService.cambiarEstado(id, true);

        assertThat(existente.isActivo()).isTrue();
    }

    @Test
    @DisplayName("cambiar el estado de un servicio inexistente responde recurso no encontrado")
    void cambiarEstadoInexistenteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(servicioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.cambiarEstado(id, false))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private static ServicioRequest peticion(String nombre) {
        return new ServicioRequest(
                nombre,
                "Descripción de " + nombre.trim(),
                40,
                new BigDecimal("25000"),
                "corte-normal.jpg");
    }

    private static Servicio servicio(String nombre, boolean activo) {
        return Servicio.builder()
                .nombre(nombre)
                .descripcion("Descripción de " + nombre)
                .duracionMin(40)
                .precio(new BigDecimal("25000"))
                .imagen("corte-normal.jpg")
                .activo(activo)
                .build();
    }

    private static ServicioResponse respuesta(Servicio servicio) {
        return new ServicioResponse(
                UUID.randomUUID(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getDuracionMin(),
                servicio.getPrecio(),
                servicio.getImagen(),
                servicio.isActivo());
    }
}
