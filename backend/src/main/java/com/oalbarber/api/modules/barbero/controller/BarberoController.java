package com.oalbarber.api.modules.barbero.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oalbarber.api.modules.barbero.dto.BarberoAdminResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoPublicoResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoRequest;
import com.oalbarber.api.modules.barbero.dto.CambiarEstadoBarberoRequest;
import com.oalbarber.api.modules.barbero.service.BarberoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Adaptador HTTP para el catálogo público y la administración de perfiles. */
@Tag(name = "Barberos", description = "Consulta y administración de perfiles de barbero")
@RestController
@RequestMapping("/api/barberos")
@RequiredArgsConstructor
public class BarberoController {

    private final BarberoService barberoService;

    /** Lista solo perfiles activos con los datos mínimos de presentación pública. */
    @Operation(summary = "Listar barberos activos",
            description = "Endpoint público. No devuelve nombre de cuenta ni celular.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfiles activos"),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<BarberoPublicoResponse> listarActivos() {
        return barberoService.listarActivos();
    }

    /** Lista perfiles activos e inactivos y los datos de cuenta solo para administradores. */
    @Operation(summary = "Listar todos los barberos",
            description = "Incluye perfiles activos e inactivos y datos de cuenta. Solo ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfiles administrativos"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/admin")
    public List<BarberoAdminResponse> listarTodos() {
        return barberoService.listarTodos();
    }

    /** Crea una cuenta BARBERO y su perfil; el rol nunca viene del cliente. */
    @Operation(summary = "Crear un barbero",
            description = "Crea cuenta y perfil en una transacción. Solo ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perfil creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El celular ya está registrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<BarberoAdminResponse> crear(@Valid @RequestBody BarberoRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(barberoService.crear(solicitud));
    }

    /** Cambia el estado del perfil; conserva activa la cuenta y sus sesiones. */
    @Operation(summary = "Cambiar el estado de un barbero",
            description = "Activa o desactiva solo el perfil. No desactiva la cuenta ni sus sesiones. "
                    + "Solo ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No existe el perfil indicado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PatchMapping("/{id}/estado")
    public BarberoAdminResponse cambiarEstado(
            @Parameter(description = "UUID del perfil") @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoBarberoRequest solicitud) {
        return barberoService.cambiarEstado(id, solicitud.activo());
    }
}
