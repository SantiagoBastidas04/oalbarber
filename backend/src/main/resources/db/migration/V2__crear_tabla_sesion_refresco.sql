-- V2: una fila por dispositivo con sesión iniciada.
-- Guardamos el hash del refresh token, nunca el token en claro.
CREATE TABLE sesion_refresco (
    id                 uuid          PRIMARY KEY,
    usuario_id         uuid          NOT NULL,
    hash_token         varchar(64)   NOT NULL,
    agente_usuario     varchar(255),
    ip                 varchar(45),
    fecha_creacion     timestamptz   NOT NULL DEFAULT now(),
    ultima_actividad   timestamptz   NOT NULL DEFAULT now(),
    fecha_expiracion   timestamptz   NOT NULL,
    revocado           boolean       NOT NULL DEFAULT false,
    fecha_revocacion   timestamptz,
    reemplazado_por_id uuid,

    CONSTRAINT fk_sesion_refresco_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_sesion_refresco_reemplazo
        FOREIGN KEY (reemplazado_por_id) REFERENCES sesion_refresco (id) ON DELETE SET NULL,
    CONSTRAINT uk_sesion_refresco_hash UNIQUE (hash_token),
    -- Una sesión revocada siempre tiene fecha; una activa nunca.
    CONSTRAINT ck_sesion_refresco_revocado
        CHECK ((revocado = false AND fecha_revocacion IS NULL)
            OR (revocado = true AND fecha_revocacion IS NOT NULL))
);

CREATE INDEX idx_sesion_refresco_usuario ON sesion_refresco (usuario_id);

-- Índice parcial: solo las sesiones vivas, que son las que consultamos.
CREATE INDEX idx_sesion_refresco_activas
    ON sesion_refresco (usuario_id, fecha_expiracion)
    WHERE revocado = false;