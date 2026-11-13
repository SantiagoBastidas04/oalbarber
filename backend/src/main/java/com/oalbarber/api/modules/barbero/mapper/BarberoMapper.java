package com.oalbarber.api.modules.barbero.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.oalbarber.api.modules.barbero.domain.Barbero;
import com.oalbarber.api.modules.barbero.dto.BarberoAdminResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoPublicoResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoRequest;
import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;

/** Convierte perfiles a las vistas públicas y administrativas. */
@Mapper(componentModel = "spring")
public interface BarberoMapper {

    BarberoPublicoResponse aPublico(Barbero barbero);

    @Mapping(target = "id", source = "barbero.id")
    @Mapping(target = "usuarioId", source = "barbero.usuarioId")
    @Mapping(target = "nombre", source = "usuario.nombre")
    @Mapping(target = "celular", source = "usuario.celular")
    @Mapping(target = "nombrePublico", source = "barbero.nombrePublico")
    @Mapping(target = "descripcion", source = "barbero.descripcion")
    @Mapping(target = "foto", source = "barbero.foto")
    @Mapping(target = "activo", source = "barbero.activo")
    BarberoAdminResponse aAdmin(Barbero barbero, UsuarioResponse usuario);

    /** Construye el perfil mediante la fábrica de dominio, conservando sus invariantes. */
    default Barbero aEntidad(BarberoRequest solicitud, UUID usuarioId) {
        return Barbero.crear(usuarioId,
                normalizarOpcional(solicitud.nombrePublico()),
                normalizarOpcional(solicitud.descripcion()),
                normalizarOpcional(solicitud.foto()));
    }

    private static String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
