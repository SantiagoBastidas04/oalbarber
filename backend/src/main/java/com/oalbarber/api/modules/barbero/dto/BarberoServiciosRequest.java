package com.oalbarber.api.modules.barbero.dto;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/** Lista completa de servicios que quedarán asignados a un perfil. */
@Schema(name = "BarberoServiciosRequest", description = "Reemplazo de servicios asignados a un barbero")
public record BarberoServiciosRequest(

        @NotNull(message = "La lista de servicios es obligatoria.")
        @Schema(description = "UUIDs de servicios activos; una lista vacía quita todas las asignaciones")
        List<@NotNull(message = "El identificador del servicio es obligatorio.") UUID> servicioIds) {

    @AssertTrue(message = "La lista de servicios no puede contener identificadores duplicados.")
    public boolean isListaSinDuplicados() {
        return servicioIds == null || new HashSet<>(servicioIds).size() == servicioIds.size();
    }
}
