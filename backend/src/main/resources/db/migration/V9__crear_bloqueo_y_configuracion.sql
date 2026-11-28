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
    ('limite_cancelacion_horas', '2'),
    ('maximo_dias_adelante', '7'),
    ('intervalo_turnos_minutos', '40'),
    ('margen_entre_citas_minutos', '0');
