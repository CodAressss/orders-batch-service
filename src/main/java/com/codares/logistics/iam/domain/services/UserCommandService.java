package com.codares.logistics.iam.domain.services;

import com.codares.logistics.iam.domain.model.aggregates.User;
import com.codares.logistics.iam.domain.model.commands.SignInCommand;
import com.codares.logistics.iam.domain.model.commands.SignUpCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

/**
 * Servicio de Dominio para Comandos de Usuarios.
 * <p>
 * Esta interfaz define el contrato para manejar comandos (commands) relacionados con la gestión
 * de usuarios en el contexto acotado de Gestión de Identidades y Acceso (IAM).
 * Implementa el patrón CQRS para separación de responsabilidades de escritura.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Autenticación de usuarios (inicio de sesión)</li>
 *   <li>Registro de nuevos usuarios</li>
 *   <li>Generación de tokens JWT para autenticación stateless</li>
 *   <li>Validación de credenciales y reglas de negocio</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see User
 * @see SignInCommand
 * @see SignUpCommand
 */
public interface UserCommandService {
    /**
     * Maneja el comando de inicio de sesión (autenticación).
     * <p>
     * Valida las credenciales del usuario y genera un token JWT si la autenticación es exitosa.
     * Retorna un par inmutable con el usuario autenticado y su token JWT.
     * </p>
     *
     * @param command el comando {@link SignInCommand} que contiene username y password
     * @return {@link Optional} con un {@link ImmutablePair} de usuario y token JWT si la autenticación es exitosa,
     *         {@link Optional#empty()} si las credenciales son inválidas
     * @throws com.codares.logistics.shared.domain.exceptions.ResourceNotFoundException si el usuario no existe
     */
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);

    /**
     * Maneja el comando de registro (creación de nuevo usuario).
     * <p>
     * Crea un nuevo usuario en el sistema con el nombre de usuario y contraseña especificados.
     * La contraseña es hasheada con BCrypt antes de ser persistida. Si no se especifican roles,
     * se asigna automáticamente el rol {@link com.codares.logistics.iam.domain.model.valueobjects.Roles#ROLE_USER}.
     * </p>
     *
     * @param command el comando {@link SignUpCommand} que contiene username, password y roles opcionales
     * @return {@link Optional} con la entidad {@link User} creada si el registro es exitoso,
     *         {@link Optional#empty()} si el usuario ya existe
     * @throws com.codares.logistics.shared.domain.exceptions.ResourceAlreadyExistsException si el username ya existe
     */
    Optional<User> handle(SignUpCommand command);


}
