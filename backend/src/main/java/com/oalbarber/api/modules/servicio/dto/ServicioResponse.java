package com.oalbarber.api.modules.servicio.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Servicio del catálogo tal como lo ve el visitante. La ruta de la foto no se
 * envía: el frontend la arma con {@code /servicios/<imagen>} (RN-03).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ServicioResponse", description = "Servicio del catálogo")
public class ServicioResponse {

    @Schema(description = "Identificador del servicio",
            example = "9a7c1e2d-4b3f-4a6e-8c1d-2f5a7b9c0d1e")
    private UUID id;

    @Schema(description = "Nombre visible", example = "Corte normal")
    private String nombre;

    @Schema(description = "Descripción breve", example = "Corte de cabello clásico")
    private String descripcion;

    @Schema(description = "Duración en minutos (RN-03)", example = "40")
    private int duracionMin;

    @Schema(description = "Precio en pesos colombianos, igual para ambos barberos (RN-03)",
            example = "25000")
    private BigDecimal precio;

    @Schema(description = "Nombre del archivo estático de frontend/public/servicios/",
            example = "corte-normal.jpg")
    private String imagen;

    @Schema(description = "Si el servicio se ofrece en el catálogo público", example = "true")
    private boolean activo;
}
