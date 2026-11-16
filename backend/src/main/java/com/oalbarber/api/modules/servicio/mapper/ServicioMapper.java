package com.oalbarber.api.modules.servicio.mapper;

import com.oalbarber.api.modules.servicio.domain.Servicio;
import com.oalbarber.api.modules.servicio.dto.ServicioRequest;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServicioMapper {
    ServicioResponse toResponse(Servicio servicio);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Servicio toEntity(ServicioRequest peticion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void actualizar(ServicioRequest peticion, @MappingTarget Servicio servicio);
}
