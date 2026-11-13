package com.oalbarber.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.modules.barbero.service.BarberoService;
import com.oalbarber.api.modules.usuario.domain.RolUsuario;
import com.oalbarber.api.modules.usuario.domain.Usuario;
import com.oalbarber.api.modules.usuario.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Crea el administrador inicial al arrancar la API, con las credenciales del
 * .env ({@code ADMIN_CELULAR}, {@code ADMIN_NOMBRE}, {@code ADMIN_CONTRASENA}).
 *
 * <p>Reglas deliberadas: si falta alguna variable, no se crea nada. Si el
 * celular ya existe, NO se toca (ni rol ni contraseña): ese usuario es de
 * alguien y lo decide su dueño. El registro de administradores se hace aquí
 * una sola vez, nunca por un endpoint público.</p>
 */
@Slf4j
@Component
public class AdminInicializador implements ApplicationRunner {

    private final UsuarioRepository usuarios;
    private final BarberoService barberoService;
    private final PasswordEncoder codificador;
    private final String celular;
    private final String nombre;
    private final String contrasena;

    public AdminInicializador(UsuarioRepository usuarios,
                              BarberoService barberoService,
                              PasswordEncoder codificador,
                              @Value("${app.admin.celular:}") String celular,
                              @Value("${app.admin.nombre:OalBarber Admin}") String nombre,
                              @Value("${app.admin.contrasena:}") String contrasena) {
        this.usuarios = usuarios;
        this.barberoService = barberoService;
        this.codificador = codificador;
        this.celular = celular;
        this.nombre = nombre;
        this.contrasena = contrasena;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (celular.isBlank() || contrasena.isBlank()) {
            log.warn("ADMIN_CELULAR o ADMIN_CONTRASENA sin definir: no se crea el "
                    + "administrador inicial.");
            return;
        }

        Usuario administrador = usuarios.findByCelular(celular.trim())
                .orElseGet(() -> crearAdministrador());

        if (administrador.getRol() != RolUsuario.ADMINISTRADOR) {
            log.warn("El celular del administrador inicial ya pertenece a un usuario con rol {}: "
                    + "no se modifica la cuenta ni se crea un perfil de barbero.",
                    administrador.getRol());
            return;
        }

        boolean perfilAsegurado = barberoService.asegurarPerfilAdministrador(administrador.getId());
        if (perfilAsegurado) {
            log.info("Perfil de barbero del administrador inicial verificado.");
        }
    }

    private Usuario crearAdministrador() {
        Usuario administrador = usuarios.save(Usuario.builder()
                .nombre(nombre.isBlank() ? "OalBarber Admin" : nombre.trim())
                .celular(celular.trim())
                .contrasenaHash(codificador.encode(contrasena))
                .rol(RolUsuario.ADMINISTRADOR)
                .activo(true)
                .build());
        log.info("Administrador inicial creado para el celular registrado en ADMIN_CELULAR.");
        return administrador;
    }
}
