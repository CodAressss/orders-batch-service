package com.codares.logistics.iam.infrastructure.tokens.jwt;


import com.codares.logistics.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

/**
 * Interfaz Marcadora para el Servicio de Tokens Bearer (JWT).
 * <p>
 * Esta interfaz marca el servicio de generación y validación de tokens JWT implementando
 * la interfaz {@link TokenService} del dominio con funcionalidades adicionales para
 * extracción de tokens de solicitudes HTTP.
 * </p>
 * <p>
 * Propósito: Actuar como adaptador (ACL - Anti-Corruption Layer) entre la interfaz de dominio
 * {@link TokenService} y la implementación específica de JWT, permitiendo inyección transparente.
 * </p>
 * <p>
 * Patrón: Adapter Pattern para mantener independencia entre capas de dominio e infraestructura.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.application.internal.outboundservices.tokens.TokenService
 * @see com.codares.logistics.iam.infrastructure.tokens.jwt.services.TokenServiceImpl
 */
public interface BearerTokenService extends TokenService {

    /**
     * Extrae el token JWT del header Authorization de la solicitud HTTP.
     * <p>
     * Busca el header "Authorization" que típicamente contiene "Bearer &lt;token&gt;".
     * Si el header existe y comienza con "Bearer ", extrae solo el token sin el prefijo.
     * </p>
     *
     * @param token la solicitud HTTP ({@link HttpServletRequest}) que contiene el token
     * @return String el token JWT sin el prefijo "Bearer ", o null si no se encuentra
     * @see jakarta.servlet.http.HttpServletRequest
     */
    String getBearerTokenFrom(HttpServletRequest token);

    /**
     * Genera un token JWT a partir de un objeto de autenticación de Spring Security.
     * <p>
     * Este método es una sobrecarga específica para generar tokens desde un objeto {@link Authentication}
     * que contiene el usuario autenticado y sus autoridades. Es utilizado tras completar la autenticación.
     * </p>
     *
     * @param authentication el objeto de autenticación de Spring Security con usuario y autoridades
     * @return String el token JWT firmado y codificado en Base64
     * @see org.springframework.security.core.Authentication
     */
    String generateToken(Authentication authentication);
}
