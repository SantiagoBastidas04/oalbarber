package com.oalbarber.api.modules.servicio.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Comprueba que la duración esté entre las que ofrece el negocio. */
public class DuracionPermitidaValidator
        implements ConstraintValidator<DuracionPermitida, Integer> {

    private static final Set<Integer> PERMITIDAS = Set.of(40, 90, 180);

    @Override
    public boolean isValid(Integer valor, ConstraintValidatorContext contexto) {
        return valor == null || PERMITIDAS.contains(valor);
    }
}
