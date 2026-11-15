package com.oalbarber.api.modules.servicio.controller;

import java.util.List;

import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.service.ServicioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Catálogo público de servicios (RF-07). El controlador solo recibe la
 * petición y delega en el servicio; el filtro de activos vive en
 * {@link ServicioService} y la regla de quién puede ver los inactivos se
 * declara en {@link PreAuthorize}.
 */
@Tag(name = "Servicios", description = "Catálogo de servicios")
@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    /**
     * Lista el catálogo. Sin parámetros devuelve solo los servicios activos;
     * un ADMINISTRADOR puede pedir también los inactivos. Para cualquier otro
     * rol, o para un visitante anónimo, esa opción responde 403.
     */
    @Operation(summary = "Listar el catálogo de servicios",
            description = "Público. Devuelve los servicios activos con nombre, duración, "
                    + "precio y nombre del archivo de foto (RF-07). Los servicios inactivos solo "
                    + "se incluyen si quien consulta es ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo de servicios"),
            @ApiResponse(responseCode = "403", description = "Se pidieron inactivos sin ser administrador",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("!#incluirInactivos or hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<ServicioResponse> listar(
            @Parameter(description = "Incluir también los servicios desactivados (solo ADMINISTRADOR)",
                    example = "false")
            @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return servicioService.listar(incluirInactivos);
    }
}
