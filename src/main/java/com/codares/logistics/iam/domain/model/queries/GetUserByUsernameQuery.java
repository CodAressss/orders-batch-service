package com.codares.logistics.iam.domain.model.queries;

/**
 * Consulta para Obtener un Usuario por su Nombre de Usuario.
 * <p>
 * Record inmutable que transporta el nombre de usuario para buscar un usuario específico.
 * El parámetro {@code username} debe ser único en el sistema (validado al registro).
 * </p>
 * <p>
 * Propósito: Facilitar la búsqueda de un usuario por su nombre de usuario único,
 * esencial para autenticación y búsqueda de perfiles en la interfaz de usuario.
 * </p>
 *
 * @param username el nombre de usuario único para la búsqueda
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.services.UserQueryService#handle(GetUserByUsernameQuery)
 * @see com.codares.logistics.iam.domain.model.aggregates.User
 */
public record GetUserByUsernameQuery(String username) {
}
