package com.oalbarber.api.modules.usuario.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oalbarber.api.modules.usuario.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    boolean existsByCelular(String celular);
}
