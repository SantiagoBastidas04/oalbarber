CREATE TABLE servicio
(
    id           uuid PRIMARY KEY,
    nombre       varchar(100)   NOT NULL,
    descripcion  text,
    duracion_min integer        NOT NULL,
    precio       numeric(12, 2) NOT NULL,
    imagen       varchar(255),
    activo       boolean        NOT NULL DEFAULT true,

    CONSTRAINT uk_servicio_nombre UNIQUE (nombre),
    CONSTRAINT ck_servicio_duracion CHECK (duracion_min > 0),
    CONSTRAINT ck_servicio_precio CHECK (precio >= 0)
);

CREATE INDEX idx_servicio_activo ON servicio (nombre) WHERE activo = true;