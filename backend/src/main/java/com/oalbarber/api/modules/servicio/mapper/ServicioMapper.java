package com.oalbarber.api.modules.servicio.mapper;

import com.oalbarber.api.modules.servicio.domain.Servicio;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServicioMapper {
    ServicioResponse toResponse(Servicio servicio);
}
