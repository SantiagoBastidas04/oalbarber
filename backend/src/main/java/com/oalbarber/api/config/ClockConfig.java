package com.oalbarber.api.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provee el reloj de negocio en la zona horaria de OalBarber. */
@Configuration
public class ClockConfig {

    /** Crea el reloj del sistema en America/Bogota para permitir sustituirlo en pruebas. */
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
