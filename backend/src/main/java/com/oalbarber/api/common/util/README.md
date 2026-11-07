# Utilidades comunes

Paquete `com.oalbarber.api.common.util`.

## Qué debe ir en esta carpeta

- Utilidades stateless y sin dependencia de módulos: manejo de fechas y horas en
  `America/Bogota`, formato y validación del celular internacional (`+57...`), cálculos de
  intervalos y conversión de `timestamptz`.
- Pequeños helpers puros (sin acceso a base de datos ni a HTTP) que se usen en más de un módulo.

## Qué NO debe ir en esta carpeta

- Reglas de negocio: por ejemplo, "una cita no puede solaparse" o "se cancela hasta 2 horas
  antes". Eso vive en el service del módulo `cita`.
- Acceso a repositorios, `EntityManager`, `SecurityContextHolder` o `HttpServletRequest`.
- Beans con estado, cachés o contadores compartidos.
- Dependencias de Spring Web o de un módulo concreto.

## Convenciones

- Clases finales terminadas en `Util`, con constructor privado y métodos estáticos.
- Métodos en camelCase y en español: `formatearFecha`, `normalizarCelular`, `calcularFin`.
- Nada de `static` mutable; si se necesita configuración, se inyecta en el service que la usa.
- Cada método con Javadoc que indique el formato esperado y el comportamiento ante nulos.
