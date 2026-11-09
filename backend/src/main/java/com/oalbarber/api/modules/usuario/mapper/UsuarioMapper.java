package com.oalbarber.api.modules.usuario.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import com.oalbarber.api.modules.usuario.domain.Usuario;
import com.oalbarber.api.modules.usuario.dto.UsuarioRequest;
import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;

@Mapper(componentModel = "spring")
public interface UsuarioMapper{

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Usuario toEntity(UsuarioRequest request);

    UsuarioResponse toResponse(Usuario usuario);

}
