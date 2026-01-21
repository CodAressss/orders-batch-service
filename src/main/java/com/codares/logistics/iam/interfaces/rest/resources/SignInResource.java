package com.codares.logistics.iam.interfaces.rest.resources;

/**
 * DTO que representa las credenciales para autenticación (Sign-In).
 * <p>
 * Contiene los datos mínimos requeridos para que un usuario se autentique en el sistema.
 * Este DTO es recibido en el body JSON de la solicitud POST /api/v1/authentication/sign-in
 * y luego es transformado a {@link com.codares.logistics.iam.domain.model.commands.SignInCommand}
 * mediante el patrón Assembler.
 * </p>
 * <p>
 * <strong>Estructura:</strong>
 * </p>
 * <ul>
 *   <li><code>username</code>: Nombre de usuario registrado en el sistema</li>
 *   <li><code>password</code>: Contraseña en texto plano (será validada contra hash BCrypt)</li>
 * </ul>
 * <p>
 * <strong>Validaciones Realizadas:</strong>
 * </p>
 * <ul>
 *   <li>Username debe corresponder a un usuario registrado</li>
 *   <li>Contraseña debe coincidir con el hash almacenado en BD</li>
 *   <li>Usuario debe estar activo (no deshabilitado)</li>
 * </ul>
 * <p>
 * <strong>Ejemplo de Solicitud JSON:</strong>
 * </p>
 * <pre>
 * POST /api/v1/authentication/sign-in
 * Content-Type: application/json
 * 
 * {
 *   "username": "john.doe",
 *   "password": "MySecurePassword123!"
 * }
 * </pre>
 * <p>
 * Patrón: Record inmutable (DTO) que trasporta datos crudos sin validación.
 * </p>
 *
 * @param username nombre de usuario registrado
 * @param password contraseña en texto plano
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.interfaces.rest.AuthenticationController#signIn(SignInResource)
 * @see com.codares.logistics.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler
 */
public record SignInResource(String username, String password) {
}
