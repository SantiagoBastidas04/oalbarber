package com.oalbarber.api.modules.barbero.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class BarberoRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void aceptaDatosOpcionalesVaciosYFotoConNombreEstatico() {
        BarberoRequest solicitud = new BarberoRequest("Nombre", "+573000000001",
                "ClaveSegura123", null, null, "barbero-01.webp");

        assertTrue(validator.validate(solicitud).isEmpty());
    }

    @Test
    void rechazaRutasTraversalYExtensionesNoPermitidasEnFoto() {
        assertCampoInvalido("../barbero.webp", "foto");
        assertCampoInvalido("imagenes/barbero.webp", "foto");
        assertCampoInvalido("barbero.svg", "foto");
        assertCampoInvalido("Barbero.webp", "foto");
    }

    @Test
    void limitaLongitudDelNombrePublicoYDescripcion() {
        BarberoRequest solicitud = new BarberoRequest("Nombre", "+573000000001",
                "ClaveSegura123", "N".repeat(101), "D".repeat(501), null);

        Set<String> camposInvalidos = validator.validate(solicitud).stream()
                .map(error -> error.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("nombrePublico", "descripcion"), camposInvalidos);
    }

    private static void assertCampoInvalido(String foto, String campo) {
        BarberoRequest solicitud = new BarberoRequest("Nombre", "+573000000001",
                "ClaveSegura123", null, null, foto);

        assertTrue(validator.validate(solicitud).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals(campo)));
    }
}
