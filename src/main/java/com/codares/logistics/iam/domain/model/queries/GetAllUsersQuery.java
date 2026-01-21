package com.codares.logistics.iam.domain.model.queries;

/**
 * Consulta para Obtener Todos los Usuarios del Sistema.
 * <p>
 * Record inmutable que dispara la obtención de todos los usuarios registrados en el sistema.
 * Es una consulta vacía que actúa como señal para recuperar la lista completa de usuarios.
 * </p>
 * <p>
 * Propósito: Facilitar la recuperación de todos los usuarios para operaciones
 * de administración, auditoría y gestión de permisos.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.services.UserQueryService#handle(GetAllUsersQuery)
 * @see com.codares.logistics.iam.domain.model.aggregates.User
 */
public record GetAllUsersQuery() {
}
