-- P-05 sigue pendiente: COP 1.00 es un marcador técnico provisional, no un precio aprobado.
-- Sustituirlo por los precios confirmados antes de publicar el catálogo en producción.
COMMENT ON COLUMN servicio.precio IS
    'Los registros creados por V9 usan COP 1.00 como marcador provisional mientras P-05 siga pendiente.';

INSERT INTO servicio (id, nombre, descripcion, duracion_min, precio, imagen, activo) VALUES
    ('90000000-0000-4000-8000-000000000001', 'Corte normal',
     'Precio provisional pendiente de confirmación (P-05).', 40, 1.00, 'corte-normal.jpg', true),
    ('90000000-0000-4000-8000-000000000002', 'Corte premium',
     'Precio provisional pendiente de confirmación (P-05).', 90, 1.00, 'corte-premium.jpg', true),
    ('90000000-0000-4000-8000-000000000003', 'Mechas',
     'Precio provisional pendiente de confirmación (P-05).', 180, 1.00, 'mechas.jpg', true),
    ('90000000-0000-4000-8000-000000000004', 'Ondulación',
     'Precio provisional pendiente de confirmación (P-05).', 180, 1.00, 'ondulacion.jpg', true);
