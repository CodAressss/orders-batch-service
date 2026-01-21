package com.codares.logistics.iam.infrastructure.authorization.sfs.model;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

/**
 * Constructor de Tokens de Autenticación UsernamePassword.
 * <p>
 * Esta clase utiliza el patrón Builder para construir objetos {@link UsernamePasswordAuthenticationToken}
 * que son utilizados por Spring Security para autenticar usuarios.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Crear tokens de autenticación configurados correctamente</li>
 *   <li>Inyectar detalles de la solicitud HTTP en el token</li>
 *   <li>Facilitar la integración con filtros de autorización</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 * @see org.springframework.security.core.userdetails.UserDetails
 */
public class UsernamePasswordAuthenticationTokenBuilder {

    /**
     * Construye un token de autenticación UsernamePassword.
     * <p>
     * Este método estático crea un token de autenticación configurado con:
     * <ul>
     *   <li>Principal: los detalles del usuario</li>
     *   <li>Credenciales: null (ya fueron validadas)</li>
     *   <li>Autoridades: roles del usuario desde UserDetails</li>
     *   <li>Detalles de la solicitud: información del cliente HTTP</li>
     * </ul>
     * </p>
     *
     * @param principal los detalles del usuario ({@link UserDetails})
     * @param request la solicitud HTTP actual con información del cliente
     * @return el token UsernamePasswordAuthenticationToken completamente configurado
     * @see UsernamePasswordAuthenticationToken
     * @see UserDetails
     */
    public static UsernamePasswordAuthenticationToken build(UserDetails principal, HttpServletRequest request) {
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }
}
