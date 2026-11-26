-- V8: fechas especiales por barbero.
-- Permiten abrir un domingo, cerrar un festivo o cambiar el horario de una
-- fecha concreta sin tocar la semana habitual.
CREATE TABLE horario_excepcion
(
    id              uuid PRIMARY KEY,
    barbero_id      uuid    NOT NULL,
    fecha           date    NOT NULL,
    cerrado         boolean NOT NULL,
    hora_inicio     time,
    hora_fin        time,
    almuerzo_inicio time,
    almuerzo_fin    time,

    CONSTRAINT fk_horario_excepcion_barbero
        FOREIGN KEY (barbero_id) REFERENCES barbero (id) ON DELETE CASCADE,
    CONSTRAINT uk_horario_excepcion_barbero_fecha
        UNIQUE (barbero_id, fecha),
    CONSTRAINT ck_horario_excepcion_horas
        CHECK (
            (cerrado = true
                AND hora_inicio IS NULL
                AND hora_fin IS NULL
                AND almuerzo_inicio IS NULL
                AND almuerzo_fin IS NULL)
            OR (cerrado = false
                AND hora_inicio IS NOT NULL
                AND hora_fin IS NOT NULL
                AND hora_inicio < hora_fin
                AND (almuerzo_inicio IS NULL) = (almuerzo_fin IS NULL)
                AND (almuerzo_inicio IS NULL
                    OR (almuerzo_inicio > hora_inicio
                        AND almuerzo_fin < hora_fin
                        AND almuerzo_inicio < almuerzo_fin)))
        )
);

CREATE INDEX idx_horario_excepcion_barbero ON horario_excepcion (barbero_id);
