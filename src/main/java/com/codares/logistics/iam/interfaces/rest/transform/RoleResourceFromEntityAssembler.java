package com.codares.logistics.iam.interfaces.rest.transform;


import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.iam.interfaces.rest.resources.RoleResource;

/**
 * Ensamblador para transformar Entidad de Rol → DTO de Rol REST.
 * <p>
 * Implementa el patrón Assembler para convertir la entidad de dominio {@link Role}
 * a su representación REST {@link RoleResource}. Se utiliza en endpoints de consulta
 * de roles para serializar la información a JSON.
 * </p>
 * <p>
 * <strong>Propósito:</strong>
 * </p>
 * <ul>
 *   <li>Desacoplar entidades de dominio de DTOs REST</li>
 *   <li>Centralizar lógica de transformación Role → RoleResource</li>
 *   <li>Permitir cambios en estructura REST sin afectar el dominio</li>
 *   <li>Facilitar selección de qué datos exponer en API</li>
 * </ul>
 * <p>
 * <strong>Flujo de Uso:</strong>
 * </p>
 * <ol>
 *   <li>RolesController recibe solicitud GET /api/v1/roles</li>
 *   <li>Delega a RoleQueryService para obtener lista de roles</li>
 *   <li>Mapea cada Role a RoleResource usando este Assembler</li>
 *   <li>Retorna List&lt;RoleResource&gt; serializable a JSON</li>
 * </ol>
 * <p>
 * <strong>Datos Transformados:</strong>
 * </p>
 * <ul>
 *   <li>role.getId() → RoleResource.id()</li>
 *   <li>role.getStringName() → RoleResource.name()</li>
 * </ul>
 * <p>
 * <strong>Notas de Implementación:</strong>
 * </p>
 * <ul>
 *   <li>Método estático para facilitar stream().map()</li>
 *   <li>Sin estado (stateless) - realiza solo transformación</li>
 *   <li>No valida datos - confía en Role ya validado</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see Role
 * @see RoleResource
 * @see com.codares.logistics.iam.interfaces.rest.RolesController#getAllRoles()
 */
public class RoleResourceFromEntityAssembler {

    /**
     * Transforma una entidad Role a DTO RoleResource.
     * <p>
     * Extrae los datos públicos del rol (id y nombre) y los empaqueta
     * en el DTO que será serializado a JSON en la respuesta HTTP.
     * </p>
     * <p>
     * <strong>Uso Típico:</strong>
     * </p>
     * <pre>
     * var roleResources = roles.stream()
     *     .map(RoleResourceFromEntityAssembler::toResourceFromEntity)
     *     .toList();
     * </pre>
     *
     * @param role entidad de rol del dominio
     * @return RoleResource DTO con los datos del rol
     * @see Role
     * @see RoleResource
     */
    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}
