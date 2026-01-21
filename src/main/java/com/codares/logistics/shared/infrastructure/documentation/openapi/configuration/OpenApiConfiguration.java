package com.codares.logistics.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada para la documentación de la API REST utilizando OpenAPI/Swagger.
 * <p>
 * Esta clase configura la especificación OpenAPI para el microservicio Orders Batch Service,
 * incluyendo metadatos básicos de la API, esquemas de seguridad y documentación externa.
 * Utiliza SpringDoc OpenAPI para generar automáticamente la documentación basada en las anotaciones
 * de los controladores REST.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Nombre de la aplicación inyectado desde las propiedades de Spring.
     * Se utiliza para configurar el título de la documentación OpenAPI.
     */
    @Value("${spring.application.name}")
    String applicationName;

    /**
     * Descripción de la aplicación inyectada desde las propiedades de Spring.
     * Se utiliza para configurar la descripción de la documentación OpenAPI.
     */
    @Value("${spring.application.description}")
    String applicationDescription;

    /**
     * Versión de la aplicación inyectada desde las propiedades de Spring.
     * Se utiliza para configurar la versión de la documentación OpenAPI.
     */
    @Value("${spring.application.version}")
    String applicationVersion;

    /**
     * Configura y devuelve la instancia de OpenAPI para el microservicio.
     * <p>
     * Este método crea la especificación OpenAPI con información básica (título, descripción, versión),
     * licencia, documentación externa y esquema de seguridad JWT Bearer. La configuración se basa
     * en las propiedades de la aplicación y se utiliza para generar la UI de Swagger.
     * </p>
     *
     * @return La instancia configurada de OpenAPI para la documentación de la API.
     */
    @Bean
    public OpenAPI ordersBatchServiceApi(){
        var openApi = new OpenAPI();
        openApi.info(new Info()
                .title(this.applicationName)
                .description(this.applicationDescription)
                .version(this.applicationVersion)
                .license(new License().name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Orders Batch Service wiki Documentation")
                        .url("https://orders-batch-service.wiki.github.io/docs"));


        // Add a security scheme

        final String securitySchemeName = "bearerAuth";

        openApi.addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));



        return openApi;

    }
}
