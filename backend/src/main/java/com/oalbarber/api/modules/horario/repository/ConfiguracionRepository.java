package com.oalbarber.api.modules.horario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oalbarber.api.modules.horario.domain.Configuracion;

/** Lectura de parametros RN-18 guardados en la base de datos. */
public interface ConfiguracionRepository extends JpaRepository<Configuracion, String> {
}
