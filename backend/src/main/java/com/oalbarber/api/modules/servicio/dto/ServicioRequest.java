package com.oalbarber.api.modules.servicio.dto;

import com.oalbarber.api.modules.servicio.validation.DuracionPermitida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ServicioRequest", description = "Datos para crear o editar un servicio")
public class ServicioRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede pasar de 1000 caracteres")
    private String descripcion;

    @NotNull(message = "La duración es obligatoria")
    @DuracionPermitida            // 40, 90 o 180
    private Integer duracionMin;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2, message = "El precio admite máximo dos decimales")
    private BigDecimal precio;

    @Pattern(regexp = "^[\\w .-]+\\.(jpg|jpeg|png|webp)$",
            message = "Usa solo el nombre del archivo, con extensión .jpg, .png o .webp")
    private String imagen;
}
