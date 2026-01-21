package com.codares.logistics.iam.domain.model.queries;

/**
 * Consulta para Obtener Todos los Roles del Sistema.
 * <p>
 * Record inmutable que dispara la obtención de todos los roles registrados en el sistema.
 * Es una consulta vacía que actúa como señal para recuperar la lista completa de roles.
 * </p>
 * <p>
 * Propósito: Facilitar la recuperación de todos los roles disponibles para operaciones
 * de visualización, administración y asignación a usuarios.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.services.RoleQueryService#handle(GetAllRolesQuery)
 * @see com.codares.logistics.iam.domain.model.entities.Role
 */
public record GetAllRolesQuery() {
}
