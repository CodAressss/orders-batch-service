package com.codares.logistics.iam.domain.model.queries;

import java.util.UUID;

/**
 * Consulta para Obtener un Usuario por su Identificador.
 * <p>
 * Record inmutable que transporta el UUID (identificador único) para buscar un usuario específico.
 * El parámetro {@code userId} es el identificador único generado automáticamente al crear el usuario.
 * </p>
 * <p>
 * Propósito: Facilitar la búsqueda directa y eficiente de un usuario específico mediante su UUID,
 * útil para recuperación de perfil, auditoría y operaciones administrativas.
 * </p>
 *
 * @param userId el identificador único (UUID) del usuario a buscar
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.services.UserQueryService#handle(GetUserByIdQuery)
 * @see com.codares.logistics.iam.domain.model.aggregates.User
 */
public record GetUserByIdQuery(UUID userId) {
}
