ALTER TABLE barbero
    ADD COLUMN fecha_creacion timestamptz NOT NULL DEFAULT now();
