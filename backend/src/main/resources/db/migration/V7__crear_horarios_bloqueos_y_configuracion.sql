CREATE TABLE horario_atencion (
    id              uuid PRIMARY KEY,
    barbero_id      uuid NOT NULL,
    dia_semana      smallint NOT NULL,
    hora_inicio     time NOT NULL,
    hora_fin        time NOT NULL,
    inicio_almuerzo time,
    fin_almuerzo    time,

    CONSTRAINT fk_horario_barbero
        FOREIGN KEY (barbero_id) REFERENCES barbero (id) ON DELETE RESTRICT,
    CONSTRAINT uk_horario_barbero_dia UNIQUE (barbero_id, dia_semana),
    CONSTRAINT ck_horario_dia_semana CHECK (dia_semana BETWEEN 1 AND 6),
    CONSTRAINT ck_horario_rango CHECK (hora_inicio < hora_fin),
    CONSTRAINT ck_horario_almuerzo CHECK (
        (inicio_almuerzo IS NULL AND fin_almuerzo IS NULL)
        OR (inicio_almuerzo IS NOT NULL AND fin_almuerzo IS NOT NULL
            AND inicio_almuerzo < fin_almuerzo
            AND inicio_almuerzo >= hora_inicio AND fin_almuerzo <= hora_fin)
    )
);

CREATE TABLE bloqueo (
    id          uuid PRIMARY KEY,
    barbero_id  uuid NOT NULL,
    inicio      timestamptz NOT NULL,
    fin         timestamptz NOT NULL,
    motivo      varchar(255),

    CONSTRAINT fk_bloqueo_barbero
        FOREIGN KEY (barbero_id) REFERENCES barbero (id) ON DELETE RESTRICT,
    CONSTRAINT ck_bloqueo_rango CHECK (inicio < fin)
);

CREATE INDEX idx_bloqueo_barbero_inicio_fin ON bloqueo (barbero_id, inicio, fin);

CREATE TABLE configuracion (
    clave   varchar(100) PRIMARY KEY,
    valor   varchar(255) NOT NULL
);

INSERT INTO configuracion (clave, valor) VALUES
    ('anticipacion_minima_horas', '24'),
    ('maximo_dias_adelante', '7'),
    ('intervalo_turnos_minutos', '40'),
    ('margen_entre_citas_minutos', '0');
