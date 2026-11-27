package com.oalbarber.api.modules.horario.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oalbarber.api.modules.horario.domain.HorarioAtencion;

/** Consultas de horarios semanales por barbero y dia. */
public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, UUID> {

    Optional<HorarioAtencion> findByBarberoIdAndDiaSemana(UUID barberoId, short diaSemana);

    List<HorarioAtencion> findByBarberoIdInAndDiaSemana(Collection<UUID> barberoIds,
                                                        short diaSemana);
}
