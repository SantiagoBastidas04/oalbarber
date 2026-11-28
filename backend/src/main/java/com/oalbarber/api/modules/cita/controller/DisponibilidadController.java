package com.oalbarber.api.modules.cita.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oalbarber.api.modules.cita.dto.DisponibilidadResponse;
import com.oalbarber.api.modules.cita.service.DisponibilidadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;

/** Adaptador HTTP público para consultar opciones de agenda (RF-15 y RF-16). */
@Tag(name = "Disponibilidad", description = "Consulta pública de turnos reservables")
@RestController
@RequestMapping("/api/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    /**
     * Consulta los turnos válidos para un servicio y fecha. Si se omite {@code barberoId}, se
     * buscan todos los perfiles activos que ofrezcan el servicio.
     *
     * @param servicioId identificador UUID del servicio
     * @param fecha fecha local ISO {@code AAAA-MM-DD}
     * @param barberoId perfil opcional; ausente significa cualquiera disponible
     * @return turnos con inicio, fin y barbero asignado
     */
    @Operation(summary = "Consultar disponibilidad",
            description = "Público. Devuelve horarios libres para el servicio y fecha indicados. "
                    + "Sin barberoId, busca cualquier barbero activo que ofrezca el servicio. "
                    + "Las fechas y horas de respuesta incluyen el offset -05:00 de Bogotá.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turnos disponibles",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = DisponibilidadResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Falta un parámetro o su formato es inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "El servicio no existe o está inactivo",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El barbero no está disponible para el servicio",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<DisponibilidadResponse> consultar(
            @Parameter(description = "UUID de un servicio activo", required = true)
            @RequestParam("servicioId") UUID servicioId,
            @Parameter(description = "Fecha local en formato AAAA-MM-DD", required = true,
                    example = "2026-11-26")
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(description = "UUID de barbero; omitir para cualquiera disponible")
            @RequestParam(value = "barberoId", required = false) UUID barberoId) {
        return disponibilidadService.calcularDisponibilidad(servicioId, fecha, barberoId);
    }
}
