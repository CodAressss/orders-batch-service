package com.codares.logistics.iam.application.internal.outboundservices.tokens;

import java.util.List;

/**
 * Servicio de Generación y Validación de Tokens JWT.
 * <p>
 * Esta interfaz define el contrato para gestionar tokens JSON Web Token (JWT) utilizados en
 * autenticación y autorización stateless. Permite generar tokens con información del usuario
 * y roles, extraer información del token y validar su integridad.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Generar tokens JWT con información del usuario y roles</li>
 *   <li>Extraer información del usuario desde el token</li>
 *   <li>Validar la integridad y vigencia del token</li>
 *   <li>Manejar firma criptográfica (HS512 o RSA)</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see io.jsonwebtoken.Jwts
 */
public interface TokenService {

    /**
     * Genera un token JWT para un usuario sin roles.
     * <p>
     * Crea un token firmado con el nombre de usuario. El token tendrá una vigencia
     * configurada y contendrá el username como claim principal.
     * </p>
     *
     * @param username el nombre de usuario para incluir en el token
     * @return String el token JWT firmado y codificado en Base64
     * @throws IllegalArgumentException si el username es null o vacío
     */
    String generateToken(String username);

    /**
     * Genera un token JWT para un usuario con roles específicos.
     * <p>
     * Crea un token firmado que incluye el nombre de usuario y la lista de roles asignados.
     * Los roles se incluyen como claim personalizado para ser utilizados en autorización
     * stateless sin consultar la base de datos en cada solicitud.
     * </p>
     *
     * @param username el nombre de usuario para incluir en el token
     * @param roles la lista de roles del usuario a incluir como claim en el token
     * @return String el token JWT firmado y codificado en Base64
     * @throws IllegalArgumentException si el username es null o la lista de roles es null
     */
    String generateToken(String username, List<String> roles);

    /**
     * Extrae el nombre de usuario desde un token JWT.
     * <p>
     * Parsea el token, valida su firma y extrae el claim de username.
     * Si el token es inválido, lanzará una excepción.
     * </p>
     *
     * @param token el token JWT del cual extraer el username
     * @return String el nombre de usuario contenido en el token
     * @throws io.jsonwebtoken.JwtException si el token es inválido o su firma no es válida
     * @throws IllegalArgumentException si el token es null o vacío
     */
    String getUsernameFromToken(String token);

    /**
     * Valida la integridad y vigencia de un token JWT.
     * <p>
     * Verifica que:
     * <ul>
     *   <li>La firma del token es válida (no ha sido manipulado)</li>
     *   <li>El token no ha expirado</li>
     *   <li>La estructura del token es correcta</li>
     * </ul>
     * </p>
     *
     * @param token el token JWT a validar
     * @return boolean {@code true} si el token es válido y vigente, {@code false} en caso contrario
     * @throws IllegalArgumentException si el token es null o vacío
     */
    boolean validateToken(String token);
}
