package com.oalbarber.api.modules.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oalbarber.api.modules.auth.domain.SesionRefresco;

public interface SesionRefrescoRepository extends JpaRepository<SesionRefresco, UUID> {

    Optional<SesionRefresco> findByHashToken(String hashToken);

    List<SesionRefresco> findByUsuarioIdAndRevocadoFalseAndFechaExpiracionAfter(
            UUID usuarioId, Instant ahora);

    /** Cierra de golpe todas las sesiones vivas del usuario (uso anómalo detectado). */
    @Modifying
    @Query("""
            update SesionRefresco s
            set s.revocado = true, s.fechaRevocacion = :cuando
            where s.usuario.id = :usuarioId and s.revocado = false
            """)
    int revocarTodas(@Param("usuarioId") UUID usuarioId, @Param("cuando") Instant cuando);
}
