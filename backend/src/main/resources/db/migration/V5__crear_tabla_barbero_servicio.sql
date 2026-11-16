CREATE TABLE barbero_servicio (
    barbero_id  uuid        NOT NULL,
    servicio_id uuid        NOT NULL,
    creado_en   timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT pk_barbero_servicio
        PRIMARY KEY (barbero_id, servicio_id),
    CONSTRAINT fk_barbero_servicio_barbero
        FOREIGN KEY (barbero_id) REFERENCES barbero (id) ON DELETE RESTRICT,
    CONSTRAINT fk_barbero_servicio_servicio
        FOREIGN KEY (servicio_id) REFERENCES servicio (id) ON DELETE RESTRICT
);

CREATE INDEX idx_barbero_servicio_servicio_id ON barbero_servicio (servicio_id);
