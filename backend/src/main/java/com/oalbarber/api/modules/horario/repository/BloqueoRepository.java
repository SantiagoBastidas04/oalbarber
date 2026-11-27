package com.oalbarber.api.modules.horario.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oalbarber.api.modules.horario.domain.Bloqueo;

/** Consultas por lote de bloqueos que se cruzan con una ventana de agenda. */
public interface BloqueoRepository extends JpaRepository<Bloqueo, UUID> {

    @Query("select b from Bloqueo b where b.barberoId in :barberoIds "
            + "and b.inicio < :fin and b.fin > :inicio")
    List<Bloqueo> buscarCruces(@Param("barberoIds") Collection<UUID> barberoIds,
                               @Param("inicio") Instant inicio,
                               @Param("fin") Instant fin);
}
