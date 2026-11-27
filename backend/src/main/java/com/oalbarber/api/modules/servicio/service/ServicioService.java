package com.oalbarber.api.modules.servicio.service;

import com.oalbarber.api.common.exception.RecursoDuplicadoException;
import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.modules.servicio.domain.Servicio;
import com.oalbarber.api.modules.servicio.dto.ServicioRequest;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.mapper.ServicioMapper;
import com.oalbarber.api.modules.servicio.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServicioService {
    private final ServicioRepository servicioRepository;
    private final ServicioMapper servicioMapper;

    public ServicioService(ServicioRepository servicioRepository, ServicioMapper servicioMapper) {
        this.servicioMapper = servicioMapper;
        this.servicioRepository = servicioRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar(boolean incluirInactivos) {
        List<Servicio> servicios = incluirInactivos
                ? servicioRepository.findAllByOrderByNombreAsc()
                : servicioRepository.findByActivoTrueOrderByNombreAsc();

        return servicios.stream().map(servicioMapper::toResponse).toList();
    }

    /** Obtiene un servicio activo para que cita use su duracion sin acceder al repositorio ajeno. */
    @Transactional(readOnly = true)
    public ServicioResponse obtenerActivo(UUID id) {
        Servicio servicio = servicioRepository.findById(id)
                .filter(Servicio::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un servicio activo con el identificador indicado."));
        return servicioMapper.toResponse(servicio);
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest peticion) {
        String nombre = peticion.getNombre().trim();
        if (servicioRepository.existsByNombreIgnoreCase(nombre)) {
            throw new RecursoDuplicadoException("NOMBRE_DUPLICADO",
                    "Ya existe un servicio con el nombre :" + nombre);
        }
        Servicio servicio = servicioMapper.toEntity(peticion);
        servicio.setNombre(nombre);
        servicio.setActivo(true);
        return servicioMapper.toResponse(servicioRepository.save(servicio));
    }

    @Transactional
    public ServicioResponse actualizar(UUID id, ServicioRequest peticion) {
        Servicio servicio = buscar(id);
        String nombre = peticion.getNombre().trim();
        if (servicioRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new RecursoDuplicadoException("NOMBRE_DUPLICADO",
                    "Ya existe otro servicio con el nombre :" + nombre);
        }
        servicioMapper.actualizar(peticion, servicio);
        servicio.setNombre(nombre);
        return servicioMapper.toResponse(servicio);
    }


    @Transactional
    public ServicioResponse cambiarEstado(UUID id, boolean activo) {
        Servicio servicio = buscar(id);
        servicio.setActivo(activo);
        return servicioMapper.toResponse(servicio);
    }

    private Servicio buscar(UUID id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el servicio: " + id));
    }
}
