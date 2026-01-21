package com.codares.logistics.iam.interfaces.rest.transform;


import com.codares.logistics.iam.domain.model.aggregates.User;
import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.iam.interfaces.rest.resources.UserResource;

/**
 * Ensamblador para transformar Entidad de Usuario → DTO de Usuario REST.
 * <p>
 * Implementa el patrón Assembler para convertir la entidad de dominio {@link User}
 * (Aggregate Root) a su representación REST {@link UserResource}. Se utiliza en
 * endpoints de consulta y creación de usuarios para serializar datos a JSON.
 * </p>
 * <p>
 * <strong>Propósito:</strong>
 * </p>
 * <ul>
 *   <li>Desacoplar Aggregate Root de dominio de DTO REST</li>
 *   <li>Controlar qué información se expone en API (no contraseña ni detalles internos)</li>
 *   <li>Centralizar lógica de transformación User → UserResource</li>
 *   <li>Transformar roles de entidades a nombres para serialización</li>
 * </ul>
 * <p>
 * <strong>Flujo de Uso:</strong>
 * </p>
 * <ol>
 *   <li>UsersController recibe solicitud GET /api/v1/users o GET /api/v1/users/{id}</li>
 *   <li>Delega a UserQueryService para obtener usuario(s)</li>
 *   <li>Mapea cada User a UserResource usando este Assembler</li>
 *   <li>Retorna UserResource o List&lt;UserResource&gt; serializable a JSON</li>
 * </ol>
 * <p>
 * <strong>Datos Transformados:</strong>
 * </p>
 * <ul>
 *   <li>user.getId() → UserResource.id()</li>
 *   <li>user.getUsername() → UserResource.username()</li>
 *   <li>user.getRoles() (List&lt;Role&gt;) → UserResource.roles() (List&lt;String&gt;) con nombres</li>
 * </ul>
 * <p>
 * <strong>Datos NO Incluidos (Por Seguridad):</strong>
 * </p>
 * <ul>
 *   <li>Contraseña hasheada</li>
 *   <li>Fecha de creación/modificación</li>
 *   <li>Detalles internos de auditoría</li>
 * </ul>
 * <p>
 * <strong>Notas de Implementación:</strong>
 * </p>
 * <ul>
 *   <li>Método estático para facilitar stream().map()</li>
 *   <li>Sin estado (stateless) - realiza solo transformación</li>
 *   <li>Los roles se transforman de entidades a strings para API</li>
 *   <li>No valida datos - confía en User ya validado</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see User
 * @see Role
 * @see UserResource
 * @see com.codares.logistics.iam.interfaces.rest.UsersController#getAllUsers()
 * @see com.codares.logistics.iam.interfaces.rest.UsersController#getUserById(java.util.UUID)
 * @see com.codares.logistics.iam.interfaces.rest.AuthenticationController#signUp(com.codares.logistics.iam.interfaces.rest.resources.SignUpResource)
 */
public class UserResourceFromEntityAssembler {

    /**
     * Transforma una entidad User a DTO UserResource.
     * <p>
     * Extrae los datos públicos del usuario (id, username) y transforma
     * sus roles de objetos {@link Role} a nombres de strings para que
     * puedan ser serializados fácilmente a JSON.
     * </p>
     * <p>
     * <strong>Transformación de Roles:</strong>
     * </p>
     * <pre>
     * User {id, username, roles=[Role{ADMIN}, Role{USER}]}
     * ↓ (mapeo de roles)
     * UserResource {id, username, roles=["ADMIN", "USER"]}
     * </pre>
     * <p>
     * <strong>Seguridad:</strong> La contraseña (hasheada) nunca se incluye
     * en la respuesta, incluso aunque esté presente en la entidad User.
     * </p>
     *
     * @param user entidad User del dominio (con roles cargados)
     * @return UserResource DTO con datos públicos listo para JSON
     * @see User
     * @see Role#getStringName()
     * @see UserResource
     */
    public static UserResource toResourceFromEntity(User user) {
        var roles = user.getRoles().stream().map(Role::getStringName).toList();
        return new UserResource(user.getId(), user.getUsername(), roles);
    }
}
