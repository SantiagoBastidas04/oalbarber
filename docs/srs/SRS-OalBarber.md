# SRS — OalBarber (Fase 1: Web de reservas)

**Fecha:** 5 de noviembre de 2026 · **Autor:** @Truchini
**Estado:** aprobado por el cliente, con pendientes por confirmar (sección 11).

---

## 1. Introducción

La fase 1 de OalBarber es una web donde los clientes reservan citas con uno de los dos barberos
de la barbería; el pago se hace en el local. Este documento fija qué debe hacer el sistema, bajo
qué reglas y con qué límites, para que el cliente lo apruebe y el equipo lo construya.

**Propósito.** Servir de acuerdo entre el cliente y el equipo (tres personas: backend, frontend y
despliegue) y de base para el diseño, las pruebas y la aceptación.

**Alcance de la fase 1.** Catálogo de servicios, reserva de citas con elección de barbero,
registro e inicio de sesión de clientes, "Mis citas" con cancelación, panel de administración y
de barbero, y aviso al administrador por cada reserva. El servidor queda funcional y preparado
para sumar después un módulo de WhatsApp sin rehacer la arquitectura.

**Referencias.** Respuestas del cliente al cuestionario de requisitos (noviembre de 2026) y
Ley 1581 de 2012 sobre protección de datos personales en Colombia.

### Glosario

| Término | Significado |
|---|---|
| SRS | Especificación de requisitos de software (este documento). |
| RF / RNF / RN | Requisito funcional / no funcional / regla de negocio. |
| Cita | Reserva de un servicio con un barbero, con hora de inicio y fin exactas. |
| Hueco libre | Intervalo de la agenda de un barbero sin citas, almuerzo ni bloqueos. |
| Bloqueo | Franja o fecha completa que el administrador marca como no disponible. |
| Canal | Origen de una cita: web, administrador o, en la fase 2, WhatsApp. |
| JWT | Token firmado que identifica al usuario en cada petición a la API. |

---

## 2. Descripción general

El sistema es una aplicación web pensada primero para celular, con una API REST central: la web
es el primer canal de reservas y WhatsApp será el segundo. Todos los canales usarán la misma
lógica de agenda, de modo que nunca existan dos versiones de la disponibilidad.

### Roles de usuario

| Rol | Quién es | Qué puede hacer |
|---|---|---|
| CLIENTE | Público que se registra con nombre, celular y contraseña. | Ver el catálogo, reservar, ver sus citas y cancelarlas. |
| BARBERO | Barbero contratado. | Ver y gestionar solo su propia agenda desde el panel. |
| ADMIN | Dueño, que además atiende como barbero. | Todo: servicios, precios, horarios, bloqueos, citas de ambos barberos, citas manuales y cuentas. |

### Restricciones

- Backend en Java, base de datos PostgreSQL y autenticación con JWT.
- Equipo de tres personas y plazo deseado por el cliente de menos de un mes.
- Sin pasarela de pagos: el cliente paga en el local.
- Sin subida ni almacenamiento de imágenes: las fotos del catálogo son archivos estáticos del
  frontend.
- Sin correo electrónico del cliente: solo se guardan nombre y celular.
- El costo de infraestructura no es una restricción: el cliente puede pagar un plan sin límites
  de arranque.

### Supuestos

- Un solo local con dos barberos que ofrecen los mismos servicios, al mismo precio y en el mismo
  horario.
- Zona horaria única: `America/Bogota`.
- La mayoría de los clientes usará la web desde el celular.

### Dependencias

- El cliente no tiene logo, colores, fotos ni textos listos: necesita ayuda del equipo para
  producirlos, y el diseño final depende de ellos.
- El cliente no tiene dominio ni hosting: el equipo lo guía en su compra y configuración.

---

## 3. Reglas de negocio

Estas reglas salen de las respuestas del cliente; las marcadas "Por confirmar" son propuestas del
equipo que el cliente aún debe aprobar.

