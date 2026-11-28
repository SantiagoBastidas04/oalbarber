package com.oalbarber.api.modules.cita.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oalbarber.api.modules.cita.domain.Cita;
import com.oalbarber.api.modules.cita.domain.EstadoCita;

/** Consultas en lote de citas que ocupan parte de la ventana solicitada. */
public interface CitaRepository extends JpaRepository<Cita, UUID> {

    @Query("select c from Cita c where c.barberoId in :barberoIds "
            + "and c.estado <> :cancelada and c.inicio < :fin and c.fin > :inicio")
    List<Cita> buscarOcupadas(@Param("barberoIds") Collection<UUID> barberoIds,
                              @Param("cancelada") EstadoCita cancelada,
                              @Param("inicio") Instant inicio,
                              @Param("fin") Instant fin);
}
