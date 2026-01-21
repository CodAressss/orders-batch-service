package com.codares.logistics.iam.infrastructure.authorization.sfs.pipeline;

import com.codares.logistics.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.codares.logistics.iam.infrastructure.tokens.jwt.BearerTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de Autorización Bearer para Solicitudes HTTP.
 * <p>
 * Este filtro intercepta cada solicitud HTTP y extrae el token JWT del header "Authorization".
 * Si el token es válido, lo procesa para crear un objeto de autenticación de Spring Security
 * y lo establece en el contexto de seguridad de la aplicación. De esta manera se implementa
 * la autenticación stateless basada en JWT.
 * </p>
 * <p>
 * <strong>Flujo de Procesamiento:</strong>
 * </p>
 * <ol>
 *   <li>Extrae el token Bearer del header Authorization</li>
 *   <li>Valida la firma y expiración del token usando {@link BearerTokenService}</li>
 *   <li>Si es válido, extrae el nombre de usuario del token</li>
 *   <li>Carga los detalles del usuario y autoridades desde BD</li>
 *   <li>Construye un objeto de autenticación usando {@link UsernamePasswordAuthenticationTokenBuilder}</li>
 *   <li>Establece la autenticación en el contexto de seguridad de Spring</li>
 * </ol>
 * <p>
 * Este filtro se ejecuta una sola vez por solicitud (extiende {@link OncePerRequestFilter}) y
 * siempre continúa la cadena de filtros incluso si hay errores (autenticación es degradable).
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see org.springframework.security.core.context.SecurityContextHolder
 * @see com.codares.logistics.iam.infrastructure.tokens.jwt.BearerTokenService
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
    private final BearerTokenService tokenService;


    @Qualifier("defaultUserDetailsService")
    private final UserDetailsService userDetailsService;

    /**
     * Constructor que inyecta las dependencias necesarias para el filtro.
     *
     * @param tokenService servicio de tokens JWT para validación y extracción
     * @param userDetailsService servicio para cargar detalles del usuario desde BD
     */
    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Procesa la solicitud HTTP para establecer la autenticación basada en JWT.
     * <p>
     * Este método es invocado una sola vez por solicitud HTTP. Intenta extraer y validar
     * el token Bearer del header Authorization. Si es válido:
     * </p>
     * <ol>
     *   <li>Extrae el nombre de usuario del token</li>
     *   <li>Carga los detalles del usuario y sus autoridades desde BD</li>
     *   <li>Construye un objeto de autenticación precargado (no requiere password)</li>
     *   <li>Lo establece en el contexto de seguridad de Spring</li>
     * </ol>
     * <p>
     * Si el token es inválido, nulo o no se encuentra, el filtro simplemente continúa
     * sin establecer autenticación, permitiendo que otros mecanismos de seguridad
     * (como formularios) manejen la solicitud.
     * </p>
     *
     * @param request solicitud HTTP que puede contener el token Bearer
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros para continuar con el procesamiento
     * @throws ServletException si hay un error en el procesamiento de servlet
     * @throws IOException si hay error en E/S de la solicitud/respuesta
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = tokenService.getBearerTokenFrom(request);
            LOGGER.info("Token: {}", token);
            
            if (token != null && tokenService.validateToken(token)) {
                String username = tokenService.getUsernameFromToken(token);
                var userDetails = userDetailsService.loadUserByUsername(username);
                SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request));
            } else {
                LOGGER.warn("Token is not valid");
            }

        } catch (Exception e) {
            LOGGER.error("Error en autenticación: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
