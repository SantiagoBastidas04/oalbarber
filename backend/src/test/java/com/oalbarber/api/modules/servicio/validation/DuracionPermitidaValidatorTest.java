package com.oalbarber.api.modules.servicio.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * La regla de la duración (RF-08) se prueba sin Spring y sin base de datos:
 * el validador es una clase normal que responde sí o no.
 */
class DuracionPermitidaValidatorTest {

    private final DuracionPermitidaValidator validador = new DuracionPermitidaValidator();

    @ParameterizedTest(name = "acepta {0} minutos")
    @ValueSource(ints = {40, 90, 180})
    @DisplayName("acepta las duraciones que ofrece el negocio")
    void aceptaLasDuracionesDelNegocio(int duracion) {
        assertThat(validador.isValid(duracion, null)).isTrue();
    }

    @ParameterizedTest(name = "rechaza {0} minutos")
    @ValueSource(ints = {-40, 0, 30, 41, 50, 89, 91, 120, 179, 181, 240})
    @DisplayName("rechaza cualquier duración que no esté en la lista")
    void rechazaDuracionesFueraDeLaLista(int duracion) {
        assertThat(validador.isValid(duracion, null)).isFalse();
    }

    @Test
    @DisplayName("el nulo no se reporta aquí: lo cubre @NotNull")
    void elNuloLoSeparaNotNull() {
        assertThat(validador.isValid(null, null)).isTrue();
    }
}
