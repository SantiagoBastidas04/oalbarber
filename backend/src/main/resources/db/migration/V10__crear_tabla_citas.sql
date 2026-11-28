CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE cita (
    id              uuid PRIMARY KEY,
    cliente_id      uuid NOT NULL,
    barbero_id      uuid NOT NULL,
    servicio_id     uuid NOT NULL,
    inicio          timestamptz NOT NULL,
    fin             timestamptz NOT NULL,
    duracion_min    integer NOT NULL,
    precio          numeric(12, 2) NOT NULL,
    estado          varchar(20) NOT NULL,
    canal           varchar(20) NOT NULL,
    creada_en       timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_cita_cliente
        FOREIGN KEY (cliente_id) REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_cita_barbero
        FOREIGN KEY (barbero_id) REFERENCES barbero (id) ON DELETE RESTRICT,
    CONSTRAINT fk_cita_servicio
        FOREIGN KEY (servicio_id) REFERENCES servicio (id) ON DELETE RESTRICT,
    CONSTRAINT ck_cita_rango CHECK (inicio < fin),
    CONSTRAINT ck_cita_duracion CHECK (duracion_min > 0),
    CONSTRAINT ck_cita_precio CHECK (precio >= 0),
    CONSTRAINT ck_cita_estado CHECK (
        estado IN ('CONFIRMADA', 'CANCELADA', 'COMPLETADA', 'NO_ASISTIO')
    ),
    CONSTRAINT ck_cita_canal CHECK (canal IN ('WEB', 'WHATSAPP', 'ADMIN')),
    CONSTRAINT ex_cita_barbero_rango
        EXCLUDE USING gist (
            barbero_id WITH =,
            tstzrange(inicio, fin, '[)') WITH &&
        ) WHERE (estado <> 'CANCELADA')
);

CREATE INDEX idx_cita_barbero_inicio_vigente
    ON cita (barbero_id, inicio) WHERE estado <> 'CANCELADA';
