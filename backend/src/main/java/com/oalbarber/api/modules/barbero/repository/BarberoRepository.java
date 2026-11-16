package com.oalbarber.api.modules.barbero.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oalbarber.api.modules.barbero.domain.Barbero;

/** Consultas de persistencia para los perfiles de barbero. */
public interface BarberoRepository extends JpaRepository<Barbero, UUID> {

    List<Barbero> findByActivoTrue();

    Optional<Barbero> findByUsuarioId(UUID usuarioId);

    boolean existsByUsuarioId(UUID usuarioId);
}