| ID | Regla | Estado |
|---|---|---|
| RN-01 | Horario de atención: lunes a sábado, de 10:00 a 19:00; domingo cerrado. Es el mismo para ambos barberos y debe ser configurable. | Confirmado |
| RN-02 | El almuerzo es el único descanso y en esa franja no se ofrece agenda. Falta la hora y la duración. | Por confirmar |
| RN-03 | Duración de los servicios: corte normal 40 min, premium 90 min, mechas 180 min, ondulación 180 min. Cada servicio tiene un precio distinto, igual para ambos barberos. | Confirmado |
| RN-04 | La lista final de servicios con sus precios la entrega el cliente. | Por confirmar |
| RN-05 | El cliente elige el barbero al reservar. Se propone añadir la opción "cualquiera disponible", que asigna al barbero con el hueco. | Por confirmar |
| RN-06 | Cada cita ocupa exactamente la duración de su servicio y debe terminar antes del cierre (19:00), sin solaparse con almuerzo, bloqueos ni otras citas. | Por confirmar |
| RN-07 | Los horarios se ofrecen cada 40 minutos (la duración del servicio más corto), contados desde el inicio de cada hueco libre. Al terminar una cita, el siguiente turno libre empieza en ese momento. | Por confirmar |
| RN-08 | Un cliente reserva con al menos 24 horas de anticipación y con una semana adelante como máximo. | Confirmado |
| RN-09 | Un cliente puede cancelar su cita hasta 2 horas antes. | Confirmado |
| RN-10 | Una cita confirmada nunca se mueve sola. La disponibilidad se recalcula solo para ofrecer horarios a nuevas reservas. | Por confirmar |
| RN-11 | Dos citas del mismo barbero nunca se solapan. Si dos clientes reservan a la vez, gana la primera y la segunda ve horarios actualizados. | Confirmado |
| RN-12 | El administrador puede bloquear franjas o fechas completas por barbero. Un bloqueo no cancela citas existentes y, si hay citas dentro del rango, exige confirmación explícita. | Confirmado |
| RN-13 | La atención sin reserva la registra el administrador a mano, a la hora exacta y sin las restricciones de anticipación. | Confirmado |
| RN-14 | El pago se hace en el local; no hay pasarela de pagos. | Confirmado |
| RN-15 | El cliente se registra con nombre, celular (único, en formato internacional) y contraseña, y desde su sesión ve y cancela sus citas. Si olvida la contraseña, el administrador la restablece. | Por confirmar |
| RN-16 | Cada cita guarda el precio y la duración vigentes al reservar; cambiarlos después en el catálogo no la afecta. | Por confirmar |
| RN-17 | Estados de una cita: confirmada, cancelada, completada y no asistió. | Por confirmar |
| RN-18 | Anticipación mínima, límite de cancelación, máximo de días, intervalo entre turnos y margen entre citas (0 por defecto) se configuran desde el panel y no están fijos en el código. | Por confirmar |

---

## 4. Requisitos funcionales

La fase 1 cubre 29 requisitos en seis módulos; los de prioridad Alta forman el mínimo que se
entrega en el primer mes.

| ID | Módulo | Requisito | Prioridad |
|---|---|---|---|
| RF-01 | Autenticación | El cliente se registra con nombre, celular en formato internacional y contraseña, aceptando la política de tratamiento de datos. | Alta |
| RF-02 | Autenticación | El usuario inicia sesión con celular y contraseña y recibe un JWT. | Alta |
| RF-03 | Autenticación | La sesión se renueva sin pedir de nuevo las credenciales mientras el refresh token sea válido. | Media |
| RF-04 | Autenticación | El usuario puede cerrar sesión y el refresh token queda revocado. | Media |
| RF-05 | Autenticación | El administrador restablece la contraseña de un usuario asignándole una clave temporal. | Media |
| RF-06 | Autenticación | El backend valida el rol (CLIENTE, BARBERO, ADMIN) en cada operación protegida. | Alta |
| RF-07 | Catálogo | La web muestra los servicios con nombre, duración, precio y foto. | Alta |
| RF-08 | Catálogo | El administrador crea, edita y desactiva servicios (nombre, descripción, duración y precio). | Alta |
| RF-09 | Catálogo | La página de inicio presenta el negocio, el horario de atención y el acceso al catálogo y a la reserva. | Alta |
| RF-10 | Barberos y horarios | El administrador crea y desactiva barberos (usuarios con rol BARBERO). | Media |
| RF-11 | Barberos y horarios | El administrador configura el horario por día y por barbero, incluido el almuerzo. | Alta |
| RF-12 | Barberos y horarios | El administrador crea bloqueos de franjas o de fechas completas por barbero. | Alta |
| RF-13 | Barberos y horarios | Si un bloqueo cubre citas existentes, el sistema avisa y pide confirmación, y no las cancela. | Alta |
| RF-14 | Barberos y horarios | El administrador edita los parámetros de la RN-18 desde el panel. | Media |
| RF-15 | Reservas | El cliente elige servicio, barbero (o "cualquiera disponible") y fecha. | Alta |
| RF-16 | Reservas | El sistema muestra solo los horarios válidos según duración del servicio, huecos libres, anticipación y máximo de días. | Alta |
| RF-17 | Reservas | Al confirmar, el sistema guarda inicio y fin exactos, precio, duración y canal de la cita. | Alta |
| RF-18 | Reservas | Si el horario se ocupó mientras el cliente reservaba, el sistema lo rechaza y muestra horarios actualizados. | Alta |
| RF-19 | Reservas | "Mis citas" lista las citas próximas y el historial del cliente. | Alta |
| RF-20 | Reservas | El cliente cancela una cita propia hasta 2 horas antes. | Alta |
| RF-21 | Reservas | Fuera de ese plazo, el sistema impide la cancelación e indica contactar al local. | Media |
| RF-22 | Panel | El administrador ve la agenda de ambos barberos por día y por semana. | Alta |
| RF-23 | Panel | El barbero ve y gestiona solo su propia agenda. | Alta |
| RF-24 | Panel | El administrador cancela o reprograma cualquier cita; el barbero, solo las suyas. | Media |
| RF-25 | Panel | El administrador y el barbero (solo las suyas) marcan una cita como completada o no asistió. | Media |
| RF-26 | Panel | El administrador registra atención sin reserva a hora exacta, sin restricciones de anticipación, para un cliente existente o nuevo (nombre y celular). | Alta |
| RF-27 | Panel | El administrador consulta y desactiva cuentas de clientes. | Media |
| RF-28 | Avisos | El administrador recibe un aviso por cada nueva reserva (correo o Telegram, por confirmar). | Alta |
| RF-29 | Avisos | El administrador recibe un aviso cuando un cliente cancela una cita. | Media |

