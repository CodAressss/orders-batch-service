package com.codares.logistics.iam.interfaces.rest;

import com.codares.logistics.iam.domain.model.queries.GetAllRolesQuery;
import com.codares.logistics.iam.domain.services.RoleQueryService;
import com.codares.logistics.iam.interfaces.rest.resources.RoleResource;
import com.codares.logistics.iam.interfaces.rest.transform.RoleResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para Consultas de Roles (Seguro).
 * <p>
 * Este controlador expone endpoints para consultar los roles disponibles en el sistema.
 * Todos los endpoints requieren autenticación con token JWT y autorización de administrador.
 * </p>
 * <p>
 * <strong>Endpoints Protegidos (Requieren @PreAuthorize("hasRole('ADMIN')"):</strong>
 * </p>
 * <ul>
 *   <li><code>GET /api/v1/roles</code> - Obtener todos los roles del sistema</li>
 * </ul>
 * <p>
 * <strong>Seguridad:</strong>
 * </p>
 * <ul>
 *   <li>Requiere token JWT válido en header Authorization: Bearer &lt;token&gt;</li>
 *   <li>Usuario debe tener rol ADMIN asignado</li>
 *   <li>Si token falta o no es válido: HTTP 401 Unauthorized</li>
 *   <li>Si usuario no es ADMIN: HTTP 403 Forbidden (manejado por Spring Security)</li>
 * </ul>
 * <p>
 * <strong>Patrón: Query Service Pattern (CQRS)</strong>
 * </p>
 * <p>
 * Los endpoints de consulta utilizan {@link RoleQueryService} que implementa
 * operaciones de lectura-sola (@Transactional(readOnly = true)) para optimizar
 * el rendimiento y minimizar bloqueos de BD.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.model.queries.GetAllRolesQuery
 * @see com.codares.logistics.iam.domain.services.RoleQueryService
 */
@RestController
@RequestMapping(value = "/ap/v1/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Roles", description = "Available Role Endpoints")
public class RolesController {
    private final RoleQueryService roleQueryService;

    /**
     * Constructor que inyecta el servicio de consultas de roles.
     *
     * @param roleQueryService servicio que orquesta la lectura de roles desde BD
     */
    public RolesController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    /**
     * Obtiene todos los roles disponibles en el sistema.
     * <p>
     * Endpoint seguro que requiere autorización de administrador. Utiliza el
     * patrón CQRS para consultas, delegando al QueryService que ejecuta
     * una transacción de lectura-sola optimizada.
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
     *   <li>Se construye GetAllRolesQuery (objeto DTO de entrada)</li>
     *   <li>RoleQueryService ejecuta consulta en BD</li>
     *   <li>RoleResourceFromEntityAssembler transforma Entidad → Resource</li>
     *   <li>Se retorna lista de roles en formato JSON</li>
     * </ol>
     *
     * @return ResponseEntity&lt;List&lt;RoleResource&gt;&gt; lista de todos los roles (HTTP 200)
     *         o HTTP 401 si el usuario no está autenticado o no es administrador
     * @see RoleResource
     * @see GetAllRolesQuery
     * @see RoleResourceFromEntityAssembler
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all roles", description = "Get all the roles available in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<List<RoleResource>> getAllRoles() {
        var getAllRolesQuery = new GetAllRolesQuery();
        var roles = roleQueryService.handle(getAllRolesQuery);
        var roleResources = roles.stream().map(RoleResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(roleResources);
    }
}
