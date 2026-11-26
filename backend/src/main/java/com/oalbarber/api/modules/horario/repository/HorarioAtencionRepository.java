package com.oalbarber.api.modules.horario.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oalbarber.api.modules.horario.domain.HorarioAtencion;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, UUID> {

    List<HorarioAtencion> findByBarberoIdOrderByDiaSemanaAsc(UUID barberoId);

    List<HorarioAtencion> findAllByOrderByBarberoIdAscDiaSemanaAsc();

    void deleteByBarberoId(UUID barberoId);
}
