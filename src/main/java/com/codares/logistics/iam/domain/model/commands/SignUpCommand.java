package com.codares.logistics.iam.domain.model.commands;


import com.codares.logistics.iam.domain.model.entities.Role;

import java.util.List;

/**
 * Comando de Registro (Sign-Up).
 * <p>
 * Record inmutable que transporta los datos necesarios para registrar un nuevo usuario en el sistema.
 * Este comando NO contiene lógica de validación; solo transporta datos crudos de entrada.
 * Las validaciones ocurren en el manejador de comandos {@link com.codares.logistics.iam.domain.services.UserCommandService}.
 * </p>
 * <p>
 * Propósito: Servir como DTO para transferir datos de registro desde la capa de presentación (REST)
 * hacia la capa de aplicación (CommandService) sin acoplamiento.
 * </p>
 *
 * @param username el nombre de usuario único para el nuevo usuario. No puede ser nulo, vacío ni duplicado
 * @param password la contraseña en texto plano. Será hasheada con BCrypt antes de persistencia
 * @param roles la lista de roles a asignar. Si es nula o vacía, se asigna automáticamente ROLE_USER
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.services.UserCommandService#handle(SignUpCommand)
 * @see com.codares.logistics.iam.domain.model.aggregates.User
 * @see Role
 */
public record SignUpCommand(String username, String password, List<Role> roles) {
}
