package com.codares.logistics.iam.domain.model.commands;

/**
 * Comando de Inicio de Sesión (Sign-In).
 * <p>
 * Record inmutable que transporta las credenciales necesarias para autenticar un usuario en el sistema.
 * Este comando NO contiene lógica de validación; solo transporta datos crudos de entrada.
 * Las validaciones ocurren en el manejador de comandos {@link com.codares.logistics.iam.domain.services.UserCommandService}.
 * </p>
 * <p>
 * Propósito: Servir como DTO para transferir credenciales desde la capa de presentación (REST)
 * hacia la capa de aplicación (CommandService) sin acoplamiento.
 * </p>
 *
 * @param username el nombre de usuario para autenticación. No puede ser nulo ni vacío
 * @param password la contraseña en texto plano para verificación. Debe coincidir con el hash almacenado en BD
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.services.UserCommandService#handle(SignInCommand)
 * @see com.codares.logistics.iam.domain.model.aggregates.User
 */
public record SignInCommand(String username, String password) {
}
