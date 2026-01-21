package com.codares.logistics.iam.interfaces.rest.resources;

import java.util.List;

/**
 * DTO que representa los datos para registro de nuevo usuario (Sign-Up).
 * <p>
 * Contiene la información mínima requerida para crear una nueva cuenta de usuario
 * en el sistema. Este DTO es recibido en el body JSON de POST /api/v1/authentication/sign-up
 * y es transformado a {@link com.codares.logistics.iam.domain.model.commands.SignUpCommand}
 * mediante el patrón Assembler.
 * </p>
 * <p>
 * <strong>Estructura:</strong>
 * </p>
 * <ul>
 *   <li><code>username</code>: Nombre de usuario único que actuará como principal de login</li>
 *   <li><code>password</code>: Contraseña en texto plano (será hasheada con BCrypt antes de persistir)</li>
 *   <li><code>roles</code>: Lista opcional de nombres de roles a asignar al usuario</li>
 * </ul>
 * <p>
 * <strong>Comportamiento de Roles:</strong>
 * </p>
 * <ul>
 *   <li>Si <code>roles</code> es null: Se asigna el rol por defecto ("USER")</li>
 *   <li>Si <code>roles</code> está vacío: Se asigna el rol por defecto</li>
 *   <li>Si un rol en la lista no existe: Se ignora (no causa error)</li>
 *   <li>Si un rol existe: Se asigna al usuario</li>
 * </ul>
 * <p>
 * <strong>Validaciones Realizadas:</strong>
 * </p>
 * <ul>
 *   <li>Username debe ser único (no puede duplicarse)</li>
 *   <li>Username debe cumplir políticas de nombre de usuario</li>
 *   <li>Contraseña debe cumplir políticas de seguridad</li>
 * </ul>
 * <p>
 * <strong>Ejemplo de Solicitud JSON:</strong>
 * </p>
 * <pre>
 * POST /api/v1/authentication/sign-up
 * Content-Type: application/json
 * 
 * {
 *   "username": "jane.smith",
 *   "password": "SecurePassword456!",
 *   "roles": ["USER"]
 * }
 * </pre>
 * <p>
 * Patrón: Record inmutable para transferencia de datos sin validación previa.
 * </p>
 *
 * @param username nombre de usuario único
 * @param password contraseña en texto plano
 * @param roles lista opcional de nombres de roles (ej: "ADMIN", "USER", "MODERATOR")
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.interfaces.rest.AuthenticationController#signUp(SignUpResource)
 * @see com.codares.logistics.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler
 */
public record SignUpResource(String username, String password, List<String> roles) {
}
