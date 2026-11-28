package com.oalbarber.api.modules.horario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.common.exception.ValidacionException;
import com.oalbarber.api.modules.barbero.domain.Barbero;
import com.oalbarber.api.modules.barbero.repository.BarberoRepository;
import com.oalbarber.api.modules.horario.domain.HorarioAtencion;
import com.oalbarber.api.modules.horario.domain.HorarioExcepcion;
import com.oalbarber.api.modules.horario.dto.HorarioDiaRequest;
import com.oalbarber.api.modules.horario.dto.HorarioDiaResponse;
import com.oalbarber.api.modules.horario.dto.HorarioExcepcionRequest;
import com.oalbarber.api.modules.horario.dto.HorarioSemanaRequest;
import com.oalbarber.api.modules.horario.repository.HorarioAtencionRepository;
import com.oalbarber.api.modules.horario.repository.HorarioExcepcionRepository;

@ExtendWith(MockitoExtension.class)
class HorarioServiceTest {

    private static final UUID BARBERO_ID =
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
    private static final UUID OTRO_BARBERO_ID =
            UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
    private static final UUID USUARIO_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final LocalDate FECHA = LocalDate.of(2026, 12, 6);

    @Mock
    private HorarioAtencionRepository horarioRepository;

    @Mock
    private HorarioExcepcionRepository excepcionRepository;

    @Mock
    private BarberoRepository barberoRepository;

    @InjectMocks
    private HorarioService horarioService;

    // ------------------------------------------------------------------
    // Reemplazo de la semana
    // ------------------------------------------------------------------

