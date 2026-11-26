package com.oalbarber.api.modules.horario.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oalbarber.api.modules.horario.domain.HorarioExcepcion;

public interface HorarioExcepcionRepository extends JpaRepository<HorarioExcepcion, UUID> {

    List<HorarioExcepcion> findByBarberoIdOrderByFechaAsc(UUID barberoId);

    Optional<HorarioExcepcion> findByBarberoIdAndFecha(UUID barberoId, LocalDate fecha);

    void deleteByBarberoIdAndFecha(UUID barberoId, LocalDate fecha);
}
