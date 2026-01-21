package com.codares.logistics.iam.interfaces.rest.resources;

/**
 * DTO que representa un rol del sistema en respuestas REST.
 * <p>
 * Contiene la información mínima de un rol que es necesaria exponer en la API.
 * Los roles son utilizados en Spring Security para implementar control de acceso
 * basado en autoridades (role-based access control).
 * </p>
 * <p>
 * <strong>Estructura:</strong>
 * </p>
 * <ul>
 *   <li><code>id</code>: Identificador único del rol (clave primaria)</li>
 *   <li><code>name</code>: Nombre del rol (ej: "ADMIN", "USER", "MODERATOR")</li>
 * </ul>
 * <p>
 * <strong>Roles del Sistema:</strong>
 * </p>
 * <ul>
 *   <li><code>ADMIN</code>: Acceso total a administración de usuarios y roles</li>
 *   <li><code>USER</code>: Rol por defecto para usuarios regulares</li>
 * </ul>
 * <p>
 * <strong>Uso:</strong> Retornado por GET /api/v1/roles (solo administrador)
 * </p>
 * <p>
 * Patrón: Record inmutable para transferencia de datos entre API REST y cliente.
 * </p>
 *
 * @param id identificador único del rol
 * @param name nombre del rol (ej: ADMIN, USER)
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.interfaces.rest.RolesController#getAllRoles()
 * @see com.codares.logistics.iam.domain.model.entities.Role
 */
public record RoleResource(Long id, String name) {
}
