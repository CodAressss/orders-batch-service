package com.codares.logistics.iam.domain.model.queries;

import com.codares.logistics.iam.domain.model.valueobjects.Roles;

/**
 * Consulta para Obtener un Rol por su Nombre.
 * <p>
 * Record inmutable que transporta el nombre del rol para buscar un rol específico en el sistema.
 * El parámetro {@code name} debe ser uno de los valores predefinidos en la enumeración {@link Roles}.
 * </p>
 * <p>
 * Propósito: Facilitar la búsqueda eficiente de un rol específico por su nombre único,
 * útil para asignación a usuarios, validación de permisos y administración.
 * </p>
 *
 * @param name el nombre del rol a buscar. Debe ser un valor válido de la enumeración {@link Roles}
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see Roles
 * @see com.codares.logistics.iam.domain.services.RoleQueryService#handle(GetRoleByNameQuery)
 */
public record GetRoleByNameQuery(Roles name) {
}
