package com.oalbarber.api.modules.servicio.repository;

import com.oalbarber.api.modules.servicio.domain.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicioRepository extends JpaRepository<Servicio, UUID> {
    List<Servicio> findByActivoTrueOrderByNombreAsc();
    List<Servicio> findAllByOrderByNombreAsc();
}