---

## 5. Requisitos no funcionales

La carga esperada es baja (un solo local), así que el énfasis está en seguridad, confiabilidad de
la agenda y facilidad de uso en celular. Las cifras marcadas como objetivo son propuestas del
equipo.

| ID | Categoría | Requisito |
|---|---|---|
| RNF-01 | Seguridad | Las contraseñas se guardan con hash BCrypt, nunca en texto plano. |
| RNF-02 | Seguridad | El JWT de acceso dura de 10 a 15 minutos y se renueva con un refresh token revocable, guardado en una cookie HttpOnly. |
| RNF-03 | Seguridad | La autorización se valida siempre en el backend; el frontend solo oculta opciones. |
| RNF-04 | Seguridad | Hay límite de intentos de inicio de sesión con bloqueo temporal, límite de peticiones por IP y captcha en el registro. |
| RNF-05 | Seguridad | Los secretos viven en variables de entorno, nunca en el repositorio, y producción usa HTTPS. |
| RNF-06 | Integridad | La base de datos impide por sí misma el solapamiento de citas de un mismo barbero, incluso con reservas simultáneas. |
| RNF-07 | Rendimiento | Objetivo: la consulta de horarios disponibles responde en menos de 1 segundo en uso normal. |
| RNF-08 | Usabilidad | Diseño móvil primero; objetivo: un cliente nuevo completa su primera reserva en menos de 3 minutos. |
| RNF-09 | Disponibilidad | El servicio no debe "dormirse" por inactividad y la base de datos tiene copias de seguridad automáticas diarias. |
| RNF-10 | Privacidad | Cumplir la Ley 1581 de 2012: política de tratamiento visible, consentimiento en el registro y solo los datos necesarios (nombre y celular). |
| RNF-11 | Fechas | Zona horaria America/Bogota; las fechas se guardan con zona (`timestamptz`). |
| RNF-12 | Mantenibilidad | Monolito modular, DTOs en la API, errores con formato uniforme, API documentada con OpenAPI y esquema versionado con migraciones. |
| RNF-13 | Calidad | Pruebas automáticas del cálculo de disponibilidad y de las reglas de cancelación, con PostgreSQL real. |
| RNF-14 | Despliegue | Todo el sistema se levanta con docker compose y se despliega con CI/CD desde GitHub. |
| RNF-15 | Extensibilidad | La lógica de reservas no depende de HTTP y cada cita registra su canal, para añadir WhatsApp sin rehacer el módulo. |
| RNF-16 | Compatibilidad | Funciona en las versiones actuales de Chrome, Safari, Firefox y Edge, en celular y escritorio. |

---

## 6. Arquitectura y stack tecnológico

La solución es un monolito modular en Java con API REST, un frontend React separado y
PostgreSQL: con tres personas, los microservicios añadirían despliegue y coordinación que no
compensan. El backend se organiza en seis módulos (`auth`, `usuario`, `barbero`, `servicio`,
`horario` y `cita`) y la lógica de reservas no depende de HTTP, para que WhatsApp la use después
sin cambios.

