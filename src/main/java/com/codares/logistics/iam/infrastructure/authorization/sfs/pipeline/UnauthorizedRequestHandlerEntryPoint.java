package com.codares.logistics.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manejador de Solicitudes No Autorizadas (Unauthorized Entry Point).
 * <p>
 * Este componente implementa el punto de entrada para solicitudes HTTP que fallan
 * en la autenticación de Spring Security. Es invocado cuando:
 * </p>
 * <ul>
 *   <li>No se proporciona token Bearer en el header Authorization</li>
 *   <li>El token Bearer es inválido o expirado</li>
 *   <li>La credencial no puede ser validada por ningún mecanismo de autenticación</li>
 * </ul>
 * <p>
 * El manejador responde con un código HTTP 401 (Unauthorized) con un mensaje
 * descriptivo en formato JSON, siguiendo el estándar de respuestas de error de la aplicación.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.security.web.AuthenticationEntryPoint
 * @see org.springframework.security.core.AuthenticationException
 */

@Component
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnauthorizedRequestHandlerEntryPoint.class);

    /**
     * Maneja solicitudes no autorizadas devolviendo un código HTTP 401 con respuesta JSON formateada.
     * <p>
     * Este método es invocado automáticamente por Spring Security cuando detecta que una
     * solicitud no tiene credenciales válidas. Devuelve una respuesta JSON consistente con
     * el formato de errores de la aplicación, facilitando el consumo por clientes REST.
     * </p>
     * <p>
     * <strong>Casos de Invocación:</strong>
     * </p>
     * <ul>
     *   <li>Token Bearer ausente en el header Authorization</li>
     *   <li>Token Bearer presente pero inválido (firma alterada, expirado, etc.)</li>
     *   <li>Usuario en el token no existe en la base de datos</li>
     *   <li>Acceso sin credenciales a recurso protegido (@PreAuthorize, @Secured)</li>
     * </ul>
     *
     * @param request solicitud HTTP que causó la excepción de autenticación
     * @param response respuesta HTTP donde se enviará el código de error 401 en JSON
     * @param authenticationException excepción de autenticación que contiene detalles del error
     * @throws IOException si hay error en la E/S de la respuesta
     * @throws ServletException si hay error en el procesamiento de servlet
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
        LOGGER.error("Unauthorized request: {}", authenticationException.getMessage());
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String requestUri = request.getRequestURI();
        
        // Construir JSON de error manualmente
        String jsonResponse = String.format(
            "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"code\":\"NO_AUTORIZADO\"," +
            "\"message\":\"No autorizado - Token requerido o inválido\",\"path\":\"%s\"}",
            timestamp, requestUri
        );
        
        response.getWriter().write(jsonResponse);
    }
}