    @Test
    @DisplayName("guarda los días válidos y reemplaza la semana anterior")
    void guardaSemanaValida() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(horarioRepository.saveAll(anyList()))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        List<HorarioDiaResponse> resultado = horarioService.reemplazar(BARBERO_ID,
                new HorarioSemanaRequest(List.of(
                        dia(1, "10:00", "19:00", "13:00", "14:00"),
                        dia(6, "10:00", "19:00", null, null))));

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).diaSemana());
        assertEquals(6, resultado.get(1).diaSemana());
        assertEquals(LocalTime.of(10, 0), resultado.get(0).horaInicio());
        assertEquals(LocalTime.of(13, 0), resultado.get(0).almuerzoInicio());
        verify(horarioRepository).deleteByBarberoId(BARBERO_ID);
    }

    @Test
    @DisplayName("una lista vacía cierra la semana")
    void listaVaciaCierraLaSemana() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(horarioRepository.saveAll(anyList())).thenReturn(List.of());

        List<HorarioDiaResponse> resultado = horarioService.reemplazar(BARBERO_ID,
                new HorarioSemanaRequest(List.of()));

        assertTrue(resultado.isEmpty());
        verify(horarioRepository).deleteByBarberoId(BARBERO_ID);
    }

    @Test
    @DisplayName("el domingo responde con el detalle del día 7 (RN-01)")
    void domingoNoSeConfigura() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.reemplazar(BARBERO_ID,
                        new HorarioSemanaRequest(List.of(
                                dia(7, "10:00", "19:00", null, null)))));

        assertEquals("HORARIO_INVALIDO", error.getCodigo());
        assertEquals("El domingo está cerrado (RN-01).", error.getErrores().get("7"));
        verify(horarioRepository, never()).deleteByBarberoId(any());
    }

    @Test
    @DisplayName("el inicio debe ser menor que el fin y se informa por día")
    void inicioDebeSerMenorQueFin() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.reemplazar(BARBERO_ID,
                        new HorarioSemanaRequest(List.of(
                                dia(2, "19:00", "10:00", null, null)))));

        assertEquals("La hora de inicio debe ser menor que la de fin.",
                error.getErrores().get("2"));
    }

    @Test
    @DisplayName("la jornada debe quedar entre las 10:00 y las 19:00")
    void jornadaDentroDeLaAtencion() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.reemplazar(BARBERO_ID,
                        new HorarioSemanaRequest(List.of(
                                dia(3, "08:00", "19:00", null, null)))));

        assertEquals("La jornada debe estar entre las 10:00 y las 19:00 (RN-01).",
                error.getErrores().get("3"));
    }

    @Test
    @DisplayName("el almuerzo debe venir completo o vacío")
    void almuerzoCompleto() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.reemplazar(BARBERO_ID,
                        new HorarioSemanaRequest(List.of(
                                dia(4, "10:00", "19:00", "13:00", null)))));

        assertEquals("El almuerzo debe tener hora de inicio y de fin, o quedar vacío.",
                error.getErrores().get("4"));
    }

    @Test
    @DisplayName("el almuerzo debe quedar dentro de la jornada")
    void almuerzoDentroDeLaJornada() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.reemplazar(BARBERO_ID,
                        new HorarioSemanaRequest(List.of(
                                dia(5, "10:00", "19:00", "18:30", "19:00")))));

        assertEquals("El almuerzo debe quedar dentro de la jornada.",
                error.getErrores().get("5"));
    }

    @Test
    @DisplayName("acumula el detalle de varios días en una sola respuesta")
    void acumulaErroresPorDia() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.reemplazar(BARBERO_ID,
                        new HorarioSemanaRequest(List.of(
                                dia(2, "19:00", "10:00", null, null),
                                dia(7, "10:00", "19:00", null, null)))));

        assertEquals(2, error.getErrores().size());
        assertNotNull(error.getErrores().get("2"));
        assertNotNull(error.getErrores().get("7"));
    }

    @Test
    @DisplayName("un día repetido se rechaza")
    void diaRepetido() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.reemplazar(BARBERO_ID,
                        new HorarioSemanaRequest(List.of(
                                dia(1, "10:00", "19:00", null, null),
                                dia(1, "10:00", "19:00", null, null)))));

        assertEquals("El día está repetido en la lista.", error.getErrores().get("1"));
    }

    @Test
    @DisplayName("un perfil inexistente responde 404 y no toca el horario")
    void barberoInexistente() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class,
                () -> horarioService.reemplazar(BARBERO_ID,
                        new HorarioSemanaRequest(List.of())));

        verifyNoInteractions(horarioRepository);
    }

    // ------------------------------------------------------------------
    // Consulta
    // ------------------------------------------------------------------

    @Test
    @DisplayName("el administrador consulta el horario de cualquier barbero")
    void administradorConsultaCualquierHorario() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(horarioRepository.findByBarberoIdOrderByDiaSemanaAsc(BARBERO_ID))
                .thenReturn(List.of(fila(1)));

        List<HorarioDiaResponse> resultado =
                horarioService.listar(BARBERO_ID, true, USUARIO_ID);

        assertEquals(1, resultado.size());
        assertEquals(BARBERO_ID, resultado.get(0).barberoId());
    }

    @Test
    @DisplayName("sin barberoId el administrador ve todos los horarios")
    void administradorVeTodos() {
        when(horarioRepository.findAllByOrderByBarberoIdAscDiaSemanaAsc())
                .thenReturn(List.of(fila(1), fila(2)));

        List<HorarioDiaResponse> resultado = horarioService.listar(null, true, USUARIO_ID);

        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("un barbero solo puede consultar el horario propio")
    void barberoNoConsultaHorarioAjeno() {
        when(barberoRepository.findByUsuarioId(USUARIO_ID))
                .thenReturn(Optional.of(Barbero.crear(USUARIO_ID)));

        assertThrows(AccessDeniedException.class,
                () -> horarioService.listar(OTRO_BARBERO_ID, false, USUARIO_ID));

        verify(horarioRepository, never()).findByBarberoIdOrderByDiaSemanaAsc(any());
    }

    @Test
    @DisplayName("una cuenta BARBERO sin perfil no puede consultar horarios")
    void barberoSinPerfil() {
        when(barberoRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> horarioService.listar(null, false, USUARIO_ID));
    }

    // ------------------------------------------------------------------
    // Fechas especiales
    // ------------------------------------------------------------------

    @Test
    @DisplayName("lista las fechas especiales del barbero")
    void listaExcepcionesDelBarbero() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(excepcionRepository.findByBarberoIdOrderByFechaAsc(BARBERO_ID))
                .thenReturn(List.of(excepcion(FECHA, false)));

        var resultado = horarioService.listarExcepciones(BARBERO_ID, true, USUARIO_ID);

        assertEquals(1, resultado.size());
        assertEquals(FECHA, resultado.get(0).fecha());
        assertFalse(resultado.get(0).cerrado());
        assertEquals(LocalTime.of(11, 0), resultado.get(0).horaInicio());
    }

    @Test
    @DisplayName("un barbero no puede ver las fechas especiales de otro")
    void barberoNoVeExcepcionesAjenas() {
        when(barberoRepository.existsById(OTRO_BARBERO_ID)).thenReturn(true);
        when(barberoRepository.findByUsuarioId(USUARIO_ID))
                .thenReturn(Optional.of(Barbero.crear(USUARIO_ID)));

        assertThrows(AccessDeniedException.class,
                () -> horarioService.listarExcepciones(OTRO_BARBERO_ID, false, USUARIO_ID));

        verifyNoInteractions(excepcionRepository);
    }

    @Test
    @DisplayName("crea una fecha especial abierta, por ejemplo un domingo")
    void creaExcepcionAbierta() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(excepcionRepository.findByBarberoIdAndFecha(BARBERO_ID, FECHA))
                .thenReturn(Optional.empty());
        when(excepcionRepository.save(any(HorarioExcepcion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = horarioService.guardarExcepcion(BARBERO_ID, FECHA,
                new HorarioExcepcionRequest(false, LocalTime.of(11, 0),
                        LocalTime.of(17, 0), LocalTime.of(13, 0), LocalTime.of(14, 0)));

        assertEquals(FECHA, resultado.fecha());
        assertFalse(resultado.cerrado());
        assertEquals(LocalTime.of(11, 0), resultado.horaInicio());
        assertEquals(LocalTime.of(13, 0), resultado.almuerzoInicio());
    }

    @Test
    @DisplayName("actualiza la fecha especial que ya existía")
    void actualizaExcepcionExistente() {
        HorarioExcepcion existente = excepcion(FECHA, false);
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);
        when(excepcionRepository.findByBarberoIdAndFecha(BARBERO_ID, FECHA))
                .thenReturn(Optional.of(existente));
        when(excepcionRepository.save(existente)).thenReturn(existente);

        var resultado = horarioService.guardarExcepcion(BARBERO_ID, FECHA,
                new HorarioExcepcionRequest(true, null, null, null, null));

        assertTrue(resultado.cerrado());
        assertNull(resultado.horaInicio());
        assertNull(resultado.almuerzoInicio());
        verify(excepcionRepository).save(existente);
    }

    @Test
    @DisplayName("una fecha cerrada no lleva horas")
    void fechaCerradaConHorasFalla() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.guardarExcepcion(BARBERO_ID, FECHA,
                        new HorarioExcepcionRequest(true, LocalTime.of(10, 0),
                                LocalTime.of(19, 0), null, null)));

        assertEquals("EXCEPCION_INVALIDA", error.getCodigo());
        assertEquals("Una fecha cerrada no lleva horas.",
                error.getErrores().get("horario"));
        verify(excepcionRepository, never()).save(any());
    }

    @Test
    @DisplayName("una fecha abierta exige hora de inicio y de fin")
    void fechaAbiertaSinHorasFalla() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.guardarExcepcion(BARBERO_ID, FECHA,
                        new HorarioExcepcionRequest(false, null, null, null, null)));

        assertEquals("Indica la hora de inicio y la hora de fin.",
                error.getErrores().get("horario"));
    }

    @Test
    @DisplayName("una fecha especial respeta la jornada de 10:00 a 19:00")
    void excepcionFueraDeJornadaFalla() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.guardarExcepcion(BARBERO_ID, FECHA,
                        new HorarioExcepcionRequest(false, LocalTime.of(9, 0),
                                LocalTime.of(19, 0), null, null)));

        assertEquals("La jornada debe estar entre las 10:00 y las 19:00 (RN-01).",
                error.getErrores().get("horario"));
    }

    @Test
    @DisplayName("el almuerzo de una fecha especial debe quedar dentro")
    void excepcionAlmuerzoFueraFalla() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> horarioService.guardarExcepcion(BARBERO_ID, FECHA,
                        new HorarioExcepcionRequest(false, LocalTime.of(10, 0),
                                LocalTime.of(19, 0), LocalTime.of(18, 30),
                                LocalTime.of(19, 0))));

        assertEquals("El almuerzo debe quedar dentro de la jornada.",
                error.getErrores().get("horario"));
    }

    @Test
    @DisplayName("elimina la fecha especial del barbero")
    void eliminaExcepcion() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(true);

        horarioService.eliminarExcepcion(BARBERO_ID, FECHA);

        verify(excepcionRepository).deleteByBarberoIdAndFecha(BARBERO_ID, FECHA);
    }

    @Test
    @DisplayName("un perfil inexistente no guarda fechas especiales")
    void barberoInexistenteNoGuardaExcepcion() {
        when(barberoRepository.existsById(BARBERO_ID)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class,
                () -> horarioService.guardarExcepcion(BARBERO_ID, FECHA,
                        new HorarioExcepcionRequest(true, null, null, null, null)));

        verifyNoInteractions(excepcionRepository);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private static HorarioDiaRequest dia(int dia, String inicio, String fin,
                                         String almuerzoInicio, String almuerzoFin) {
        return new HorarioDiaRequest(dia, LocalTime.parse(inicio), LocalTime.parse(fin),
                almuerzoInicio == null ? null : LocalTime.parse(almuerzoInicio),
                almuerzoFin == null ? null : LocalTime.parse(almuerzoFin));
    }

    private static HorarioAtencion fila(int dia) {
        return HorarioAtencion.builder()
                .id(UUID.randomUUID())
                .barberoId(BARBERO_ID)
                .diaSemana((short) dia)
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(19, 0))
                .build();
    }

    private static HorarioExcepcion excepcion(LocalDate fecha, boolean cerrado) {
        return HorarioExcepcion.builder()
                .id(UUID.randomUUID())
                .barberoId(BARBERO_ID)
                .fecha(fecha)
                .cerrado(cerrado)
                .horaInicio(cerrado ? null : LocalTime.of(11, 0))
                .horaFin(cerrado ? null : LocalTime.of(17, 0))
                .build();
    }
}
