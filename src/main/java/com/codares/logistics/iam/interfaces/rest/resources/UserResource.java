package com.codares.logistics.iam.interfaces.rest.resources;

import java.util.List;
import java.util.UUID;

/**
 * DTO que representa la información de un usuario en respuestas REST.
 * <p>
 * Contiene los datos públicamente expuestos de un usuario del sistema.
 * Este DTO es generado por {@link com.codares.logistics.iam.interfaces.rest.transform.UserResourceFromEntityAssembler}
 * cuando se necesita transformar la entidad {@link com.codares.logistics.iam.domain.model.aggregates.User}
 * a un formato serializable JSON para respuestas de API.
 * </p>
 * <p>
 * <strong>Estructura:</strong>
 * </p>
 * <ul>
 *   <li><code>id</code>: UUID único del usuario</li>
 *   <li><code>username</code>: Nombre de usuario (principal de login)</li>
 *   <li><code>roles</code>: Lista de nombres de roles asignados al usuario</li>
 * </ul>
 * <p>
 * <strong>Nota de Seguridad:</strong> NO incluye la contraseña ni datos sensibles.
 * La contraseña nunca es transmitida en respuestas API.
 * </p>
 * <p>
 * <strong>Uso:</strong>
 * </p>
 * <ul>
 *   <li>Retornado por GET /api/v1/users (lista de usuarios)</li>
 *   <li>Retornado por GET /api/v1/users/{userId} (usuario específico)</li>
 *   <li>Retornado por POST /api/v1/authentication/sign-up (usuario creado)</li>
 * </ul>
 * <p>
 * <strong>Ejemplo de Respuesta JSON:</strong>
 * </p>
 * <pre>
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "username": "john.doe",
 *   "roles": ["USER", "ADMIN"]
 * }
 * </pre>
 * <p>
 * Patrón: Record inmutable para transferencia de datos entre capas REST y cliente.
 * </p>
 *
 * @param id UUID único del usuario
 * @param username nombre de usuario
 * @param roles lista de nombres de roles asignados
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.interfaces.rest.UsersController#getAllUsers()
 * @see com.codares.logistics.iam.interfaces.rest.UsersController#getUserById(UUID)
 * @see com.codares.logistics.iam.interfaces.rest.transform.UserResourceFromEntityAssembler
 */
public record UserResource(UUID id, String username, List<String> roles) {
}
