package com.codares.logistics.iam.interfaces.rest.transform;

import com.codares.logistics.iam.domain.model.aggregates.User;
import com.codares.logistics.iam.interfaces.rest.resources.AuthenticatedUserResource;

/**
 * Ensamblador para transformar Entidad de Usuario + Token → DTO de Usuario Autenticado.
 * <p>
 * Este clase implementa el patrón Assembler para convertir una entidad de dominio
 * {@link User} junto con un token JWT a un DTO REST {@link AuthenticatedUserResource}.
 * Se utiliza en el flujo de autenticación para serializar la respuesta del sign-in.
 * </p>
 * <p>
 * <strong>Propósito:</strong>
 * </p>
 * <ul>
 *   <li>Desacoplar la entidad de dominio de la representación REST</li>
 *   <li>Controlar qué información se expone en la API (no expone contraseña ni detalles internos)</li>
 *   <li>Centralizar la lógica de transformación en un único lugar</li>
 *   <li>Facilitar cambios futuros en la estructura de la API</li>
 * </ul>
 * <p>
 * <strong>Flujo de Uso:</strong>
 * </p>
 * <ol>
 *   <li>AuthenticationController recibe credenciales en SignInResource</li>
 *   <li>CommandService valida y genera token JWT</li>
 *   <li>AuthenticationController delega a este Assembler para transformar resultado</li>
 *   <li>Assembler extrae datos públicos de User + token generado</li>
 *   <li>Retorna AuthenticatedUserResource serializable a JSON</li>
 * </ol>
 * <p>
 * <strong>Notas de Implementación:</strong>
 * </p>
 * <ul>
 *   <li>Método estático para facilitar invocación sin instancia</li>
 *   <li>Sin estado (stateless) - solo realiza transformación</li>
 *   <li>No valida datos - confía en que User ya está validado</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see User
 * @see AuthenticatedUserResource
 * @see com.codares.logistics.iam.interfaces.rest.AuthenticationController#signIn(com.codares.logistics.iam.interfaces.rest.resources.SignInResource)
 */
public class AuthenticatedUserResourceFromEntityAssembler {

    /**
     * Transforma una entidad User y token JWT a DTO AuthenticatedUserResource.
     * <p>
     * Extrae los datos públicos del usuario (id, username) y los combina con el
     * token JWT generado para crear la respuesta que será serializada a JSON
     * en la respuesta HTTP del sign-in.
     * </p>
     * <p>
     * <strong>Datos Incluidos en la Transformación:</strong>
     * </p>
     * <ul>
     *   <li>user.getId() → AuthenticatedUserResource.id()</li>
     *   <li>user.getUsername() → AuthenticatedUserResource.username()</li>
     *   <li>token (parámetro) → AuthenticatedUserResource.token()</li>
     * </ul>
     * <p>
     * <strong>Datos NO Incluidos (Por Seguridad):</strong>
     * </p>
     * <ul>
     *   <li>Contraseña hasheada</li>
     *   <li>Roles (aunque podrían incluirse si fuera necesario)</li>
     *   <li>Metadatos internos (createdAt, updatedAt, etc.)</li>
     * </ul>
     *
     * @param user entidad de usuario del dominio (ya validada y autenticada)
     * @param token token JWT generado para este usuario
     * @return AuthenticatedUserResource DTO listo para serializar a JSON
     * @see User
     * @see AuthenticatedUserResource
     */
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(user.getId(), user.getUsername(), token);
    }
}
