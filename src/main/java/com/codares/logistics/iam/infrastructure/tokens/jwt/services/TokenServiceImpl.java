package com.codares.logistics.iam.infrastructure.tokens.jwt.services;


import com.codares.logistics.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Implementación del Servicio de Tokens JWT (JSON Web Tokens).
 * <p>
 * Esta clase es responsable de la generación, validación y extracción de información
 * de tokens JWT. Actúa como implementación de {@link BearerTokenService} en la capa de
 * infraestructura, proporcionando funcionalidades criptográficas para la autenticación
 * stateless en el sistema.
 * </p>
 * <p>
 * <strong>Características principales:</strong>
 * </p>
 * <ul>
 *   <li>Generación de tokens JWT firmados con HMAC-SHA256</li>
 *   <li>Validación de firma y expiración del token</li>
 *   <li>Extracción de claims (datos embebidos en el token)</li>
 *   <li>Extracción de tokens Bearer del header Authorization de solicitudes HTTP</li>
 * </ul>
 * <p>
 * <strong>Configuración:</strong>
 * Los parámetros de configuración se obtienen del archivo application.properties:
 * </p>
 * <ul>
 *   <li><code>authorization.jwt.secret</code>: Clave secreta para firmar tokens (HMAC-SHA256)</li>
 *   <li><code>authorization.jwt.expiration.days</code>: Días de validez del token desde emisión</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.infrastructure.tokens.jwt.BearerTokenService
 * @see io.jsonwebtoken.Jwts
 * @see javax.crypto.SecretKey
 */
@Service
public class TokenServiceImpl implements BearerTokenService {
    private final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private static final int TOKEN_BEGIN_INDEX = 7;


    @Value("${authorization.jwt.secret}")
    private String secret;

    @Value("${authorization.jwt.expiration.days}")
    private int expirationDays;

    /**
     * Genera un token JWT a partir de un objeto de autenticación de Spring Security.
     * <p>
     * Extrae el nombre de usuario y sus autoridades del objeto {@link Authentication},
     * luego construye un token JWT con estos datos incrustados como claims.
     * El token es firmado con la clave secreta configurada y tiene validez por
     * el número de días especificado en la configuración.
     * </p>
     *
     * @param authentication objeto de autenticación que contiene usuario y autoridades
     * @return String token JWT codificado en Base64 y firmado
     * @see org.springframework.security.core.Authentication
     * @see org.springframework.security.core.GrantedAuthority
     */
    @Override
    public String generateToken(Authentication authentication) {
        var roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();
        return buildTokenWithDefaultParameters(authentication.getName(), roles);
    }

    /**
     * Genera un token JWT para un nombre de usuario sin roles.
     * <p>
     * Este método es utilizado cuando se requiere crear un token para un usuario
     * sin necesidad de incluir sus roles en el token (lista vacía).
     * </p>
     *
     * @param username nombre de usuario para incluir en el claim "sub" (subject)
     * @return String token JWT codificado y firmado
     */
    public String generateToken(String username) {
        return buildTokenWithDefaultParameters(username, List.of());
    }

    /**
     * Genera un token JWT con nombre de usuario y lista de roles.
     * <p>
     * Permite especificar explícitamente los roles a incluir en el token.
     * Los roles se incrustados como claim "roles" en el payload del token para
     * posterior validación de autorización.
     * </p>
     *
     * @param username nombre de usuario para el claim "sub"
     * @param roles lista de autoridades/roles a incluir en el token
     * @return String token JWT con claims de usuario y roles
     */
    public String generateToken(String username, List<String> roles) {
        return buildTokenWithDefaultParameters(username, roles);
    }

