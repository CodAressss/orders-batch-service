package com.codares.logistics.shared.interfaces.rest.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para agregar CorrelationId a cada request/response.
 * <p>
 * Este filtro implementa el patrón de trazabilidad distribuida (Distributed Tracing)
 * al asignar un único CorrelationId a cada solicitud HTTP. Este ID se propaga a través
 * de toda la aplicación y puede ser utilizado para:
 * </p>
 * <ul>
 *   <li>Correlacionar logs entre múltiples capas de la aplicación</li>
 *   <li>Rastrear una solicitud específica en los logs</li>
 *   <li>Facilitar debugging y troubleshooting</li>
 *   <li>Integración con sistemas de observabilidad (ELK, Datadog, etc.)</li>
 * </ul>
 * <p>
 * <strong>Flujo:</strong>
 * </p>
 * <ol>
 *   <li>Intercepta cada request entrante</li>
 *   <li>Obtiene o genera un CorrelationId (UUID)</li>
 *   <li>Lo almacena en MDC (Mapped Diagnostic Context) de SLF4J</li>
 *   <li>Lo propaga en el header de la response</li>
 *   <li>Se limpia al finalizar la request</li>
 * </ol>
 * <p>
 * <strong>Headers utilizados:</strong>
 * </p>
 * <ul>
 *   <li>Request: <code>X-Correlation-Id</code> (opcional, se genera si no existe)</li>
 *   <li>Response: <code>X-Correlation-Id</code> (siempre presente)</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.slf4j.MDC
 * @see org.springframework.cloud.sleuth.Tracer
 */
@Slf4j
@Component
public class CorrelationIdFilter implements Filter {

    /**
     * Nombre del header para el CorrelationId.
     * Estándar de la industria para trazabilidad distribuida.
     */
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /**
     * Nombre del key en MDC para almacenar el CorrelationId.
     * Los valores en MDC se incluyen automáticamente en los logs estructurados.
     */
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            try {
                // 1. OBTENER O GENERAR CorrelationId
                String correlationId = obtenerOGenerarCorrelationId(request);

                // 2. ALMACENAR EN MDC (Mapped Diagnostic Context)
                // Los logs automáticamente incluirán este valor
                org.slf4j.MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

                // 3. LOG DE INICIO
                logRequestInicio(request, correlationId);

                // 4. ENVOLVER LA RESPONSE PARA CAPTURAR EL BODY
                ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

                // 5. EJECUTAR LA CADENA DE FILTROS Y ENDPOINT
                try {
                    filterChain.doFilter(servletRequest, wrappedResponse);
                } finally {
                    // 6. AGREGAR CorrelationId AL HEADER DE RESPONSE
                    wrappedResponse.addHeader(CORRELATION_ID_HEADER, correlationId);

                    // 7. LOG DE FINALIZACIÓN
                    logRequestFinalizacion(request, wrappedResponse, correlationId);

                    // 8. FLUSH DE LA RESPONSE
                    wrappedResponse.copyBodyToResponse();
                }

            } finally {
                // 9. LIMPIAR MDC (importante para evitar memory leaks)
                org.slf4j.MDC.remove(CORRELATION_ID_MDC_KEY);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * Obtiene el CorrelationId del header de request o genera uno nuevo.
     * <p>
     * <strong>Lógica:</strong>
     * </p>
     * <ol>
     *   <li>Si el header <code>X-Correlation-Id</code> está presente → usarlo</li>
     *   <li>Si NO está presente → generar un nuevo UUID</li>
     * </ol>
     * <p>
     * Esto permite que servicios upstream (API Gateway, Load Balancer) generen
     * un CorrelationId que se propaga a través de toda la cadena de servicios.
     * </p>
     *
     * @param request HttpServletRequest
     * @return UUID del CorrelationId (nuevo o existente)
     */
    private String obtenerOGenerarCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            // Generar nuevo UUID si no existe
            correlationId = UUID.randomUUID().toString();
        }

        return correlationId;
    }

    /**
     * Registra el inicio de la request en los logs con su CorrelationId.
     * <p>
     * Información capturada:
     * </p>
     * <ul>
     *   <li>Método HTTP (GET, POST, etc.)</li>
     *   <li>URI de la solicitud</li>
     *   <li>IP del cliente</li>
     *   <li>User-Agent</li>
     *   <li>CorrelationId (incluido automáticamente en JSON)</li>
     * </ul>
     *
     * @param request HttpServletRequest
     * @param correlationId UUID del CorrelationId
     */
    private void logRequestInicio(HttpServletRequest request, String correlationId) {
        String metodo = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = obtenerIpCliente(request);
        String userAgent = request.getHeader("User-Agent");

        String fullUri = queryString != null ? uri + "?" + queryString : uri;

        log.info(
            "==> INCOMING REQUEST | Metodo: {} | URI: {} | IP: {} | UserAgent: {}",
            metodo,
            fullUri,
            clientIp,
            userAgent != null ? userAgent : "N/A"
        );
    }

    /**
     * Registra la finalización de la request con su respuesta y CorrelationId.
     * <p>
     * Información capturada:
     * </p>
     * <ul>
     *   <li>Método HTTP</li>
     *   <li>URI de la solicitud</li>
     *   <li>Status HTTP de la respuesta (200, 404, 500, etc.)</li>
     *   <li>Tiempo total de procesamiento (en ms)</li>
     *   <li>CorrelationId (incluido automáticamente en JSON)</li>
     * </ul>
     *
     * @param request HttpServletRequest
     * @param response ContentCachingResponseWrapper
     * @param correlationId UUID del CorrelationId
     */
    private void logRequestFinalizacion(HttpServletRequest request, ContentCachingResponseWrapper response, String correlationId) {
        String metodo = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        log.info(
            "<== OUTGOING RESPONSE | Metodo: {} | URI: {} | Status: {} | CorrelationId: {}",
            metodo,
            uri,
            status,
            correlationId
        );
    }

    /**
     * Obtiene la dirección IP real del cliente.
     * <p>
     * Considera headers de proxies y load balancers:
     * </p>
     * <ul>
     *   <li><code>X-Forwarded-For</code> (proxy reverso)</li>
     *   <li><code>X-Real-IP</code> (nginx)</li>
     *   <li><code>CF-Connecting-IP</code> (Cloudflare)</li>
     *   <li>RemoteAddr (conexión directa)</li>
     * </ul>
     *
     * @param request HttpServletRequest
     * @return Dirección IP del cliente
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For puede contener múltiples IPs, tomar la primera
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isEmpty()) {
            return cfConnectingIp;
        }

        return request.getRemoteAddr();
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No requiere inicialización
    }

    @Override
    public void destroy() {
        // No requiere limpieza
    }
}
