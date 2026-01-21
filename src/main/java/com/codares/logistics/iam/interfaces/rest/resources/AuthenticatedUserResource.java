package com.codares.logistics.iam.interfaces.rest.resources;

import java.util.UUID;

/**
 * DTO que representa la respuesta de autenticación exitosa (Sign-In).
 * <p>
 * Contiene los datos del usuario autenticado junto con su token JWT, que debe ser
 * incluido en el header Authorization de solicitudes posteriores para mantener
 * la autenticación stateless.
 * </p>
 * <p>
 * <strong>Estructura:</strong>
 * </p>
 * <ul>
 *   <li><code>id</code>: UUID único del usuario en el sistema</li>
 *   <li><code>username</code>: Nombre de usuario (principal de login)</li>
 *   <li><code>token</code>: Token JWT para autenticación en solicitudes posteriores</li>
 * </ul>
 * <p>
 * <strong>Uso:</strong> Retornado por POST /api/v1/authentication/sign-in
 * </p>
 * <p>
 * <strong>Ejemplo de Token en Solicitud Posterior:</strong>
 * </p>
 * <pre>
 * GET /api/v1/users
 * Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
 * </pre>
 * <p>
 * Patrón: Record inmutable (Java 16+) para transportar datos entre capas
 * sin necesidad de getters/setters explícitos.
 * </p>
 *
 * @param id UUID único del usuario
 * @param username nombre de usuario
 * @param token JWT para autenticación stateless
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.interfaces.rest.AuthenticationController#signIn(SignInResource)
 */
public record AuthenticatedUserResource(UUID id, String username, String token) {

}
