package com.oalbarber.api.modules.barbero.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.common.exception.RecursoDuplicadoException;
import com.oalbarber.api.common.exception.ReglaNegocioException;
import com.oalbarber.api.modules.barbero.domain.Barbero;
import com.oalbarber.api.modules.barbero.domain.BarberoServicio;
import com.oalbarber.api.modules.barbero.dto.BarberoAdminResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoPerfilRequest;
import com.oalbarber.api.modules.barbero.dto.BarberoPublicoResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoRequest;
import com.oalbarber.api.modules.barbero.mapper.BarberoMapper;
import com.oalbarber.api.modules.barbero.repository.BarberoRepository;
import com.oalbarber.api.modules.barbero.repository.BarberoServicioRepository;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.service.ServicioService;
import com.oalbarber.api.modules.usuario.domain.RolUsuario;
import com.oalbarber.api.modules.usuario.dto.UsuarioRequest;
import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;
import com.oalbarber.api.modules.usuario.service.UsuarioService;

import lombok.RequiredArgsConstructor;

/** Reglas de aplicación para perfiles de barbero (RF-10, RN-01 y RN-12). */
@Service
@RequiredArgsConstructor
public class BarberoService {

    private final BarberoRepository barberoRepository;
    private final BarberoServicioRepository barberoServicioRepository;
    private final ServicioService servicioService;
    private final UsuarioService usuarioService;
    private final BarberoMapper barberoMapper;

    /** Lista perfiles activos sin exponer nombre de cuenta ni celular. */
    @Transactional(readOnly = true)
    public List<BarberoPublicoResponse> listarActivos() {
        return barberoRepository.findByActivoTrue().stream()
                .map(barberoMapper::aPublico)
                .toList();
    }

    /** Lista perfiles activos e inactivos para el panel administrativo. */
    @Transactional(readOnly = true)
    public List<BarberoAdminResponse> listarTodos() {
        return barberoRepository.findAll().stream()
                .map(this::aAdmin)
                .toList();
    }

    /**
     * Crea la cuenta BARBERO y su perfil dentro de una sola transacción.
     * La creación de cuenta reutiliza la unicidad del celular y BCrypt del módulo usuario.
     *
     * @param solicitud datos de la cuenta y el perfil
     * @return perfil y cuenta para el panel
     */
    @Transactional
    public BarberoAdminResponse crear(BarberoRequest solicitud) {
        UsuarioRequest datosUsuario = new UsuarioRequest(
                solicitud.nombre(), solicitud.celular(), solicitud.contrasena());
        UsuarioResponse usuario = usuarioService.crearUsuarioConRol(
                datosUsuario, RolUsuario.BARBERO);

        Barbero barbero = barberoMapper.aEntidad(solicitud, usuario.getId());
        Barbero guardado = barberoRepository.save(barbero);
        return aAdmin(guardado, usuario);
    }

    /**
     * Registra al administrador autenticado como barbero reutilizando su cuenta existente.
     * No crea ni modifica datos de usuario; un perfil ya existente debe reactivarse.
     *
     * @param usuarioId UUID del administrador autenticado
     * @param datos datos opcionales de presentación del perfil
     * @return perfil asociado a la cuenta administrativa existente
     * @throws RecursoNoEncontradoException si no existe la cuenta indicada
     * @throws ReglaNegocioException si la cuenta está inactiva o no tiene rol ADMINISTRADOR
     * @throws RecursoDuplicadoException si ya existe un perfil para la cuenta
     */
    @Transactional
    public BarberoResponse registrarAdministradorComoBarbero(
            UUID usuarioId, BarberoPerfilRequest datos) {
        UsuarioResponse usuario = usuarioService.obtenerPorId(usuarioId);
        if (!usuario.isActivo() || usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new ReglaNegocioException("BARBERO_ROL_INVALIDO",
                    "Solo una cuenta activa con rol ADMINISTRADOR puede agregarse como barbero.");
        }

        barberoRepository.findByUsuarioId(usuarioId).ifPresent(perfil -> {
            String mensaje = perfil.isActivo()
                    ? "Esta cuenta ya tiene un perfil de barbero."
                    : "Esta cuenta ya tiene un perfil de barbero inactivo; debe reactivarse.";
            throw new RecursoDuplicadoException("BARBERO_DUPLICADO", mensaje);
        });

        Barbero perfil = barberoMapper.aEntidad(datos, usuarioId);
        Barbero guardado = barberoRepository.save(perfil);
        return barberoMapper.aResponse(guardado, usuario);
    }

    /** Cambia el estado del perfil sin desactivar la cuenta ni cancelar citas. */
    @Transactional
    public BarberoAdminResponse cambiarEstado(UUID id, boolean activo) {
        Barbero barbero = barberoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el perfil de barbero indicado."));
        barbero.cambiarEstado(activo);
        Barbero guardado = barberoRepository.save(barbero);
        return aAdmin(guardado);
    }

    /** Obtiene el perfil administrativo por identificador. */
    @Transactional(readOnly = true)
    public BarberoAdminResponse obtener(UUID id) {
        Barbero barbero = barberoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el perfil de barbero indicado."));
        return aAdmin(barbero);
    }

    /** Devuelve solo los servicios activos que tiene asignados un perfil existente. */
    @Transactional(readOnly = true)
    public List<ServicioResponse> listarServiciosDe(UUID barberoId) {
        exigirBarberoExistente(barberoId);
        return listarServiciosActivosAsignados(barberoId);
    }

