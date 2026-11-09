CREATE TABLE usuario(
    id              uuid          PRIMARY KEY,
    nombre          varchar(100)  NOT NULL,
    celular         varchar(20)   NOT NULL,
    contrasena_hash varchar(100)  NOT NULL,
    rol             varchar(30)   NOT NULL,
    activo          boolean       NOT NULL DEFAULT true,
    fecha_creacion  timestamptz   NOT NULL DEFAULT now(),

    CONSTRAINT uk_usuarios_celular UNIQUE (celular),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('CLIENTE', 'BARBERO', 'ADMINISTRADOR'))
);