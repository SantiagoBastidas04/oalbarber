# Pruebas (`test`)

## Qué debe ir aquí

- `setup.ts`: archivo de preparación de Vitest; importa `@testing-library/jest-dom/vitest`.
- Utilidades de prueba compartidas cuando hagan falta (por ejemplo, un proveedor de sesión).

## Qué NO debe ir aquí

- Pruebas de páginas concretas: viven junto al archivo que prueban
  (`utils/celular.test.ts`, `routes/RutaProtegida.test.tsx`).
- Datos reales de clientes, tokens o contraseñas.
- Pruebas que dependan del orden de ejecución o de la hora del sistema sin controlarla.
- `sleep` para esperar: se usan `findBy*` y `waitFor`.

## Convenciones

- Un archivo `*.test.ts` o `*.test.tsx` por unidad bajo prueba.
- Nombres de prueba en español y en presente: `normaliza un celular con espacios`.
- Se prueba lo que el usuario ve (`getByRole`, `getByText`), no la implementación interna.
- `npm run test` ejecuta todo una vez; `npm run test:watch` queda para desarrollo.
- Las reglas de negocio no se prueban aquí: se prueban en el backend, que es su dueño.