    /** Devuelve servicios asignados solo si el perfil existe y está activo. */
    @Transactional(readOnly = true)
    public List<ServicioResponse> listarServiciosPublicosDe(UUID barberoId) {
        Barbero barbero = barberoRepository.findById(barberoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el perfil de barbero indicado."));
        if (!barbero.isActivo()) {
            throw new RecursoNoEncontradoException("No existe el perfil de barbero indicado.");
        }
        return listarServiciosActivosAsignados(barberoId);
    }

    private List<ServicioResponse> listarServiciosActivosAsignados(UUID barberoId) {
        Set<UUID> asignados = barberoServicioRepository.findById_BarberoId(barberoId).stream()
                .map(asignacion -> asignacion.getId().getServicioId())
                .collect(Collectors.toSet());

        return servicioService.listar(false).stream()
                .filter(servicio -> asignados.contains(servicio.getId()))
                .toList();
    }

    /**
     * Reemplaza las asignaciones en una sola transacción; solo modifica los pares que cambiaron.
     *
     * <p>Pendiente: aviso por citas futuras al quitar un servicio (criterio 4 de H-13); requiere
     * el módulo cita.</p>
     *
     * @param barberoId identificador del perfil
     * @param servicioIds identificadores activos que quedarán asignados
     * @return servicios activos asignados después del reemplazo
     */
    @Transactional
    public List<ServicioResponse> reemplazarServicios(UUID barberoId, Collection<UUID> servicioIds) {
        exigirBarberoExistente(barberoId);

        if (servicioIds == null) {
            throw servicioNoValido("La lista de servicios es obligatoria.");
        }
        Set<UUID> deseados = new HashSet<>(servicioIds);
        if (deseados.contains(null) || deseados.size() != servicioIds.size()) {
            throw servicioNoValido("La lista contiene identificadores nulos o duplicados.");
        }

        List<ServicioResponse> serviciosActivos = servicioService.listar(false);
        Set<UUID> idsActivos = serviciosActivos.stream()
                .map(ServicioResponse::getId)
                .collect(Collectors.toSet());
        if (!idsActivos.containsAll(deseados)) {
            throw servicioNoValido("Todos los servicios deben existir y estar activos.");
        }

        List<BarberoServicio> actuales = barberoServicioRepository.findById_BarberoId(barberoId);
        Set<UUID> idsActuales = actuales.stream()
                .map(asignacion -> asignacion.getId().getServicioId())
                .collect(Collectors.toSet());

        actuales.stream()
                .map(asignacion -> asignacion.getId().getServicioId())
                .filter(id -> !deseados.contains(id))
                .forEach(id -> barberoServicioRepository
                        .deleteById_BarberoIdAndId_ServicioId(barberoId, id));

        List<BarberoServicio> nuevos = deseados.stream()
                .filter(id -> !idsActuales.contains(id))
                .map(id -> BarberoServicio.crear(barberoId, id))
                .toList();
        if (!nuevos.isEmpty()) {
            barberoServicioRepository.saveAll(nuevos);
        }

        return serviciosActivos.stream()
                .filter(servicio -> deseados.contains(servicio.getId()))
                .toList();
    }

    /** Lista perfiles activos que ofrecen un servicio activo del catálogo. */
    @Transactional(readOnly = true)
    public List<Barbero> listarActivosQueOfrecenServicio(UUID servicioId) {
        boolean servicioActivo = servicioService.listar(false).stream()
                .anyMatch(servicio -> servicio.getId().equals(servicioId));
        if (!servicioActivo) {
            return List.of();
        }

        List<UUID> idsBarberos = barberoServicioRepository.findById_ServicioId(servicioId).stream()
                .map(asignacion -> asignacion.getId().getBarberoId())
                .distinct()
                .toList();
        return barberoRepository.findAllById(idsBarberos).stream()
                .filter(Barbero::isActivo)
                .toList();
    }

    /** Comprueba si hay asignación para un servicio que sigue activo en el catálogo. */
    @Transactional(readOnly = true)
    public boolean ofreceServicio(UUID barberoId, UUID servicioId) {
        boolean servicioActivo = servicioService.listar(false).stream()
                .anyMatch(servicio -> servicio.getId().equals(servicioId));
        return servicioActivo && barberoServicioRepository
                .existsById_BarberoIdAndId_ServicioId(barberoId, servicioId);
    }

    private BarberoAdminResponse aAdmin(Barbero barbero) {
        UsuarioResponse usuario = usuarioService.obtenerPorId(barbero.getUsuarioId());
        return aAdmin(barbero, usuario);
    }

    private BarberoAdminResponse aAdmin(Barbero barbero, UsuarioResponse usuario) {
        BarberoAdminResponse datos = barberoMapper.aAdmin(barbero, usuario);
        List<UUID> servicioIds = barberoServicioRepository.findById_BarberoId(barbero.getId()).stream()
                .map(asignacion -> asignacion.getId().getServicioId())
                .toList();
        return new BarberoAdminResponse(datos.id(), datos.usuarioId(), datos.nombre(), datos.celular(),
                datos.nombrePublico(), datos.descripcion(), datos.foto(), datos.activo(), servicioIds);
    }

    private void exigirBarberoExistente(UUID barberoId) {
        if (!barberoRepository.existsById(barberoId)) {
            throw new RecursoNoEncontradoException("No existe el perfil de barbero indicado.");
        }
    }

    private ReglaNegocioException servicioNoValido(String mensaje) {
        return new ReglaNegocioException("SERVICIO_NO_VALIDO", mensaje);
    }
}