Cada canal llama a la misma API y ninguno accede a la base de datos directamente; WhatsApp, en
línea punteada, se suma en la fase 2.

### Arquitectura: 2 canales, 6 módulos, 1 base de datos

| Capa | Tecnología | Estado |
|---|---|---|
| Backend | Java (21 LTS), Spring Boot, Spring Security con JWT, Spring Data JPA | Decidido |
| Base de datos | PostgreSQL con migraciones Flyway | Decidido |
| Frontend | React, TypeScript, Vite y Tailwind CSS | Propuesto |
| Contenedores | Docker y docker compose (API, PostgreSQL alpine y pgAdmin) | Decidido |
| Repositorio | Un monorepo en GitHub con carpetas `backend` y `frontend` | Propuesto |
| CI/CD | GitHub Actions con flujos separados por carpeta | Propuesto |
| Hosting y dominio | Frontend estático; backend con PostgreSQL gestionado o en servidor propio | Por definir |

---

## 7. Modelo de datos preliminar

El modelo tiene ocho tablas en PostgreSQL y es un borrador que el equipo ajustará al implementar.
La disponibilidad no se guarda: se calcula al consultar, a partir de horarios, bloqueos y citas.

| Tabla | Campos clave | Restricciones y notas |
|---|---|---|
| `usuario` | `id`, `nombre`, `telefono`, `password_hash`, `rol`, `activo`, `creado_en` | Teléfono único en formato `+57…`; rol CLIENTE, BARBERO o ADMIN. |
| `barbero` | `id`, `usuario_id`, `activo` | Referencia a un usuario con rol BARBERO o ADMIN. |
| `servicio` | `id`, `nombre`, `descripcion`, `duracion_min`, `precio`, `imagen`, `activo` | `imagen` guarda solo el nombre de un archivo estático del frontend. |
| `horario_atencion` | `id`, `barbero_id`, `dia_semana`, `hora_inicio`, `hora_fin`, `inicio_almuerzo`, `fin_almuerzo` | Sin registro para el domingo. |
| `bloqueo` | `id`, `barbero_id`, `inicio`, `fin`, `motivo` | Cubre una franja o una fecha completa. |
| `cita` | `id`, `cliente_id`, `barbero_id`, `servicio_id`, `inicio`, `fin`, `duracion_min`, `precio`, `estado`, `canal`, `creada_en` | `duracion_min` y `precio` copian los vigentes al reservar. |
| `refresh_token` | `id`, `usuario_id`, `token_hash`, `expira_en`, `revocado` | Permite cerrar sesión y revocar sesiones. |
| `configuracion` | `clave`, `valor` | Anticipación mínima, límite de cancelación, máximo de días, intervalo entre turnos y margen entre citas. |

La tabla `cita` lleva una restricción de exclusión de PostgreSQL sobre el rango entre `inicio` y
`fin` de cada barbero, que ignora las citas canceladas. Así la base rechaza cualquier
solapamiento aunque dos reservas lleguen al mismo tiempo.

---

## 8. Interfaces externas e integraciones

La fase 1 solo se conecta con un servicio de avisos; la integración con WhatsApp queda para la
fase 2 y por eso se deja preparada en el diseño.

**Preparación para la fase 2.** La API de reservas se expone como servicio interno reutilizable,
el teléfono se guarda en formato internacional y cada cita registra su canal. La cuenta de Meta y
la verificación del negocio pueden iniciarse en paralelo, porque su aprobación toma tiempo.

| Integración | Fase | Uso | Estado |
|---|---|---|---|
| Aviso al administrador (correo o bot de Telegram) | 1 | Notificar cada reserva y cancelación. | Canal por confirmar |
| Captcha (Cloudflare Turnstile u otro) | 1 | Frenar registros y reservas automáticas. | Propuesto |
| WhatsApp Cloud API oficial de Meta | 2 | Reservar desde WhatsApp y consultar la agenda del día. | Futuro |

---

## 9. Fuera de alcance y fases futuras

No se construye en la fase 1 nada de lo siguiente, aunque el diseño lo deja posible.

### Fase 2

- Módulo de WhatsApp: reservas por chat con botones y comandos del administrador para consultar
  la agenda del día.

### Otros elementos fuera de alcance

- Verificación del celular con un código, que sustituiría la recuperación de contraseña por el
  administrador.
- Recordatorios automáticos de cita al cliente.
- Agente con inteligencia artificial para entender mensajes libres, con herramientas acotadas y
  confirmación antes de reservar.
- Pagos o anticipos en línea.
- Reportes y estadísticas, opiniones de clientes y galería.
- Subida de imágenes desde el panel.
- Reprogramación de citas hecha por el propio cliente.

