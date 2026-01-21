package com.codares.logistics.shared.interfaces.rest.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing) para la aplicación.
 * <p>
 * Permite que la aplicación maneje solicitudes HTTP desde diferentes orígenes,
 * lo cual es esencial para aplicaciones web modernas con frontend y backend separados.
 * </p>
 * <p>
 * Configuración actual:
 * - Permite todos los orígenes (*)
 * - Permite métodos: GET, POST, PUT, DELETE, PATCH, OPTIONS
 * - Permite todos los headers
 * </p>
 * <p>
 * NOTA DE SEGURIDAD: En producción, se recomienda especificar
 * orígenes específicos en lugar de usar "*" para mejorar la seguridad.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 2024
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    /**
     * Configura las reglas CORS para toda la aplicación.
     * <p>
     * Aplica la configuración a todos los endpoints (/**).
     * </p>
     *
     * @param registry el registro CORS donde se definen las reglas
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }
}
