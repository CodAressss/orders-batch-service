package com.codares.logistics.iam.interfaces.acl;

import com.codares.logistics.iam.domain.model.commands.SignUpCommand;
import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.iam.domain.model.queries.GetUserByIdQuery;
import com.codares.logistics.iam.domain.model.queries.GetUserByUsernameQuery;
import com.codares.logistics.iam.domain.services.UserCommandService;
import com.codares.logistics.iam.domain.services.UserQueryService;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fachada del Contexto de Identidad y Acceso (IAM Context Facade).
 * <p>
 * Esta clase implementa el patrón ACL (Anti-Corruption Layer) para permitir que otros
 * Bounded Contexts accedan a funcionalidades del contexto IAM sin acoplarse a sus
 * detalles internos. Actúa como contrato público hacia otros contextos limitando
 * el acceso solo a métodos de creación y consulta de usuarios.
 * </p>
 * <p>
 * <strong>Propósito del ACL:</strong>
 * </p>
 * <ul>
 *   <li>Protege el dominio IAM de cambios en otros contextos</li>
 *   <li>Proporciona interfaz simplificada con parámetros primitivos (UUID, String)</li>
 *   <li>Evita que otros contextos conozcan Commands, Queries, o detalles internos</li>
 *   <li>Maneja traducción: primitivos ↔ Commands/Queries ↔ Servicios de aplicación</li>
 *   <li>Centraliza lógica de orquestación inter-contextos</li>
 * </ul>
 * <p>
 * <strong>Métodos Públicos (Contrato Externo):</strong>
 * </p>
 * <ul>
 *   <li>{@link #createUser(String, String)} - Crear usuario con rol por defecto</li>
 *   <li>{@link #createUser(String, String, List)} - Crear usuario con roles específicos</li>
 *   <li>{@link #fetchUserIdByUsername(String)} - Obtener ID desde username</li>
 *   <li>{@link #fetchUsernameByUserId(UUID)} - Obtener username desde ID</li>
 * </ul>
 * <p>
 * <strong>Principios SOLID Aplicados:</strong>
 * </p>
 * <ul>
 *   <li><strong>SRP:</strong> Facade solo orquesta, sin lógica de negocio</li>
 *   <li><strong>DIP:</strong> Depende de abstracciones (CommandService, QueryService)</li>
 *   <li><strong>ISP:</strong> Interfaz expone solo métodos que otros contextos necesitan</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.services.UserCommandService
 * @see com.codares.logistics.iam.domain.services.UserQueryService
 */
public class IamContextFacade {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    /**
     * Constructor que inyecta los servicios de aplicación necesarios.
     *
     * @param userCommandService servicio para operaciones de escritura de usuarios
     * @param userQueryService servicio para operaciones de lectura de usuarios
     */
    public IamContextFacade(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    /**
     * Crea un nuevo usuario con el nombre de usuario y contraseña proporcionados.
     * <p>
     * El usuario se crea con el rol por defecto del sistema ("USER"). La contraseña
     * se hashea automáticamente con BCrypt antes de persistirse en la base de datos.
     * </p>
     * <p>
     * <strong>Parámetros:</strong> Solo acepta primitivos (String, UUID) evitando
     * que otros contextos conozcan Commands o entidades de dominio.
     * </p>
     * <p>
     * <strong>Retorno:</strong> Si la creación es exitosa, retorna el UUID del usuario.
     * Si falla (ej: username duplicado), retorna UUID de ceros (nil-like value).
     * </p>
     *
     * @param username nombre de usuario único en el sistema (principal de login)
     * @param password contraseña en texto plano (será hasheada por BCrypt)
     * @return UUID del usuario creado, o UUID de ceros si la creación falla
     * @see SignUpCommand
     * @see Role#getDefaultRole()
     */
    public UUID createUser(String username, String password) {
        var signUpCommand = new SignUpCommand(username, password, List.of(Role.getDefaultRole()));
        var result = userCommandService.handle(signUpCommand);
        if (result.isEmpty()) return UUID.fromString("00000000-0000-0000-0000-000000000000");
        return result.get().getId();
    }

    /**
     * Crea un nuevo usuario con los roles específicos proporcionados.
     * <p>
     * Permite crear un usuario con una lista de roles personalizada. Si un nombre
     * de rol no existe en el sistema, se ignora sin error. Si la lista de roles
     * es nula o vacía, se asigna el rol por defecto.
     * </p>
     * <p>
     * <strong>Parámetros:</strong> Solo primitivos (String, UUID, List&lt;String&gt;) para
     * mantener independencia entre contextos.
     * </p>
     * <p>
     * <strong>Comportamiento de Roles:</strong>
     * </p>
     * <ul>
     *   <li>Si roleNames es null: se asigna rol por defecto</li>
     *   <li>Si roleNames está vacío: se asigna rol por defecto</li>
     *   <li>Si rol no existe: se ignora (no error)</li>
     *   <li>Si rol existe: se asigna al usuario</li>
     * </ul>
     *
     * @param username nombre de usuario único en el sistema
     * @param password contraseña en texto plano (será hasheada por BCrypt)
     * @param roleNames lista de nombres de roles a asignar (ej: "ADMIN", "USER", "MODERATOR")
     * @return UUID del usuario creado, o UUID de ceros si la creación falla
     * @see SignUpCommand
     * @see Role#toRoleFromName(String)
     */
    public UUID createUser(String username, String password, List<String> roleNames) {
        var roles = roleNames != null ? roleNames.stream().map(Role::toRoleFromName).toList() : new ArrayList<Role>();
        var signUpCommand = new SignUpCommand(username, password, roles);
        var result = userCommandService.handle(signUpCommand);
        if (result.isEmpty()) return UUID.fromString("00000000-0000-0000-0000-000000000000");
        return result.get().getId();
    }

    /**
     * Obtiene el UUID del usuario con el nombre de usuario proporcionado.
     * <p>
     * Método de consulta para obtener el identificador único de un usuario a partir
     * de su username (principal de login). Útil para otros contextos que necesitan
     * referenciar a un usuario pero solo conocen su nombre de usuario.
     * </p>
     * <p>
     * <strong>Retorno:</strong> Si el usuario existe, retorna su UUID. Si no existe,
     * retorna UUID de ceros (nil-like value para mantener interfaz consistente).
     * </p>
     *
     * @param username nombre de usuario del cual obtener el ID
     * @return UUID del usuario, o UUID de ceros si el usuario no existe
     * @see GetUserByUsernameQuery
     */
    public UUID fetchUserIdByUsername(String username) {
        var getUserByUsernameQuery = new GetUserByUsernameQuery(username);
        var result = userQueryService.handle(getUserByUsernameQuery);
        if (result.isEmpty()) return UUID.fromString("00000000-0000-0000-0000-000000000000");
        return result.get().getId();
    }

    /**
     * Obtiene el nombre de usuario dado su UUID.
     * <p>
     * Método de consulta para obtener el username de un usuario a partir de su
     * identificador único (UUID). Útil para otros contextos que tienen referencias
     * a usuarios pero necesitan obtener su nombre de usuario para auditoría, logging, etc.
     * </p>
     * <p>
     * <strong>Retorno:</strong> Si el usuario existe, retorna su username. Si no existe,
     * retorna string vacío (Strings.EMPTY) para mantener tipo primitivo consistente.
     * </p>
     *
     * @param userId UUID único del usuario
     * @return nombre de usuario, o string vacío si el usuario no existe
     * @see GetUserByIdQuery
     */
    public String fetchUsernameByUserId(UUID userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(getUserByIdQuery);
        if (result.isEmpty()) return Strings.EMPTY;
        return result.get().getUsername();
    }

}
