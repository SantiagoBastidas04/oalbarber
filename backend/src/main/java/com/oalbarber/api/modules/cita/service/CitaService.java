package com.oalbarber.api.modules.cita.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.modules.cita.domain.EstadoCita;
import com.oalbarber.api.modules.cita.dto.IntervaloOcupadoResponse;
import com.oalbarber.api.modules.cita.repository.CitaRepository;

/** Lecturas minimas de agenda necesarias para calcular disponibilidad (RN-05 a RN-10). */
@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    /** Lee citas de todos los barberos candidatos en una consulta y omite las canceladas. */
    @Transactional(readOnly = true)
    public List<IntervaloOcupadoResponse> listarOcupadas(Collection<java.util.UUID> barberoIds,
                                                         Instant inicio, Instant fin) {
        if (barberoIds.isEmpty()) {
            return List.of();
        }
        return citaRepository.buscarOcupadas(barberoIds, EstadoCita.CANCELADA, inicio, fin).stream()
                .map(cita -> new IntervaloOcupadoResponse(
                        cita.getBarberoId(), cita.getInicio(), cita.getFin()))
                .toList();
    }
}
