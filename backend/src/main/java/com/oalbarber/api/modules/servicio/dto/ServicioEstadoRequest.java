package com.oalbarber.api.modules.servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ServicioEstadoRequest", description = "Activa o desactiva un servicio")
public class ServicioEstadoRequest {
    @NotNull(message = "Indica si el servicio queda activo")
    private Boolean activo;
}
