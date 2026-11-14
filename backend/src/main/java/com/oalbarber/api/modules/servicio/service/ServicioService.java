package com.oalbarber.api.modules.servicio.service;

import com.oalbarber.api.modules.servicio.domain.Servicio;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.mapper.ServicioMapper;
import com.oalbarber.api.modules.servicio.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioService {
    private final ServicioRepository servicioRepository;
    private final ServicioMapper servicioMapper;

    public ServicioService(ServicioRepository servicioRepository, ServicioMapper servicioMapper){
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
}
