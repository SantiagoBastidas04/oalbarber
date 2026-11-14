package com.oalbarber.api.modules.servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oalbarber.api.modules.servicio.domain.Servicio;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.mapper.ServicioMapper;
import com.oalbarber.api.modules.servicio.repository.ServicioRepository;

/**
 * El catálogo público solo entrega servicios activos (RF-07). El service no
 * decide quién puede pedir los inactivos: eso lo resuelve el controlador.
 */
@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private ServicioMapper servicioMapper;

    @InjectMocks
    private ServicioService servicioService;

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