---

## 10. Criterios de aceptación

La fase 1 se da por entregada cuando se cumplen estos escenarios, que son también la base de las
pruebas.

| ID | Escenario | Criterio |
|---|---|---|
| CA-01 | Reserva exitosa | Un cliente registrado elige corte normal, barbero y horario libre; la cita queda confirmada con 40 minutos de duración y el administrador recibe el aviso. |
| CA-02 | Duración real | Una ondulación reservada a las 10:00 ocupa hasta las 13:00 y el siguiente horario ofrecido a ese barbero empieza a las 13:00. |
| CA-03 | Cierre | Un servicio de 180 minutos no se ofrece si no termina antes de las 19:00; su último inicio es a las 16:00. |
| CA-04 | Anticipación | No se ofrecen horarios con menos de 24 horas de anticipación ni a más de una semana. |
| CA-05 | Cancelación en plazo | El cliente cancela con más de 2 horas de margen y el horario vuelve a quedar disponible. |
| CA-06 | Cancelación tardía | Con menos de 2 horas de margen, el sistema rechaza la cancelación e indica contactar al local. |
| CA-07 | Reservas simultáneas | Si dos clientes eligen el mismo horario a la vez, uno queda confirmado, el otro ve horarios actualizados y la base no registra solapamiento. |
| CA-08 | Citas fijas | Reservar una ondulación en la mañana no cambia la hora de ninguna cita ya agendada en la tarde. |
| CA-09 | Bloqueos | Al bloquear una fecha, sus horarios dejan de ofrecerse; si ya hay citas ese día, el sistema pide confirmación y no las cancela. |
| CA-10 | Atención sin reserva | El administrador registra una cita a una hora exacta, como las 15:10, sin respetar la anticipación mínima. |
| CA-11 | Permisos | Un barbero solo ve sus citas, un cliente solo las suyas, y un cliente que llama a una operación de administrador recibe un error de permisos. |
| CA-12 | Seguridad de acceso | Las contraseñas están cifradas y varios intentos fallidos de inicio de sesión bloquean la cuenta de forma temporal. |
| CA-13 | Despliegue local | Con un solo comando de docker compose se levantan la API, PostgreSQL y pgAdmin, y la API responde como saludable. |
| CA-14 | Móvil | El flujo completo de reserva se usa en un celular sin desplazamiento horizontal. |

---

## 11. Pendientes por confirmar y riesgos

Hay diez puntos por cerrar con el cliente; los cuatro primeros cambian reglas de agenda y conviene
resolverlos antes de programar el módulo de citas.

| ID | Pendiente | Afecta a |
|---|---|---|
| P-01 | Hora y duración del almuerzo. | RN-02, RF-11 |
| P-02 | Turnos que reservan el tiempo exacto del servicio, o turnos de hora fija aunque se pierda tiempo. | RN-06, RN-07 |
| P-03 | Opción "cualquiera disponible" al elegir barbero. | RN-05, RF-15 |
| P-04 | Registro con nombre, celular y contraseña (antes el cliente respondió que no necesitaba cuenta) y restablecimiento por el administrador. | RN-15, RF-01 a RF-05 |
| P-05 | Lista final de servicios con nombres y precios. | RN-04, RF-07 |
| P-06 | Aviso al administrador por correo o por Telegram. | RF-28, RF-29 |
| P-07 | Si el barbero contratado puede cancelar y reprogramar sus propias citas. | RF-24 |
| P-08 | Página de ejemplo que le guste y qué no quiere en la suya. | Diseño |
| P-09 | Quién produce logo, colores, fotos y textos, y para cuándo. | Dependencias |
| P-10 | Compra y titularidad del dominio y del hosting. | Despliegue |

### Riesgos

| Riesgo | Mitigación |
|---|---|
| Reservas falsas con celulares sin verificar; una de 3 horas bloquea media jornada. | Captcha, límites por cuenta e IP, y panel para desactivar cuentas; verificación por código en la fase 2. |
| El cálculo de disponibilidad es la parte más compleja y el plazo es de un mes. | Asignarlo a la persona más fuerte del backend, probarlo desde el inicio y entregar primero los requisitos de prioridad Alta. |
| El cliente entrega tarde logo, fotos y textos. | Acordar una fecha de entrega y usar contenido provisional. |
| La aprobación de Meta para WhatsApp tarda. | Iniciar el trámite en paralelo a la fase 1. |
| Sin correo, cada contraseña olvidada pasa por el administrador. | Aceptar la carga en la fase 1 y sustituirla por verificación con código en la fase 2. |
