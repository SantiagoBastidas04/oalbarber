import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { PAISES } from '../../utils/celularPais'
import { SelectorPais } from './SelectorPais'

function renderizar(valor = '57') {
  const onCambio = vi.fn()
  render(<SelectorPais paises={PAISES} valor={valor} onCambio={onCambio} />)
  return { onCambio, boton: screen.getByRole('button', { name: /país del celular/i }) }
}

describe('SelectorPais', () => {
  it('muestra el país seleccionado con su prefijo', () => {
    const { boton } = renderizar()

    expect(boton).toHaveAccessibleName(/Colombia, prefijo más 57/i)
    expect(boton).toHaveTextContent('+57')
  })

  it('abre la lista y elige un país con el mouse', async () => {
    const usuario = userEvent.setup()
    const { onCambio, boton } = renderizar()

    await usuario.click(boton)
    expect(screen.getByRole('listbox', { name: /país del celular/i })).toBeInTheDocument()

    await usuario.click(screen.getByRole('option', { name: /Ecuador/i }))

    expect(onCambio).toHaveBeenCalledWith('593')
    expect(screen.queryByRole('listbox')).not.toBeInTheDocument()
  })

  it('navega con las flechas y confirma con Enter', async () => {
    const usuario = userEvent.setup()
    const { onCambio, boton } = renderizar()

    await usuario.click(boton)
    await usuario.keyboard('{ArrowDown}')
    await usuario.keyboard('{Enter}')

    expect(onCambio).toHaveBeenCalledWith('54')
  })

  it('cierra con Escape sin cambiar el valor', async () => {
    const usuario = userEvent.setup()
    const { onCambio, boton } = renderizar()

    await usuario.click(boton)
    await usuario.keyboard('{Escape}')

    expect(screen.queryByRole('listbox')).not.toBeInTheDocument()
    expect(onCambio).not.toHaveBeenCalled()
    expect(boton).toHaveFocus()
  })

  it('se abre con el teclado desde el botón', async () => {
    const usuario = userEvent.setup()
    const { boton } = renderizar()

    boton.focus()
    await usuario.keyboard('{ArrowDown}')

    expect(screen.getByRole('listbox')).toBeInTheDocument()
  })
})
