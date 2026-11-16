package com.oalbarber.api.modules.servicio.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * La duración de un servicio solo puede ser 40, 90 o 180 minutos.
 * El {@code null} lo controla {@code @NotNull}: aquí no se reporta.
 */
@Documented
@Constraint(validatedBy = DuracionPermitidaValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DuracionPermitida {

    String message() default "La duración debe ser 40, 90 o 180 minutos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}