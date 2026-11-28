package com.oalbarber.api.modules.horario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Parámetro global configurable de las reglas de agenda RN-18. */
@Entity
@Table(name = "configuracion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Configuracion {

    @Id
    @Column(length = 100)
    private String clave;

    @Column(nullable = false, length = 255)
    private String valor;
}
