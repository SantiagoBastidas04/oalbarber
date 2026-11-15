CREATE TABLE barbero (
    id              uuid          PRIMARY KEY,
    usuario_id      uuid          NOT NULL,
    nombre_publico  varchar(100),
    descripcion     text,
    foto            varchar(255),
    activo          boolean       NOT NULL DEFAULT true,

    CONSTRAINT uk_barbero_usuario_id UNIQUE (usuario_id),
    CONSTRAINT fk_barbero_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE RESTRICT
);
