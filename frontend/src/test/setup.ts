import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// Sin `globals: true`, Testing Library no registra su limpieza automática:
// cada prueba debe desmontar lo que renderizó la anterior.
afterEach(() => {
  cleanup()
})
