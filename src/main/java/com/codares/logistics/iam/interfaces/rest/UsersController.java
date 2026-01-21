package com.codares.logistics.iam.interfaces.rest;

import com.codares.logistics.iam.domain.model.queries.GetAllUsersQuery;
import com.codares.logistics.iam.domain.model.queries.GetUserByIdQuery;
import com.codares.logistics.iam.domain.services.UserQueryService;
import com.codares.logistics.iam.interfaces.rest.resources.UserResource;
import com.codares.logistics.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para Consultas de Usuarios (Seguro).
 * <p>
 * Este controlador expone endpoints para consultar información de usuarios registrados.
 * Todos los endpoints requieren autenticación con token JWT y autorización de administrador.
 * </p>
 * <p>
 * <strong>Endpoints Protegidos (Requieren @PreAuthorize("hasRole('ADMIN')"):</strong>
 * </p>
 * <ul>
 *   <li><code>GET /api/v1/users</code> - Obtener todos los usuarios del sistema</li>
 *   <li><code>GET /api/v1/users/{userId}</code> - Obtener usuario específico por ID</li>
 * </ul>
 * <p>
 * <strong>Seguridad:</strong>
 * </p>
 * <ul>
 *   <li>Requiere token JWT válido en header Authorization: Bearer &lt;token&gt;</li>
 *   <li>Usuario debe tener rol ADMIN asignado</li>
 *   <li>Si token falta o no es válido: HTTP 401 Unauthorized</li>
 *   <li>Si usuario no es ADMIN: HTTP 403 Forbidden</li>
 *   <li>Propósito: Consulta administrativa de usuarios del sistema</li>
 * </ul>
 * <p>
 * <strong>Patrón: Query Service Pattern (CQRS)</strong>
 * </p>
 * <p>
 * Los endpoints utilizan {@link UserQueryService} que implementa operaciones de lectura-sola
 * (@Transactional(readOnly = true)) para optimizar el rendimiento sin necesidad de bloqueos
 * de escritura. Cada endpoint construye una Query específica y delega su ejecución al servicio.
 * </p>
 * <p>
 * <strong>Nota de Diseño:</strong> Los usuarios consultados incluyen sus roles asociados
 * mediante carga eager (LEFT JOIN FETCH) para evitar problemas de LazyInitializationException.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.model.queries.GetAllUsersQuery
 * @see com.codares.logistics.iam.domain.model.queries.GetUserByIdQuery
 * @see com.codares.logistics.iam.domain.services.UserQueryService
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Available User Endpoints")
public class UsersController {
    private final UserQueryService userQueryService;

    /**
     * Constructor que inyecta el servicio de consultas de usuarios.
     *
     * @param userQueryService servicio que orquesta la lectura de usuarios desde BD
     */
    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    /**
     * Obtiene todos los usuarios registrados en el sistema.
     * <p>
     * Endpoint administrativo que requiere autenticación y rol ADMIN.
     * Utiliza el patrón CQRS para consultas de lectura optimizadas.
     * </p>
     * <p>
     * <strong>Requisitos de Acceso:</strong>
     * </p>
     * <ul>
     *   <li>Token JWT válido en Authorization header</li>
     *   <li>Usuario debe tener rol ADMIN</li>
     *   <li>Token no debe estar expirado</li>
     * </ul>
     * <p>
     * <strong>Flujo de Procesamiento:</strong>
     * </p>
     * <ol>
     *   <li>Spring Security valida el JWT</li>
     *   <li>@PreAuthorize verifica que el usuario tiene rol ADMIN</li>
     *   <li>Se construye GetAllUsersQuery (objeto DTO de entrada)</li>
     *   <li>UserQueryService ejecuta consulta con eager loading de roles</li>
     *   <li>UserResourceFromEntityAssembler transforma Entidad → Resource</li>
     *   <li>Se retorna lista de usuarios en formato JSON (HTTP 200)</li>
     * </ol>
     * <p>
     * <strong>Nota Técnica:</strong> Los usuarios se cargan con sus roles asociados
     * mediante LEFT JOIN FETCH para evitar N+1 queries y LazyInitializationException.
     * </p>
     *
     * @return ResponseEntity&lt;List&lt;UserResource&gt;&gt; lista de todos los usuarios (HTTP 200)
     *         o HTTP 401 si no está autenticado o no es administrador
     * @see UserResource
     * @see GetAllUsersQuery
     * @see UserResourceFromEntityAssembler
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get all the users available in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);
        var userResources = users.stream().map(UserResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(userResources);
    }

    /**
     * Obtiene la información detallada de un usuario específico por su ID.
     * <p>
     * Endpoint administrativo que requiere autenticación y rol ADMIN.
     * Busca al usuario por UUID en la base de datos e incluye sus roles asociados.
     * </p>
     * <p>
     * <strong>Requisitos de Acceso:</strong>
     * </p>
     * <ul>
     *   <li>Token JWT válido en Authorization header</li>
     *   <li>Usuario debe tener rol ADMIN</li>
     *   <li>Token no debe estar expirado</li>
     * </ul>
     * <p>
     * <strong>Flujo de Procesamiento:</strong>
     * </p>
     * <ol>
     *   <li>Spring Security valida el JWT</li>
     *   <li>@PreAuthorize verifica que el usuario tiene rol ADMIN</li>
     *   <li>Se construye GetUserByIdQuery con el UUID proporcionado</li>
     *   <li>UserQueryService busca el usuario en BD con eager loading de roles</li>
     *   <li>Si usuario existe: transforma a Resource y retorna HTTP 200</li>
     *   <li>Si usuario no existe: retorna HTTP 404 Not Found</li>
     * </ol>
     * <p>
     * <strong>Validaciones:</strong> El UUID debe ser válido y corresponder a un usuario
     * registrado en el sistema. El rol ADMIN es necesario para acceder a datos de cualquier usuario.
     * </p>
     *
     * @param userId identificador único (UUID) del usuario a consultar
     * @return ResponseEntity&lt;UserResource&gt; datos del usuario (HTTP 200)
     *         o HTTP 404 si el usuario no existe
     *         o HTTP 401 si no está autenticado/autorizado
     * @see UserResource
     * @see GetUserByIdQuery
     * @see UserResourceFromEntityAssembler
     */
    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by id", description = "Get the user with the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<UserResource> getUserById(@PathVariable UUID userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }
}
