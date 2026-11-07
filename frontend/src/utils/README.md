# Utilidades (`utils`)

## Qué debe ir aquí

- `fechas.ts`: `formatearFecha`, `formatearHora` y `formatearFechaHora`, siempre en
  `America/Bogota` y con `Intl` en `es-CO`.
- `moneda.ts`: `formatearMoneda`, pesos colombianos (COP) sin decimales.
- `celular.ts`: `normalizarCelular`, que devuelve `+57XXXXXXXXXX` para un celular colombiano de
  10 dígitos que empieza por 3, con o sin `+57`, o `null` si no es válido.
- Las pruebas unitarias junto al archivo (`*.test.ts`).

## Qué NO debe ir aquí

- Reglas de negocio: no se decide aquí si una cita se puede cancelar ni qué horarios se ofrecen.
- Acceso a la API, al DOM o al estado de React.
- Dependencias nuevas solo para formatear: `Intl` viene en el navegador.
- Funciones con estado o efectos secundarios.

## Convenciones

- Funciones puras exportadas con nombre, en camelCase y en español.
- Sin `Date` global implícita: reciben la cadena ISO y devuelven texto.
- Cada función nueva lleva prueba unitaria y un caso límite (valor inválido, cero, cambio de día
  por la zona horaria).
- No se formatean fechas ni precios dentro de los componentes: siempre con estas utilidades.
