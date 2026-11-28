package com.oalbarber.api.modules.horario.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oalbarber.api.modules.horario.dto.HorarioDiaResponse;
import com.oalbarber.api.modules.horario.dto.HorarioExcepcionRequest;
import com.oalbarber.api.modules.horario.dto.HorarioExcepcionResponse;
import com.oalbarber.api.modules.horario.dto.HorarioSemanaRequest;
import com.oalbarber.api.modules.horario.service.HorarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** Adaptador HTTP del horario de atención por barbero (RF-11). */
@Tag(name = "Horarios", description = "Horarios de atención por barbero, incluido el almuerzo")
@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private static final String ROL_ADMINISTRADOR = "ROLE_ADMINISTRADOR";

    private final HorarioService horarioService;

    /**
     * Consulta la semana. Sin {@code barberoId} el administrador ve todos los
     * horarios; un barbero siempre ve el suyo.
     */
    @Operation(summary = "Consultar horarios",
            description = "ADMINISTRADOR puede consultar cualquier perfil o todos; "
                    + "BARBERO solo el propio. Solo se devuelven los días configurados: "
                    + "los ausentes están cerrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Días configurados"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Rol sin permiso o barbero ajeno",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No existe el perfil de barbero",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BARBERO')")
    @GetMapping
    public List<HorarioDiaResponse> listar(
            @Parameter(description = "UUID del perfil; omitido, el administrador ve todos")
            @RequestParam(required = false) UUID barberoId,
            Authentication autenticacion) {
        return horarioService.listar(barberoId, esAdministrador(autenticacion),
                UUID.fromString(autenticacion.getName()));
    }

    /** Reemplaza la semana completa; el domingo no se puede configurar (RN-01). */
    @Operation(summary = "Reemplazar el horario semanal",
            description = "Solo ADMINISTRADOR. Reemplaza los días del perfil: lo que no venga "
                    + "queda cerrado y una lista vacía cierra la semana. Valida jornada "
                    + "10:00-19:00, inicio < fin y almuerzo dentro de la jornada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horario actualizado"),
            @ApiResponse(responseCode = "400", description = "Horario inválido con detalle por día",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No existe el perfil de barbero",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{barberoId}")
    public List<HorarioDiaResponse> reemplazar(
            @Parameter(description = "UUID del perfil") @PathVariable UUID barberoId,
            @RequestBody HorarioSemanaRequest solicitud) {
        return horarioService.reemplazar(barberoId, solicitud);
    }

    // ------------------------------------------------------------------
    // Fechas especiales
    // ------------------------------------------------------------------

    /** Lista las fechas especiales del perfil; un barbero solo ve las suyas. */
    @Operation(summary = "Consultar fechas especiales",
            description = "ADMINISTRADOR puede consultar cualquier perfil; BARBERO solo el propio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fechas especiales configuradas"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Rol sin permiso o barbero ajeno",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No existe el perfil de barbero",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BARBERO')")
    @GetMapping("/{barberoId}/excepciones")
    public List<HorarioExcepcionResponse> listarExcepciones(
            @Parameter(description = "UUID del perfil") @PathVariable UUID barberoId,
            Authentication autenticacion) {
        return horarioService.listarExcepciones(barberoId, esAdministrador(autenticacion),
                UUID.fromString(autenticacion.getName()));
    }

    /** Crea o actualiza el horario de una fecha concreta (por ejemplo, un domingo). */
    @Operation(summary = "Guardar una fecha especial",
            description = "Solo ADMINISTRADOR. Si la fecha ya existe se actualiza. "
                    + "Puede abrir un domingo, cerrar un festivo o cambiar el horario de un día.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fecha especial guardada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No existe el perfil de barbero",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{barberoId}/excepciones/{fecha}")
    public HorarioExcepcionResponse guardarExcepcion(
            @Parameter(description = "UUID del perfil") @PathVariable UUID barberoId,
            @Parameter(description = "Fecha en formato ISO", example = "2026-12-06")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestBody HorarioExcepcionRequest solicitud) {
        return horarioService.guardarExcepcion(barberoId, fecha, solicitud);
    }

    /** Elimina una fecha especial; responde 204 aunque ya no exista. */
    @Operation(summary = "Eliminar una fecha especial",
            description = "Solo ADMINISTRADOR. La fecha vuelve a regirse por la semana habitual.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fecha especial eliminada"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "No existe el perfil de barbero")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{barberoId}/excepciones/{fecha}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarExcepcion(
            @Parameter(description = "UUID del perfil") @PathVariable UUID barberoId,
            @Parameter(description = "Fecha en formato ISO", example = "2026-12-06")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        horarioService.eliminarExcepcion(barberoId, fecha);
    }

    private boolean esAdministrador(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> ROL_ADMINISTRADOR.equals(autoridad.getAuthority()));
    }
}
