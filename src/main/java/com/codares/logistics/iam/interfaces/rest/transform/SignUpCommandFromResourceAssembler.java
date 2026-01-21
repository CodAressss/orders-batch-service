package com.codares.logistics.iam.interfaces.rest.transform;


import com.codares.logistics.iam.domain.model.commands.SignUpCommand;
import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.iam.interfaces.rest.resources.SignUpResource;

import java.util.ArrayList;

/**
 * Ensamblador para transformar DTO REST → Comando de Dominio (Sign-Up).
 * <p>
 * Implementa el patrón Assembler para convertir {@link SignUpResource} (DTO de entrada REST)
 * a {@link SignUpCommand} (objeto de dominio). Realiza la transformación de nombres de roles
 * (strings) a objetos {@link Role} del dominio.
 * </p>
 * <p>
 * <strong>Propósito:</strong>
 * </p>
 * <ul>
 *   <li>Traducir datos REST a Commands de dominio</li>
 *   <li>Convertir nombres de roles (strings) a entidades Role</li>
 *   <li>Manejar roles nulos o vacíos (retorna lista vacía)</li>
 *   <li>Centralizar lógica de transformación SignUpResource → SignUpCommand</li>
 * </ul>
 * <p>
 * <strong>Flujo de Procesamiento:</strong>
 * </p>
 * <ol>
 *   <li>Cliente envía POST /authentication/sign-up con JSON SignUpResource</li>
 *   <li>Spring deserializa JSON a objeto SignUpResource</li>
 *   <li>AuthenticationController delega a este Assembler</li>
 *   <li>Assembler mapea nombres de roles (List&lt;String&gt;) a objetos Role</li>
 *   <li>Crea SignUpCommand con username, password, y roles transformados</li>
 *   <li>CommandService recibe Command para crear usuario</li>
 * </ol>
 * <p>
 * <strong>Transformación de Roles:</strong>
 * </p>
 * <ul>
 *   <li>Si roles es null: retorna ArrayList&lt;Role&gt; vacío</li>
 *   <li>Si roles está vacío: retorna ArrayList&lt;Role&gt; vacío</li>
 *   <li>Si roles contiene strings: mapea cada uno a Role.toRoleFromName()</li>
 * </ul>
 * <p>
 * <strong>Notas de Implementación:</strong>
 * </p>
 * <ul>
 *   <li>Método estático para facilitar invocación desde Controlador</li>
 *   <li>Sin estado - solo realiza transformación</li>
 *   <li>NO valida datos - solo traduce estructura</li>
 *   <li>Roles inexistentes serán ignorados en CommandService</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see SignUpResource
 * @see SignUpCommand
 * @see Role
 * @see com.codares.logistics.iam.interfaces.rest.AuthenticationController#signUp(SignUpResource)
 * @see com.codares.logistics.iam.domain.services.UserCommandService
 */
public class SignUpCommandFromResourceAssembler {

    /**
     * Transforma un DTO REST SignUpResource a Comando de Dominio SignUpCommand.
     * <p>
     * Realiza la conversión de datos REST a dominio, incluyendo la transformación
     * de nombres de roles (strings de la API) a objetos {@link Role} del dominio.
     * </p>
     * <p>
     * <strong>Lógica de Transformación:</strong>
     * </p>
     * <pre>
     * SignUpResource {
     *   username: "john.doe",
     *   password: "SecurePass123",
     *   roles: ["ADMIN", "USER"]
     * }
     * ↓ (transformación)
     * SignUpCommand {
     *   username: "john.doe",
     *   password: "SecurePass123",
     *   roles: [Role{ADMIN}, Role{USER}]
     * }
     * </pre>
     * <p>
     * <strong>Manejo de Roles Nulos/Vacíos:</strong>
     * </p>
     * <ul>
     *   <li>Si resource.roles() == null: retorna ArrayList&lt;Role&gt;() vacío</li>
     *   <li>Si resource.roles().isEmpty(): retorna ArrayList&lt;Role&gt;() vacío</li>
     *   <li>El CommandService después asignará rol por defecto si es necesario</li>
     * </ul>
     *
     * @param resource DTO con datos de registro (username, password, roles)
     * @return SignUpCommand objeto de dominio listo para CommandService
     * @see SignUpResource
     * @see SignUpCommand
     * @see Role#toRoleFromName(String)
     */
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var roles = resource.roles() != null ? resource.roles().stream().map(name -> Role.toRoleFromName(name)).toList() : new ArrayList<Role>();
        return new SignUpCommand(resource.username(), resource.password(), roles);
    }
}
