CREATE TABLE horario_atencion
(
    id              uuid PRIMARY KEY,
    barbero_id      uuid     NOT NULL,
    dia_semana      smallint NOT NULL,
    hora_inicio     time     NOT NULL,
    hora_fin        time     NOT NULL,
    almuerzo_inicio time,
    almuerzo_fin    time,

    CONSTRAINT fk_horario_atencion_barbero
        FOREIGN KEY (barbero_id) REFERENCES barbero (id) ON DELETE RESTRICT,
    CONSTRAINT uk_horario_atencion_barbero_dia
        UNIQUE (barbero_id, dia_semana),
    CONSTRAINT ck_horario_atencion_dia
        CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT ck_horario_atencion_horas
        CHECK (hora_inicio < hora_fin),
    CONSTRAINT ck_horario_atencion_almuerzo_par
        CHECK ((almuerzo_inicio IS NULL) = (almuerzo_fin IS NULL)),
    CONSTRAINT ck_horario_atencion_almuerzo_dentro
        CHECK (
            almuerzo_inicio IS NULL
            OR (almuerzo_inicio > hora_inicio
                AND almuerzo_fin < hora_fin
                AND almuerzo_inicio < almuerzo_fin)
        )
);

CREATE INDEX idx_horario_atencion_barbero ON horario_atencion (barbero_id);
