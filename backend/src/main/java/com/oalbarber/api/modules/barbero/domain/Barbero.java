package com.oalbarber.api.modules.barbero.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Perfil profesional enlazado a una cuenta de usuario mediante su UUID. */
@Entity
@Table(name = "barbero", uniqueConstraints = {
        @UniqueConstraint(name = "uk_barbero_usuario_id", columnNames = "usuario_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** UUID del usuario asociado; se guarda como valor para no cruzar entidades entre módulos. */
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "nombre_publico", length = 100)
    private String nombrePublico;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(length = 255)
    private String foto;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (objeto == null || getClass() != objeto.getClass()) {
            return false;
        }
        Barbero otro = (Barbero) objeto;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id == null ? getClass().hashCode() : id.hashCode();
    }
}