    /**
     * Construye un token JWT con parámetros estándar (fecha emisión y expiración).
     * <p>
     * Método privado que realiza la construcción real del token usando la biblioteca JJWT.
     * Define la fecha de emisión como ahora y calcula la expiración sumando los días
     * configurados. Firma el token con la clave HMAC-SHA256.
     * </p>
     *
     * @param username nombre de usuario para el subject del token
     * @param roles lista de roles a incluir en el claim "roles"
     * @return String token JWT compacto (serializado)
     */
    private String buildTokenWithDefaultParameters(String username, List<String> roles) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        var key = getSigningKey();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Extrae el nombre de usuario (claim "subject") de un token JWT.
     * <p>
     * El nombre de usuario es el identificador único del usuario autenticado,
     * almacenado en el claim estándar "sub" del token.
     * </p>
     *
     * @param token token JWT del cual extraer el nombre de usuario
     * @return String nombre de usuario contenido en el token
     * @throws io.jsonwebtoken.JwtException si el token es inválido o no puede ser procesado
     */
    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Valida la firma y expiración de un token JWT.
     * <p>
     * Intenta parsear y verificar el token usando la clave secreta. Si el token es válido
     * (firma correcta y no expirado), retorna true. Captura todas las excepciones posibles
     * de JWT inválidos (firma inválida, formato incorrecto, expirado, etc.) y las registra
     * en los logs sin relanzarlas.
     * </p>
     *
     * @param token token JWT a validar
     * @return boolean true si el token es válido (firma y expiración correctas), false en caso contrario
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            LOGGER.info("Token is valid");
            return true;
        }  catch (SignatureException e) {
            LOGGER.error("Invalid JSON Web Token Signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JSON Web Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("JSON Web Token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JSON Web Token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JSON Web Token claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extrae un claim específico del token usando una función provista.
     * <p>
     * Método genérico que utiliza una función para extraer y transformar un claim específico
     * del payload del token. La función recibe el objeto {@link Claims} y retorna el valor
     * transformado.
     * </p>
     *
     * @param token token JWT del cual extraer el claim
     * @param claimsResolvers función que extrae y transforma el claim deseado
     * @param <T> tipo del claim a retornar
     * @return T valor del claim extraído y transformado
     * @see io.jsonwebtoken.Claims
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Extrae todos los claims del payload del token JWT.
     * <p>
     * Parsea el token verificando su firma con la clave secreta y retorna
     * el objeto {@link Claims} que contiene todos los datos embebidos en el token.
     * </p>
     *
     * @param token token JWT a parsear
     * @return Claims objeto con todos los claims del token
     * @throws io.jsonwebtoken.JwtException si la firma es inválida
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Obtiene la clave de firmado HMAC-SHA256 a partir de la clave secreta configurada.
     * <p>
     * Convierte la clave secreta string (del archivo de configuración) a bytes UTF-8
     * y luego genera una clave HMAC-SHA256 que será utilizada para firmar y verificar
     * tokens JWT.
     * </p>
     *
     * @return SecretKey clave HMAC-SHA256 lista para firmar/verificar
     * @see javax.crypto.SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Verifica si el parámetro de autorización tiene contenido no vacío.
     *
     * @param authorizationParameter valor del header Authorization
     * @return boolean true si tiene contenido, false si está vacío o nulo
     */
    private boolean isTokenPresentIn(String authorizationParameter) {
        return StringUtils.hasText(authorizationParameter);
    }

    /**
     * Verifica si el parámetro de autorización comienza con el prefijo "Bearer ".
     *
     * @param authorizationParameter valor del header Authorization
     * @return boolean true si comienza con "Bearer ", false en caso contrario
     */
    private boolean isBearerTokenIn(String authorizationParameter) {
        return authorizationParameter.startsWith(BEARER_TOKEN_PREFIX);
    }

    /**
     * Extrae el token JWT removiendo el prefijo "Bearer " del header.
     *
     * @param authorizationHeaderParameter valor completo del header "Authorization: Bearer &lt;token&gt;"
     * @return String el token sin el prefijo "Bearer "
     */
    private String extractTokenFrom(String authorizationHeaderParameter) {
        return authorizationHeaderParameter.substring(TOKEN_BEGIN_INDEX);
    }

    /**
     * Obtiene el valor del header "Authorization" de la solicitud HTTP.
     *
     * @param request solicitud HTTP que contiene los headers
     * @return String valor del header Authorization o null si no existe
     * @see jakarta.servlet.http.HttpServletRequest
     */
    private String getAuthorizationParameterFrom(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_PARAMETER_NAME);
    }

    /**
     * Extrae el token Bearer del header Authorization de una solicitud HTTP.
     * <p>
     * Obtiene el header "Authorization", verifica que contenga un token Bearer,
     * y retorna el token sin el prefijo "Bearer ". Si el header no existe o no
     * contiene un token Bearer, retorna null.
     * </p>
     *
     * @param request solicitud HTTP que contiene el header Authorization
     * @return String token JWT sin prefijo, o null si no se encuentra token Bearer
     * @see jakarta.servlet.http.HttpServletRequest
     */
    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String parameter = getAuthorizationParameterFrom(request);
        if (isTokenPresentIn(parameter) && isBearerTokenIn(parameter)) return extractTokenFrom(parameter);
        return null;
    }

}
