import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ImagenServicio } from './ImagenServicio'

describe('ImagenServicio', () => {
  it('arma la ruta estática de la foto del servicio', () => {
    render(<ImagenServicio archivo="corte-normal.jpg" nombre="Corte normal" />)

    expect(screen.getByRole('img', { name: 'Corte normal' })).toHaveAttribute(
      'src',
      '/servicios/corte-normal.jpg',
    )
  })

  it('muestra un marcador cuando la foto falla y no rompe la tarjeta', () => {
    render(<ImagenServicio archivo="corte-normal.jpg" nombre="Corte normal" />)

    fireEvent.error(screen.getByRole('img', { name: 'Corte normal' }))

    expect(
      screen.getByRole('img', { name: 'Imagen no disponible para Corte normal' }),
    ).toBeInTheDocument()
    expect(screen.queryByRole('img', { name: 'Corte normal' })).not.toBeInTheDocument()
  })

  it('muestra un marcador cuando el servicio no tiene archivo', () => {
    render(<ImagenServicio archivo={null} nombre="Mechas" />)

    expect(
      screen.getByRole('img', { name: 'Imagen no disponible para Mechas' }),
    ).toBeInTheDocument()
  })
})
