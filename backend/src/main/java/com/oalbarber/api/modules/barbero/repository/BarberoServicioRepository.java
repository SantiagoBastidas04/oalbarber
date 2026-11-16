package com.oalbarber.api.modules.barbero.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oalbarber.api.modules.barbero.domain.BarberoServicio;
import com.oalbarber.api.modules.barbero.domain.BarberoServicioId;

/** Consultas para las asignaciones barbero-servicio. */
public interface BarberoServicioRepository extends JpaRepository<BarberoServicio, BarberoServicioId> {

    List<BarberoServicio> findById_BarberoId(UUID barberoId);

    List<BarberoServicio> findById_ServicioId(UUID servicioId);

    boolean existsById_BarberoIdAndId_ServicioId(UUID barberoId, UUID servicioId);

    long deleteById_BarberoIdAndId_ServicioId(UUID barberoId, UUID servicioId);
}
