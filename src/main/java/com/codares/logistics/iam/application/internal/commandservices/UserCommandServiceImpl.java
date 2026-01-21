package com.codares.logistics.iam.application.internal.commandservices;

import com.codares.logistics.iam.application.internal.outboundservices.hashing.HashingService;
import com.codares.logistics.iam.application.internal.outboundservices.tokens.TokenService;
import com.codares.logistics.iam.domain.model.aggregates.User;
import com.codares.logistics.iam.domain.model.commands.SignInCommand;
import com.codares.logistics.iam.domain.model.commands.SignUpCommand;
import com.codares.logistics.iam.domain.services.UserCommandService;
import com.codares.logistics.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.codares.logistics.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import com.codares.logistics.shared.domain.exceptions.ResourceAlreadyExistsException;
import com.codares.logistics.shared.domain.exceptions.ResourceNotFoundException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementación del servicio de comandos para usuarios.
 * <p>
 * Esta clase implementa la interfaz {@link UserCommandService} y proporciona la lógica
 * de orquestación para manejar comandos de autenticación y autorización:
 * <ul>
 *   <li>Inicio de sesión (Sign-In): Valida credenciales y genera JWT</li>
 *   <li>Registro (Sign-Up): Crea nuevos usuarios y los persiste en BD</li>
 * </ul>
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Orquestar validaciones de credenciales</li>
 *   <li>Gestionar encoding/decoding de contraseñas usando BCrypt</li>
 *   <li>Generar tokens JWT con roles incluidos para autorización stateless</li>
 *   <li>Garantizar idempotencia y validar reglas de negocio</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see UserCommandService
 * @see SignInCommand
 * @see SignUpCommand
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    /**
     * Repositorio de usuarios para acceso y persistencia de datos.
     */
    private final UserRepository userRepository;

    /**
     * Servicio de hashing para codificación y validación de contraseñas.
     */
    private final HashingService hashingService;

    /**
     * Servicio de generación de tokens JWT para autenticación stateless.
     */
    private final TokenService tokenService;

    /**
     * Repositorio de roles para búsqueda y asignación de roles a usuarios.
     */
    private final RoleRepository roleRepository;

    /**
     * Constructor con inyección de dependencias.
     * <p>
     * Inicializa todos los servicios y repositorios requeridos para la orquestación
     * de comandos de usuario.
     * </p>
     *
     * @param userRepository el repositorio de usuarios
     * @param hashingService el servicio de hashing de contraseñas
     * @param tokenService el servicio de generación de tokens JWT
     * @param roleRepository el repositorio de roles
     */
    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService, TokenService tokenService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
    }

    /**
     * Maneja el comando de inicio de sesión (Sign-In).
     * <p>
     * Orquesta el proceso de autenticación:
     * <ol>
     *   <li>Busca el usuario por nombre de usuario en la BD</li>
     *   <li>Valida que el usuario exista; si no, lanza excepción</li>
     *   <li>Compara la contraseña proporcionada con la almacenada (hasheada)</li>
     *   <li>Extrae los roles del usuario desde la BD</li>
     *   <li>Genera un JWT con los roles incluidos en claims para autorización stateless</li>
     *   <li>Retorna el usuario y el token JWT</li>
     * </ol>
     * </p>
     * <p>
     * Flujo de validación:
     * <ul>
     *   <li>Usuario no encontrado → InvalidArgumentException: "User not found"</li>
     *   <li>Contraseña incorrecta → InvalidArgumentException: "Invalid password"</li>
     *   <li>Éxito → Retorna tupla (usuario, token JWT)</li>
     * </ul>
     * </p>
     *
     * @param command el comando {@link SignInCommand} con nombre de usuario y contraseña en texto plano
     * @return {@link Optional} conteniendo tupla {@link ImmutablePair} con el usuario autenticado y su JWT
     * @throws InvalidArgumentException si el usuario no existe o la contraseña es inválida
     *
     * @see SignInCommand
     * @see TokenService#generateToken(String, List)
     */
    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty())
            throw new InvalidArgumentException("User not found");
        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new InvalidArgumentException("Invalid password");
        // Extract roles from user and generate token with roles included
        var roles = user.get().getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        var token = tokenService.generateToken(user.get().getUsername(), roles);
        return Optional.of(ImmutablePair.of(user.get(), token));
    }

    /**
     * Maneja el comando de registro (Sign-Up).
     * <p>
     * Orquesta el proceso de creación de nuevo usuario:
     * <ol>
     *   <li>Valida que el nombre de usuario no exista en la BD</li>
     *   <li>Resuelve los roles especificados desde la BD</li>
     *   <li>Codifica la contraseña usando BCrypt</li>
     *   <li>Crea la entidad Usuario con credenciales y roles</li>
     *   <li>Persiste el usuario en la BD</li>
     *   <li>Retorna el usuario creado</li>
     * </ol>
     * </p>
     * <p>
     * Flujo de validación:
     * <ul>
     *   <li>Nombre de usuario duplicado → ResourceAlreadyExistsException: "Username already exists"</li>
     *   <li>Rol no encontrado → ResourceNotFoundException: "Role name not found"</li>
     *   <li>Éxito → Retorna el usuario persistido</li>
     * </ul>
     * </p>
     *
     * @param command el comando {@link SignUpCommand} con nombre de usuario, contraseña y roles
     * @return {@link Optional} conteniendo el usuario creado con credenciales y roles asignados
     * @throws ResourceAlreadyExistsException si el nombre de usuario ya existe o algún rol especificado no existe
     *
     * @see SignUpCommand
     * @see Role
     */
    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new ResourceAlreadyExistsException("User already exists");
        var roles = command.roles().stream().map(role -> roleRepository.findByName(role.getName()).orElseThrow(() -> new ResourceNotFoundException("Role name not found"))).toList();
        var user = new User(command.username(), hashingService.encode(command.password()), roles);
        userRepository.save(user);
        return userRepository.findByUsername(command.username());
    }
}
